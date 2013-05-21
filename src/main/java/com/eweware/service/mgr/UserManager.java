package main.java.com.eweware.service.mgr;

import main.java.com.eweware.service.base.CommonUtilities;
import main.java.com.eweware.service.base.date.DateUtils;
import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.mgr.ManagerInterface;
import main.java.com.eweware.service.base.mgr.ManagerState;
import main.java.com.eweware.service.base.mgr.SystemManager;
import main.java.com.eweware.service.base.payload.*;
import main.java.com.eweware.service.base.store.StoreManager;
import main.java.com.eweware.service.base.store.dao.*;
import main.java.com.eweware.service.base.store.dao.schema.SchemaSpec;
import main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema;
import main.java.com.eweware.service.base.store.dao.schema.type.UserProfilePermissions;
import main.java.com.eweware.service.base.store.dao.type.DAOUpdateType;
import main.java.com.eweware.service.base.store.dao.type.RecoveryMethodType;
import main.java.com.eweware.service.base.store.dao.type.UserAccountType;
import main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import main.java.com.eweware.service.base.type.TrackerType;
import main.java.com.eweware.service.rest.session.BlahguaSession;
import main.java.com.eweware.service.search.index.common.BlahguaFilterIndexReader;
import main.java.com.eweware.service.search.index.common.BlahguaIndexReaderDecorator;
import main.java.com.eweware.service.search.index.user.UserDataIndexable;
import main.java.com.eweware.service.search.index.user.UserDataIndexableInterpreter;
import main.java.com.eweware.service.user.validation.Login;
import main.java.com.eweware.service.user.validation.RecoveryCode;
import main.java.com.eweware.service.user.validation.RecoveryCodeComponents;
import main.java.com.eweware.service.user.validation.UserValidationMethod;
import org.apache.commons.codec.binary.Base64;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;
import proj.zoie.api.DataConsumer.DataEvent;
import proj.zoie.api.DefaultDirectoryManager;
import proj.zoie.api.ZoieException;
import proj.zoie.api.ZoieIndexReader;
import proj.zoie.api.ZoieIndexReader.SubReaderAccessor;
import proj.zoie.api.ZoieIndexReader.SubReaderInfo;
import proj.zoie.api.indexing.IndexReaderDecorator;
import proj.zoie.api.indexing.ZoieIndexableInterpreter;
import proj.zoie.impl.indexing.ZoieConfig;
import proj.zoie.impl.indexing.ZoieSystem;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceException;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 *         <p/>
 *         TODO add monitors for mutating variables or lists with mutating elements
 *         TODO add transaction-level management (rollbacks)
 */
public class UserManager implements ManagerInterface {

    private static final Logger logger = Logger.getLogger(UserManager.class.getName());

    private static UserManager singleton;

    private static final int ONE_DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

    private final boolean debug;
    private final boolean doIndex;
    private StoreManager storeManager;
    private SystemManager systemManager;
    private MailManager mailManager;
    private ManagerState state = ManagerState.UNKNOWN;
    private final File indexDir;
    private final int batchSize;
    private final long batchDelay;
    private final Integer returnedObjectLimit;

    public static UserManager getInstance() throws SystemErrorException {
        if (UserManager.singleton == null) {
            throw new SystemErrorException("user manager not initialized", ErrorCodes.SERVER_NOT_INITIALIZED);
        }
        return UserManager.singleton;
    }

    private ZoieSystem<BlahguaFilterIndexReader, UserDAO> indexingSystem;

    public UserManager(Boolean debug, Boolean doIndex, String indexDir, String batchSize, String batchDelay, Integer returnedObjectLimit) {
        this.debug = (debug == Boolean.TRUE);
        this.doIndex = (doIndex == Boolean.TRUE);
        this.indexDir = new File(indexDir);
        this.batchSize = Integer.parseInt(batchSize);
        this.batchDelay = Long.parseLong(batchDelay);
        this.returnedObjectLimit = returnedObjectLimit;
        UserManager.singleton = this;
        this.state = ManagerState.INITIALIZED;
        if (doIndex) {
            System.out.println("*** UserManager Initializing ***");
            final File searchDir = this.indexDir.getParentFile();
            if (!searchDir.exists()) {
                System.out.println("*** UserManager: Search directory '" + searchDir + "' doesn't exist. Creating it...");
                try {
                    searchDir.mkdirs();
                } catch (Exception e) {    // fall through
                }
                if (!searchDir.exists()) {
                    throw new WebServiceException("Couldn't create search directory index '" + searchDir + "'. UserManager aborting.");
                }
            }
            System.out.println("*** User Index: " + this.indexDir.getAbsolutePath() + " ***");
        } else {
            System.out.println("*** UserManager search disabled ***");
        }
        System.out.println("*** UserManager initialized ***");
    }

    public boolean doIndex() {
        return doIndex;
    }

    /**
     * Called by Spring to start the manager.
     */
    public void start() {
        try {
            storeManager = MongoStoreManager.getInstance();
            systemManager = SystemManager.getInstance();
            mailManager = MailManager.getInstance();
            InitializeUserSearch(); // TODO should be its own service
            this.state = ManagerState.STARTED;
            System.out.println("*** UserManager started ***");
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
    }

    /**
     * Called by Spring to shut down the manager.
     */
    public void shutdown() {
        if (doIndex()) {
            indexingSystem.shutdown();
        }

        this.state = ManagerState.SHUTDOWN;
        System.out.println("*** UserManager shut down ***");
    }

    public ManagerState getState() {
        return this.state;
    }

    /**
     * Registers a user. First a UserDAO is created with the username and
     * a user account with the username (lc), digest, and password.
     *
     * @param localeId
     * @param username The username
     * @param password The password
     * @return The user payload with the userId and username.
     * @throws InvalidRequestException
     * @throws SystemErrorException
     * @throws StateConflictException
     */
    public UserPayload registerUser(LocaleId localeId, String username, String password) throws InvalidRequestException, SystemErrorException, StateConflictException {
        ensureReady();
        boolean createdAccount = false;
        UserDAO userDAO = null;
        try {
            username = Login.ensureUsernameString(username);
            password = Login.ensurePasswordString(password);

            ensureUsernameNotExists(username); // this is just a prelim check: final is at DB monitor level

            userDAO = getStoreManager().createUser();
            userDAO.initToDefaultValues(localeId);
            userDAO.setUsername(username);
            userDAO._insert();

            // Store sensitive data in user account record. Makes sure
            // that sensitive data never goes to the client (no corresponding Payload class).
            final UserAccountDAO userAccount = getStoreManager().createUserAccount(userDAO.getId());
            try {
                final String[] saltedPassword = Login.createSaltedPassword(password);
                userAccount.setCanonicalUsername(Login.makeCanonicalUsername(username));
                userAccount.setDigest(saltedPassword[0]);
                userAccount.setSalt(saltedPassword[1]);
                userAccount.setAccountType(UserAccountType.STANDARD.getCode());
                userAccount._insert();
            } catch (Exception e) {
                final StringBuilder b = new StringBuilder("Failed to create account for username'");
                b.append(username);
                b.append("'");
                if (e instanceof DuplicateKeyException) {
                    b.append(" because account already exists");
                }
                throw new SystemErrorException(b.toString(), e, ErrorCodes.SERVER_SEVERE_ERROR);
            }
            createdAccount = true;

            if (doIndex()) {
                maybeUpdateUserInIndex(userDAO);
            }

            return new UserPayload(userDAO);
        } finally {
            if (!createdAccount && userDAO != null) {
                try {
                    userDAO._deleteByPrimaryId();
                } catch (SystemErrorException e) {
                    logger.warning("Failed to roll back UserDAO for userId '" + userDAO.getId() + "' username '" + username + "' after failing to create an account for it");
                }
            } else {
                /* failure to create account will be logged by caller with corresponding SystemErrorException */
            }
        }

//        final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.CREATE_USER);
//        tracker.setUserId(userDAO.getId());
//        TrackingManager.getInstance().track(LocaleId.en_us, tracker);

    }

    /**
     * <p>Set, update, or delete user account fields.</p>
     *
     * @param userId           The user id
     * @param setEmail         If true, set or update email address; else, ignore it.
     * @param emailAddress     The email address. If null and setEmail is true, it will be deleted.
     * @param setChallenge     ditto
     * @param challengeAnswer1 ditto
     */
    public void setUserAccountData(String userId, boolean setEmail, String emailAddress, boolean setChallenge, String challengeAnswer1) throws SystemErrorException, StateConflictException, InvalidAuthorizedStateException, InvalidRequestException {
        ensureReady();
        if (!setEmail && !setChallenge) {
            return; // nothing to do
        }
        if (userId == null) {
            throw new InvalidAuthorizedStateException("no session", ErrorCodes.UNAUTHORIZED_USER);
        }

        checkUserAccountFieldLengths(emailAddress, challengeAnswer1);

        final UserAccountDAO userAccountDAO = getStoreManager().createUserAccount(userId);
        final boolean update = userAccountDAO._exists();

        if (setEmail) {
            // TODO: validate it!
            userAccountDAO.setEmailAddress(emailAddress);
        }
        if (setChallenge) {
            userAccountDAO.setSecurityChallengeAnswer1(challengeAnswer1);
        }
        if (update) {
            userAccountDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
        } else {
            userAccountDAO._insert();
        }
    }

    private void checkUserAccountFieldLengths(String emailAddress, String challengeAnswer1) throws InvalidRequestException {
        if ((emailAddress != null && emailAddress.length() > 64) ||
                (challengeAnswer1 != null && challengeAnswer1.length() > 64)) {
            throw new InvalidRequestException("Some field length exceeded max", ErrorCodes.INVALID_INPUT);
        }

    }

    /**
     * Used to efficiently check whether a username is already taken.
     *
     * @param username The username
     * @throws SystemErrorException
     * @throws StateConflictException  Thrown if the username is already taken
     * @throws InvalidRequestException Thrown if the specified username is either null or an empty string
     */
    public void ensureUsernameNotExists(String username) throws SystemErrorException, StateConflictException, InvalidRequestException {
        ensureReady();
        if (username == null || username.length() == 0) {
            throw new InvalidRequestException("username '" + username + "' is either null or empty", ErrorCodes.INVALID_INPUT);
        }
        if (usernameExistsP(username)) {
            throw new StateConflictException("username '" + username + "' already exists", ErrorCodes.ALREADY_EXISTS_USER_WITH_USERNAME);
        }
    }

    /**
     * <p>Returns true if the username already exists.</p>
     *
     * @param username The username
     * @return True if it exists
     * @throws SystemErrorException
     */
    public boolean usernameExistsP(String username) throws SystemErrorException {
        ensureReady();
        if (username == null) {
            return false;
        }
        final UserAccountDAO dao = getStoreManager().createUserAccount();
        dao.setCanonicalUsername(Login.makeCanonicalUsername(username));
        return dao._exists();
    }

    /**
     * Logs in a user. If the user is authenticated, the session is marked as authenticated
     * for as long as it lasts.
     *
     * @param locale
     * @param username The username
     * @param password The password for authentication
     * @param request  The http servlet request
     */
    public void loginUser(LocaleId locale, String username, String password, HttpServletRequest request)
            throws InvalidRequestException, SystemErrorException, InvalidAuthorizedStateException, ResourceNotFoundException {
        ensureReady();
        username = Login.ensureUsernameString(username);
        password = Login.ensurePasswordString(password);

        UserAccountDAO accountDAO = getStoreManager().createUserAccount();
        final String canonicalUsername = Login.makeCanonicalUsername(username);
        accountDAO.setCanonicalUsername(canonicalUsername);

        accountDAO = (UserAccountDAO) accountDAO._findByCompositeId(new String[]{UserAccountDAO.PASSWORD_DIGEST, UserAccountDAO.PASSWORD_SALT, UserAccountDAO.USER_ACCOUNT_TYPE}, UserAccountDAO.CANONICAL_USERNAME);
        if (accountDAO == null) {
            throw new ResourceNotFoundException("No such user '" + username + "'", ErrorCodes.UNAUTHORIZED_USER);
        }

        BlahguaSession.destroySession(request, true);

        if (Login.authenticate(accountDAO.getDigest(), accountDAO.getSalt(), password)) {
            BlahguaSession.markAuthenticated(request, accountDAO.getId(), accountDAO.getAccountType(), canonicalUsername);
        } else {
            BlahguaSession.markAnonymous(request);
            throw new InvalidAuthorizedStateException("User not authorized", ErrorCodes.UNAUTHORIZED_USER);
        }
    }


    /**
     * Logs out a user. It's been established that the user is authenticated (logged in).
     *
     * @param en_us
     * @param request
     * @param userId
     */
    public void logoutUser(LocaleId en_us, HttpServletRequest request, String userId) throws SystemErrorException {
        ensureReady();
        BlahguaSession.destroySession(request, false);
    }

    /**
     * Creates or updates the user profile.
     *
     * @param localeId
     * @param profile    The profile to set or change
     * @param userId     The user id
     * @param createOnly If true, we are creating the profile.
     * @return UserProfilePayload  Returns the profile with its current settings if createOnly is true; else
     *         returns null (since PUT operations don't return the payload)
     * @throws InvalidRequestException
     * @throws SystemErrorException
     * @throws StateConflictException
     */
    public UserProfilePayload createOrUpdateUserProfile(LocaleId localeId, UserProfilePayload profile, String userId, boolean createOnly)
            throws InvalidRequestException, SystemErrorException, StateConflictException, ResourceNotFoundException, InvalidAuthorizedStateException {
        ensureReady();
        if (profile == null) {
            throw new InvalidRequestException("missing profile entity", profile, ErrorCodes.MISSING_INPUT_ENTITY);
        }
        if (userId == null) {
            throw new InvalidAuthorizedStateException("user id not available", ErrorCodes.INVALID_STATE_CODE);
        }
        checkUserProfileFieldLengths(profile);

        UserProfileDAO userProfileDAO = getStoreManager().createUserProfile(userId);
        final boolean profileExists = userProfileDAO._exists();

        if (profileExists && createOnly) {
            throw new StateConflictException("profile already exists for userId=" + userId, profile, ErrorCodes.ALREADY_EXISTS_USER_PROFILE);
        } else if (!profileExists && !createOnly) {
            throw new ResourceNotFoundException("profile for userId=" + userId + " doesn't exist", profile, ErrorCodes.NOT_FOUND_USER_PROFILE);
        }
        if (createOnly) {
            userProfileDAO.initToDefaultValues(LocaleId.en_us); // userId will not be overwritten
        }
        userProfileDAO.addFromMap(profile, true);

        try {
            if (profileExists) {
                userProfileDAO._updateByPrimaryId(DAOUpdateType.ABSOLUTE_UPDATE);
            } else {
                userProfileDAO._insert();
            }
        } catch (DuplicateKeyException e) {
            throw new StateConflictException("duplicate key: " + e.getMessage(), ErrorCodes.DUPLICATE_KEY);
        }
        return createOnly ? new UserProfilePayload(userProfileDAO.toMap()) : null; // nothing returned on an update
    }

    private void checkUserProfileFieldLengths(UserProfilePayload profile) throws InvalidRequestException {
        if (profile.getUserType() != null) {
            throw new InvalidRequestException("user type cannot be changed", profile, ErrorCodes.INVALID_UPDATE);
        }
        if ((profile.getCity() != null && profile.getCity().length() > 32) || (profile.getCountry() != null && profile.getCountry().length() > 32) || (profile.getDateOfBirth() != null && profile.getDateOfBirth().length() > 32) || (profile.getGender() != null && profile.getGender().length() > 10) || (profile.getGPSLocation() != null && profile.getGPSLocation().length() > 64) || (profile.getIncomeRange() != null && profile.getIncomeRange().length() > 32) || (profile.getNickname() != null && profile.getNickname().length() > 32) || (profile.getRace() != null && profile.getRace().length() > 16) || (profile.getState() != null && profile.getState().length() > 32) || (profile.getZipCode() != null && profile.getZipCode().length() > 16)) {
            throw new InvalidRequestException("max field length exceeded on some field", ErrorCodes.INVALID_INPUT);
        }
    }


    /**
     * <p>Logs in user if recovery code is correct. Calling servlet will redirect to blahgua main page.</p>
     *
     * @param en_us             The locale id
     * @param request           The http servlet request
     * @param inputRecoveryCode The recovery code string representation
     * @return True if all checks out and the user is logged in.
     * @see RecoveryCode
     * @see RecoveryCodeComponents
     * @see UserAccountDAO
     */
    public boolean recoverUserAndRedirectToMainPage(LocaleId en_us, HttpServletRequest request, String inputRecoveryCode) throws SystemErrorException, InvalidAuthorizedStateException {
        ensureReady();
        String userId = null;
        String canonicalUsername = null;
        if (!getSystemManager().isCryptoOn()) {
            try {
                final String decoded = new String(Base64.decodeBase64(inputRecoveryCode), "UTF8");
                final String[] parts = decoded.split("\\|");
                if (parts.length != 2) {
                    throw new SystemErrorException("While recovering user and sending to main page: crypto workaround failed; has " + parts.length + " components", ErrorCodes.SERVER_CRYPT_ERROR);
                }
                userId = parts[0];
                canonicalUsername = parts[1];
            } catch (UnsupportedEncodingException e) {
                throw new SystemErrorException("error decoding base64", e, ErrorCodes.SERVER_CRYPT_ERROR);
            }
        } else {
            final RecoveryCodeComponents inputComponents = RecoveryCode.getRecoveryComponents(inputRecoveryCode);
            userId = inputComponents.getUserId();
            canonicalUsername = inputComponents.getCanonicalUsername();
        }

        // Check recovery components against account
        final UserAccountDAO userAccountDAO =
                (UserAccountDAO) getStoreManager().createUserAccount(userId)
                        ._findByPrimaryId(UserAccountDAO.CANONICAL_USERNAME, UserAccountDAO.USER_ACCOUNT_TYPE,
                                UserAccountDAO.RECOVERY_CODE_STRING, UserAccountDAO.RECOVERY_CODE_EXPIRATION_DATE);
        if (userAccountDAO == null) {
            throw new InvalidAuthorizedStateException("Illegal attempt to access system - no ac", ErrorCodes.NOT_FOUND_USER_ACCOUNT);
        }
        if (!userAccountDAO.getCanonicalUsername().equals(canonicalUsername)) {
            throw new InvalidAuthorizedStateException("Illegal access username '" + canonicalUsername + "'", ErrorCodes.INVALID_USERNAME);
        }

        // Check expiration
        final Date expiration = userAccountDAO.getRecoveryCodeExpiration();
        if (expiration == null || new Date().after(expiration)) {
            // TODO leave it in the system and let an overnight job delete the date.. maybe not worth even deleting it
            throw new InvalidAuthorizedStateException("Illegal access", ErrorCodes.RECOVERY_CODE_EXPIRED);
        }

        // Check code
        final String recoveryCode = userAccountDAO.getRecoveryCode();
        if (recoveryCode == null || !recoveryCode.equals(inputRecoveryCode)) {
            throw new InvalidAuthorizedStateException("Illegal access", ErrorCodes.RECOVERY_CODE_INVALID);
        }

        // Enable session!
        BlahguaSession.markAuthenticated(request, userId, userAccountDAO.getAccountType(), canonicalUsername);

        // redirect to blahgua
        return true;
    }

    /**
     * <p>When a user forgets his password, sends email to the user with a recovery url.</p>                                   cre
     * <p>The user must have registered an email address in his profile.</p>
     *
     * @param localeId        The locale id
     * @param username        The username
     * @param emailAddress    The email address submitted by the user (for verification only)
     * @param challengeAnswer (Optional): The challenge answer submitted by the user (for verification)
     */
    public void recoverUser(LocaleId localeId, String username, String emailAddress, String challengeAnswer)
            throws InvalidRequestException, SystemErrorException, ResourceNotFoundException, InvalidAuthorizedStateException {
        ensureReady();
        if (username == null || username.length() > 64) {
            throw new InvalidRequestException("invalid username", ErrorCodes.INVALID_USERNAME);
        }
        checkUserAccountFieldLengths(emailAddress, challengeAnswer);

        UserAccountDAO userAccountDAO = getStoreManager().createUserAccount();
        final String canonicalUsername = Login.makeCanonicalUsername(username);
        userAccountDAO.setCanonicalUsername(canonicalUsername);
        final String[] fieldsToReturnHint = {UserAccountDAO.ID, UserAccountDAO.EMAIL_ADDRESS, UserAccountDAO.CHALLENGE_ANSWER_1};
        userAccountDAO = (UserAccountDAO) userAccountDAO._findByCompositeId(fieldsToReturnHint, UserAccountDAO.CANONICAL_USERNAME);
        if (userAccountDAO == null) {
            throw new ResourceNotFoundException("user not registered", ErrorCodes.NOT_FOUND_USER_ACCOUNT);
        }
        final String userId = userAccountDAO.getId();

        // User eligible only if there's an email address and the one he input is the same
        final String email = userAccountDAO.getEmailAddress();
        if (email == null) {
            throw new ResourceNotFoundException("no way to recover account: missing email", ErrorCodes.NOT_FOUND_USER_PROFILE);
        }
        if (!email.equals(emailAddress)) {
            throw new InvalidAuthorizedStateException("invalid email address provided by user", ErrorCodes.INVALID_EMAIL_ADDRESS);
        }
        if (challengeAnswer != null) {
            final String profileChallengeAnswer = userAccountDAO.getSecurityChallengeAnswer1();
            if (profileChallengeAnswer == null || !profileChallengeAnswer.equals(challengeAnswer)) {
                throw new InvalidAuthorizedStateException("invalid answer to challenge question", ErrorCodes.INVALID_INPUT);
            }
        }

        // Stash recovery code data in user account
        final UserAccountDAO updateAccountDAO = getStoreManager().createUserAccount(userId);
        RecoveryCode recoveryCode = null;
        String nonCryptoUrlEncodedBase64RecoveryCode = null;
        if (!getSystemManager().isCryptoOn()) {
            try {
                final String codeString = userId + "|" + canonicalUsername;
                final String base64Encoded = Base64.encodeBase64String(codeString.getBytes("UTF-8"));
                nonCryptoUrlEncodedBase64RecoveryCode = URLEncoder.encode(base64Encoded, "UTF-8");
                updateAccountDAO.setRecoveryCode(base64Encoded);
            } catch (UnsupportedEncodingException e) {
                throw new SystemErrorException("workaround error", e, ErrorCodes.SERVER_CRYPT_ERROR);
            }
        } else {   // use crypto
            recoveryCode = RecoveryCode.createRecoveryCode(userId, canonicalUsername);
            updateAccountDAO.setRecoveryCode(recoveryCode.makeRecoveryCodeString());
        }
        updateAccountDAO.setRecoveryCodeExpiration(new Date(System.currentTimeMillis() + (ONE_DAY_IN_MILLIS)));
        updateAccountDAO.setRecoverySetMethod(RecoveryMethodType.EMAIL.getCode());
        updateAccountDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

        contactUser(recoveryCode, email, nonCryptoUrlEncodedBase64RecoveryCode);
    }


    private void contactUser(RecoveryCode recoveryCode, String emailAddress, String nonCryptoUrlEncodedBase64RecoveryCode) throws SystemErrorException {
        try {
            getMailManager().send(emailAddress, "Blahgua Account Recovery", makeAccountRecoveryBody(recoveryCode, nonCryptoUrlEncodedBase64RecoveryCode));
        } catch (MessagingException e) {
            throw new SystemErrorException("unable to recover account due to email system error", e, ErrorCodes.EMAIL_SYSTEM_ERROR);
        } catch (UnsupportedEncodingException e) {
            throw new SystemErrorException("encd", e, ErrorCodes.SERVER_CRYPT_ERROR);
        }
    }

    private String makeAccountRecoveryBody(RecoveryCode recoveryCode, String nonCryptoUrlEncodedBase64RecoveryCode) throws UnsupportedEncodingException, SystemErrorException {
        final StringBuilder msg = new StringBuilder("Hi there!");
        msg.append("<p>Someone requested an account recovery for your email address on Blahgua.</p>");
        msg.append("<p>If you did not request this, just ignore this email. Your account is safe!</p>");
        msg.append("<p>If you do want to reset your password, ");
        final String endpoint = getSystemManager().isDevMode() ? getSystemManager().getDevRestEndpoint() : getSystemManager().getClientServiceEndpoint();
        msg.append("<a href='http://");
        msg.append(endpoint);
        msg.append("/recover?n=");
        if (!getSystemManager().isCryptoOn()) {
            msg.append(nonCryptoUrlEncodedBase64RecoveryCode);
        } else {
            msg.append(URLEncoder.encode(recoveryCode.makeRecoveryCodeString(), "UTF-8"));
        }
        msg.append("'>just follow this link</a> within the next 24 hours. We'll show you what to do.</p>");
        msg.append("<div>Thanks!</div><div>The Blahgua Team</div>");
        return msg.toString();
    }

    /**
     * Initiates registration of a user into a group.
     * <p><b>Not in use.</b></p>
     *
     * @param localeId
     * @param userId        The user's id.
     * @param validationKey A validation method-specific input string. For example, for
     *                      an email validation method style, this would be the user's email address.
     *                      Some validation methods don't require a key, in which case this may be null.
     * @param groupId       The he group into which the user will be entered in a pending state
     * @return UserPayload  If userId is provided, returns null; else returns a payload with the user's id, displayName, and groupId
     * @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws StateConflictException
     */
    public UserGroupPayload registerUserInGroup(LocaleId localeId, String userId, String validationKey, String groupId) throws InvalidRequestException, StateConflictException, ResourceNotFoundException, SystemErrorException, InvalidUserValidationKey {
        ensureReady();
        if (CommonUtilities.isEmptyString(groupId)) {
            throw new InvalidRequestException("groupId required to join user to a group", ErrorCodes.MISSING_GROUP_ID);
        }
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("userId required to join user to a group", ErrorCodes.MISSING_USER_ID);
        }
        if (!getStoreManager().createUser(userId)._exists()) {
            throw new InvalidRequestException("userId=" + userId + " does not exist", ErrorCodes.NOT_FOUND_USER_ID);
        }
        final GroupDAO groupDAO = (GroupDAO) getStoreManager().createGroup(groupId)._findByPrimaryId(GroupDAO.DISPLAY_NAME, GroupDAO.USER_VALIDATION_METHOD, GroupDAO.USER_VALIDATION_PARAMETERS);
        if (groupDAO == null) {
            throw new ResourceNotFoundException("no group exists with groupId=" + groupId, ErrorCodes.NOT_FOUND_GROUP_ID);
        }

        final UserValidationMethod vmeth = UserValidationMethod.getValidationMethod(groupDAO.getValidationMethod());
        if (vmeth == null) {
            throw new InvalidRequestException("group validation method is obsolete or invalid; groupId=" + groupId + " method='" + groupDAO.getValidationMethod() + "'");
        }
//        vmeth.validateKey(validationKey, groupDAO.getValidationParameters());

        if (getStoreManager().createUserGroup(userId, groupId)._exists()) {
            throw new InvalidRequestException("userId=" + userId + " has already joined groupId=" + groupId, ErrorCodes.USER_ALREADY_JOINED_GROUP);
        }

        final AuthorizedState defaultState = vmeth.getDefaultAuthorizationState();
//        final String validationCode = vmeth.startValidation(userId, groupId, groupDAO.getDisplayName(), validationKey);
//        final String validationCode = AuthorizedState.A.toString();  // TODO now carte blanche authorization as we don't have any way to change it

        // Add user to the group with authorized state
        updateUserStatus(LocaleId.en_us, userId, groupId, defaultState.toString());

//        final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.USER_TO_GROUP_STATE_CHANGE);
//        tracker.setUserId(userId);
//        tracker.setGroupId(groupId);
//        tracker.setState(defaultState.toString());
//        TrackingManager.getInstance().track(LocaleId.en_us, tracker);

        final UserGroupPayload payload = new UserGroupPayload(userId, groupId);
        payload.setState(defaultState.toString());
        return payload;
    }

    /**
     * Updates username.
     *
     * @param localeId
     * @param request
     * @param newUsername
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws InvalidRequestException
     */
    public void updateUsername(LocaleId localeId, HttpServletRequest request, String newUsername) throws InvalidAuthorizedStateException, InvalidRequestException,
            StateConflictException, SystemErrorException, ResourceNotFoundException {
        ensureReady();
        if (!BlahguaSession.isAuthenticated(request)) {
            throw new InvalidAuthorizedStateException("user cannot perform this operation", ErrorCodes.UNAUTHORIZED_USER);
        }
        final String userId = BlahguaSession.getUserId(request);

        newUsername = Login.ensureUsernameString(newUsername);

        ensureUsernameNotExists(newUsername);

        // Must be changed in the accounts and username areas
        final UserAccountDAO userAccountDAO = getStoreManager().createUserAccount(userId);
        userAccountDAO.setCanonicalUsername(Login.makeCanonicalUsername(newUsername));
        userAccountDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

        final UserDAO userDAO = getStoreManager().createUser(userId);
        userDAO.setUsername(newUsername);
        userDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

        BlahguaSession.setUsername(request, newUsername);

        maybeUpdateUserInIndex(userDAO);
    }

    /**
     * <p>Updates a user's password. <i>Assumes that the user is authenticated!</i></p>
     *
     * @param en_us
     * @param request
     * @param userId
     * @param password @throws InvalidAuthorizedStateException
     * @throws InvalidRequestException
     * @throws SystemErrorException
     */
    public void updatePassword(LocaleId en_us, HttpServletRequest request, String userId, String password) throws
            InvalidAuthorizedStateException, InvalidRequestException, SystemErrorException {
        ensureReady();
        password = Login.ensurePasswordString(password);
        final String[] pwd;
        try {
            pwd = Login.createSaltedPassword(password);
            final UserAccountDAO userAccountDAO = getStoreManager().createUserAccount(userId);
            userAccountDAO.setDigest(pwd[0]);
            userAccountDAO.setSalt(pwd[1]);
            userAccountDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
        } catch (Exception e) {
            throw new SystemErrorException("Failed to update password due to system error", e, ErrorCodes.SERVER_CRYPT_ERROR);
        }

    }


    /**
     * Returns a user's data by user Id
     *
     * @param localeId
     * @param stats          If true, return user stats
     * @param statsStartDate if stats is true, format is yymmdd (e.g., August 27, 2012 is 120827
     * @param statsEndDate   if stats is true, format is yymmdd (e.g., August 27, 2012 is 120827
     * @return
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws InvalidRequestException
     * @throws ResourceNotFoundException
     */
    public UserPayload getUserInfo(LocaleId localeId, String userId, Boolean stats, String statsStartDate, String statsEndDate) throws InvalidRequestException, ResourceNotFoundException, SystemErrorException {
        ensureReady();
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("invalid user id", ErrorCodes.INVALID_USER_ID);
        }

        final UserDAO userDAO = (UserDAO) getStoreManager().createUser(userId)._findByPrimaryId();
        if (userDAO == null) {
            throw new ResourceNotFoundException("user not found", ErrorCodes.NOT_FOUND_USER_ID);
        }

        if (stats) { // TODO it is an understatement to say that this is inefficient
            fetchAndAddUserTrackers(userDAO.getId(), statsStartDate, statsEndDate, userDAO);
        }

        return new UserPayload(userDAO);
    }

    public UserBlahInfoPayload getUserInfoForBlah(String userId, String blahId) throws SystemErrorException, InvalidRequestException {
        ensureReady();
        final UserBlahInfoDAO dao = (UserBlahInfoDAO) storeManager.createUserBlahInfo(userId, blahId)._findByCompositeId(null, UserBlahInfoDAO.USER_ID, UserBlahInfoDAO.BLAH_ID);
        if (dao == null) {
            throw new InvalidRequestException("no user info for blah", ErrorCodes.INVALID_STATE_CODE);
        }
        final UserBlahInfoPayload entity = new UserBlahInfoPayload(dao);
        entity.remove(UserBlahInfoDAO.USER_ID);
        entity.remove(UserBlahInfoDAO.BLAH_ID);
        entity.remove(UserBlahInfoDAO.ID);
        return entity;
    }

    public UserProfilePayload getUserProfileById(LocaleId localeId, String userId)
            throws InvalidRequestException, SystemErrorException, ResourceNotFoundException {
        ensureReady();
        final UserProfileDAO userProfileDAO = getUserProfileDAO(userId);
        if (userProfileDAO == null) {
            throw new ResourceNotFoundException("no user profile id=" + userId, ErrorCodes.NOT_FOUND_USER_PROFILE);
        }
        return new UserProfilePayload(userProfileDAO.toMap());
    }

    public UserProfileSchema getUserProfileSchema(LocaleId localeId) throws SystemErrorException {
        ensureReady();
        return UserProfileSchema.getSchema(localeId);
    }


    public List<Map<String, String>> getUserProfileDescriptors(LocaleId en_us, HttpServletRequest request, List<String> userIds) throws InvalidRequestException, ResourceNotFoundException, SystemErrorException {
        final List<Map<String, String>> descriptors = new ArrayList<Map<String, String>>();
        if (userIds == null || userIds.size() == 0) {
            return descriptors;
        }
        for (String userId : userIds) {
            descriptors.add(getUserProfileDescriptor(en_us, request, userId));
        }
        return descriptors;
    }

    private static Map<Character, String> vowelToArticle = new HashMap<Character, String>(5);
    static {
        vowelToArticle.put('a', "An ");
        vowelToArticle.put('e', "An ");
        vowelToArticle.put('i', "An ");
        vowelToArticle.put('o', "An ");
    }

    // hastily put together
    public Map<String, String> getUserProfileDescriptor(LocaleId localeId, HttpServletRequest request, String userId)
            throws SystemErrorException, ResourceNotFoundException, InvalidRequestException {
        ensureReady();
        final StringBuilder descriptor = new StringBuilder();
        String nickname = null;
        String userImageId = null;

        boolean shownAge = false;
        boolean shownRace = false;
        boolean shownGender = false;
        boolean shownCity = false;
        boolean shownState = false;
        int count = 0;

        if (userId != null) {

            final UserDAO userDAO = (UserDAO) storeManager.createUser(userId)._findByPrimaryId(UserDAO.IMAGE_IDS);
            if (userDAO == null) {
                throw new ResourceNotFoundException("No such user id '" + userId + "'", ErrorCodes.NOT_FOUND_USER_ID);
            }
            final List<String> imageids = userDAO.getImageids();
            if (imageids != null && imageids.size() > 0) {
                userImageId = imageids.get(0);
            }

            final UserProfileDAO profile = getUserProfileDAO(userId);


            if (profile != null) {

                final UserProfileSchema schema = UserProfileSchema.getSchema(localeId);
                if (schema == null) {
                    throw new SystemErrorException("missing schema for user profile", ErrorCodes.SERVER_SEVERE_ERROR);
                }

                if (hasProfilePermission(profile.getNicknamePermissions())) {
                    nickname = profile.getNickname();
                }


                // TODO simplify the following blocks of code into an engine
                final Integer dobPermissions = profile.getDateOfBirthPermissions();
                if (hasProfilePermission(dobPermissions)) {
                    final Date dob = profile.getDateOfBirth(); // no need to look up data type
                    if (dob != null) {
                        descriptor.append("A ");
                        descriptor.append(CommonUtilities.getAgeInYears(dob));
                        descriptor.append(" year old");
                        shownAge = true;
                        count++;
                    }
                }

                final Integer racePermissions = profile.getRacePermissions();
                if (hasProfilePermission(racePermissions)) {
                    final String raceKey = profile.getRace();
                    if (raceKey != null && !raceKey.equals(UserProfileSchema.RACE_OTHER_SELECTION_KEY) && !raceKey.equals(UserProfileSchema.UNSPECIFIED_SELECTION_KEY)) {
                        final SchemaSpec spec = schema.getSpec(UserProfileDAO.USER_PROFILE_RACE);
                        if (spec != null) {
                            switch (spec.getDataType()) {
                                case ILS:
                                    final String race = (String) spec.getValidationMap().get(raceKey);
                                    if (race != null) {
                                        if (shownAge) {
                                            descriptor.append(" ");
                                        } else {
                                            final String article = vowelToArticle.get(Character.toLowerCase(race.charAt(0)));
                                            descriptor.append((article == null) ? "A " : article);
                                        }
                                        descriptor.append(race);
                                        shownRace = true;
                                        count++;
                                    } else {
                                        logger.warning("User id '" + userId + "' profile has race permissions but no race for key '" + raceKey + "'");
                                    }
                            }
                        }
                    }
                }

                final Integer genderPermissions = profile.getGenderPermissions();
                if (hasProfilePermission(genderPermissions)) {
                    final String genderKey = profile.getGender();
                    if (genderKey != null && !genderKey.equals(UserProfileSchema.UNSPECIFIED_SELECTION_KEY)) {
                        final SchemaSpec spec = schema.getSpec(UserProfileDAO.USER_PROFILE_GENDER);
                        if (spec != null) {
                            switch (spec.getDataType()) {
                                case ILS:
                                    final String gender = (String) spec.getValidationMap().get(genderKey);
                                    if (gender != null) {
                                        descriptor.append(shownAge || shownRace ? " " : "A ");
                                        descriptor.append(gender.toLowerCase());
                                        shownGender = true;
                                        count++;
                                    } else {
                                        logger.warning("User id '" + userId + "' profile has gender permissions but no gender for key '" + genderKey + "'");
                                    }
                                    break;
                                default:
                                    throw new SystemErrorException("gender data type has changed (expected list of string) and I don't know how to handle it");
                            }
                        }
                    }
                }

                final Integer cityPermissions = profile.getCityPermissions();
                if (hasProfilePermission(cityPermissions)) {
                    final String city = profile.getCity();
                    if (city != null && city.trim().length() != 0) {
                        final SchemaSpec spec = schema.getSpec(UserProfileDAO.USER_PROFILE_CITY);
                        if (spec != null) {
                            switch (spec.getDataType()) {
                                case S:
                                    if (!(shownAge || shownRace || shownGender)) {
                                        descriptor.append("Someone");
                                    }
                                    descriptor.append(" from ");
                                    descriptor.append(city);
                                    shownCity = true;
                                    count++;
                                    break;
                                default:
                                    throw new SystemErrorException("city data type has changed (expected String) and I don't know how to handle it");
                            }
                        }
                    } else {
                        logger.warning("User id '" + userId + "' profile has city permissions but no city");
                    }
                }

                final Integer statePermissions = profile.getStatePermissions();
                if (hasProfilePermission(statePermissions)) {
                    final String state = profile.getState();
                    if (state != null && state.trim().length() != 0) {
                        final SchemaSpec spec = schema.getSpec(UserProfileDAO.USER_PROFILE_STATE);
                        if (spec != null) {
                            switch (spec.getDataType()) {
                                case S:
                                    if (!(shownAge || shownRace || shownGender || shownCity)) {
                                        descriptor.append("Someone");
                                    }
                                    if (!shownCity) {
                                        descriptor.append(" from");
                                    } else {
                                        descriptor.append(',');
                                    }
                                    descriptor.append(' ');
                                    descriptor.append(state);
                                    shownState = true;
                                    count++;
                                    break;
                                default:
                                    throw new SystemErrorException("state data type has changed (expected String) and I don't know how to handle it");
                            }
                        }
                    }
                }

                final Integer countryPermissions = profile.getCountryPermissions();
                if (hasProfilePermission(countryPermissions)) {
                    final String countryKey = profile.getCountry();
                    if (countryKey != null && !countryKey.equals(UserProfileSchema.UNSPECIFIED_SELECTION_KEY)) {
                        final SchemaSpec spec = schema.getSpec(UserProfileDAO.USER_PROFILE_COUNTRY);
                        if (spec != null) {
                            switch (spec.getDataType()) {
                                case ILS:
                                    final String country = (String) spec.getValidationMap().get(countryKey);
                                    if (country != null && country.trim().length() != 0) {
                                        if (!(shownAge || shownRace || shownGender || shownCity || shownState)) {
                                            descriptor.append("Someone");
                                        }
                                        if (!(shownCity || shownState)) {
                                            descriptor.append(" from");
                                        } else {
                                            descriptor.append(',');
                                        }
                                        descriptor.append(' ');
                                        descriptor.append(country);
                                        count++;
                                    }
                                    break;
                                default:
                                    throw new SystemErrorException("country data type has changed (expected list of string) and I don't know how to handle it");
                            }
                        }
                    }
                }
            }
        }
        if (descriptor.length() == 0) {
            descriptor.append("An anonymous person.");
        } else if (shownRace && count == 1) {
            descriptor.append(" person");
        }

        // Create response
        final Map<String, String> map = new HashMap<String, String>(1);
        map.put("d", descriptor.toString());
        if (nickname != null) {
            map.put("n", nickname);
        }
        if (userImageId != null) {
            map.put("m", userImageId);
        }
        map.put("i", userId);

        return map;

    }

    private boolean hasProfilePermission(Integer permissions) {
        return ((permissions != null) &&
                ((permissions.equals(UserProfilePermissions.PUBLIC.getCode())) ||
                        (permissions.equals(UserProfilePermissions.MEMBERS.getCode()))));
    }

    private UserProfileDAO getUserProfileDAO(String userId) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException {
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("missing user id", userId, ErrorCodes.MISSING_USER_ID);
        }
        return (UserProfileDAO) getStoreManager().createUserProfile(userId)._findByPrimaryId();
    }


    /**
     * Adds trackers to user dao in a requested (or default) range.
     * If the start and end dates are not specified, only the current
     * month's tracker will be added (instead of ALL trackers for all time!).
     *
     * @param userId         The user's id
     * @param statsStartDate The range start date. Optional only if client desires only current tracker.
     *                       only if an end date is specified
     *                       (Never return everything into the indefinite past.)
     * @param statsEndDate   Optional end date.
     * @param userDAO        The user dao to which the tracker(s) will be added.
     * @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    private void fetchAndAddUserTrackers(final String userId, final String statsStartDate, final String statsEndDate, final UserDAO userDAO) throws InvalidRequestException, SystemErrorException {
        if (statsStartDate == null && statsEndDate != null) {
            throw new InvalidRequestException("stats start date (s) must be provided if an end date (e) is specified", ErrorCodes.INVALID_INPUT);
        }

        // TODO need to limit how many trackers are returned

        // user tracker ids are: <userId><2-digit year><2-digit month> (e.g., 5031b25d036408e9b4160b951208)
        // We ignore day of month as each tracker contains a whole month's worth of stats

        Calendar startDate = DateUtils.convertToCalendar(statsStartDate);
        Calendar endDate = DateUtils.convertToCalendar(statsEndDate);
        // We've made sure that the dates can be parsed as expected and are available as calendar instances for comparison
        List<UserTrackerDAO> trackers = null;
        if (org.apache.commons.lang3.time.DateUtils.isSameDay(startDate, endDate)) { // fetch single
            final String trackerId = TrackingManager.makeUserTrackerIdExternal(TrackerType.USER, startDate, userId);
            final UserTrackerDAO userTrackerDAO = (UserTrackerDAO) getStoreManager().createUserTracker(trackerId)._findByPrimaryId();
            if (userTrackerDAO != null) {
                trackers = new ArrayList<UserTrackerDAO>(1);
                trackers.add(userTrackerDAO);
            }
        } else { // range search
            final UserTrackerDAO userTrackerDAO = (UserTrackerDAO) getStoreManager().createUserTracker();
            final String from = TrackingManager.makeUserTrackerIdExternal(TrackerType.USER, startDate, userId);
            final String to = TrackingManager.makeUserTrackerIdExternal(TrackerType.USER, endDate, userId);
//            final String from = (statsStartDate == null)?null:statsStartDate.substring(0, 4);
//            final String to = (statsEndDate == null)?null:statsEndDate.substring(0, 4);
            final boolean sorted = true;
            final boolean fromInclusive = true;
            final boolean toInclusive = true;
            trackers = (List<UserTrackerDAO>) userTrackerDAO._findRangeSingleField(sorted, UserTrackerDAO.ID, from, fromInclusive, to, toInclusive);
        }
        userDAO.setStats(trackers == null ? new ArrayList<UserTrackerDAO>(0) : trackers);
    }

    /**
     * Convenience method to check whether user exists
     *
     * @param userId The user id
     * @param entity Any entity to pass in a resource not found exception
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException Thrown when the user can't be found
     */
    public void checkUserById(String userId, Object entity) throws ResourceNotFoundException, InvalidRequestException, SystemErrorException {
        ensureReady();
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("missing userId", ErrorCodes.MISSING_USER_ID);
        }
        if (!getStoreManager().createUser(userId)._exists()) {
            throw new ResourceNotFoundException("not found userId=" + userId, entity, ErrorCodes.NOT_FOUND_USER_ID);
        }
    }

    public UserGroupPayload getUserGroup(LocaleId localeId, String userId, String groupId) throws InvalidRequestException, ResourceNotFoundException, SystemErrorException {
        ensureReady();
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("missing userId", ErrorCodes.MISSING_USER_ID);
        }
        if (CommonUtilities.isEmptyString(groupId)) {
            throw new InvalidRequestException("missing groupId", ErrorCodes.MISSING_GROUP_ID);
        }
        final UserGroupDAO userGroupDAO = (UserGroupDAO) getStoreManager().createUserGroup(userId, groupId)._findByCompositeId(new String[]{UserGroupDAO.STATE}, UserGroupDAO.USER_ID, UserGroupDAO.GROUP_ID);
        if (userGroupDAO == null) {
            throw new ResourceNotFoundException("userId=" + userId + " has not joined groupId=" + groupId, ErrorCodes.USER_HAS_NOT_JOINED_GROUP);
        }
        final UserGroupPayload userGroupPayload = new UserGroupPayload();
        userGroupPayload.setUserId(userId);
        userGroupPayload.setGroupId(groupId);
        userGroupPayload.setState(userGroupDAO.getState());
        return userGroupPayload;
    }

    public List<GroupPayload> getUserGroups(LocaleId localeId, String userId, String state, Integer start, Integer count, String sortFieldName) throws InvalidRequestException, SystemErrorException {
        ensureReady();
        count = ensureCount(count);

        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("missing user id", ErrorCodes.MISSING_USER_ID);
        }
        if (state != null) {
            try {
                AuthorizedState.valueOf(state);
            } catch (Exception e) {
                throw new InvalidRequestException("requested an invalid group state", "state=" + state, ErrorCodes.INVALID_STATE_CODE);
            }
        }

        // TODO suboptimal double-query required here: consider denormalizing group data into usergroup
        final UserGroupDAO searchUserGroupDAO = getStoreManager().createUserGroup();
        searchUserGroupDAO.setUserId(userId);
        searchUserGroupDAO.setState(state);

        final List<UserGroupDAO> userGroupDAOs;
        // TODO we would obviate need of index for state field if we fetch all and filter results by state
        if (state == null) {
            userGroupDAOs = (List<UserGroupDAO>) searchUserGroupDAO._findManyByCompositeId(start, count, sortFieldName, new String[]{UserGroupDAO.GROUP_ID, UserGroupDAO.STATE}, UserGroupDAO.USER_ID);
        } else {
            userGroupDAOs = (List<UserGroupDAO>) searchUserGroupDAO._findManyByCompositeId(start, count, sortFieldName, new String[]{UserGroupDAO.GROUP_ID, UserGroupDAO.STATE}, UserGroupDAO.USER_ID, UserGroupDAO.STATE);
        }
        final int groupCount = userGroupDAOs.size();
        if (groupCount == 0) {
            return new ArrayList<GroupPayload>(0);
        }

        // TODO inefficient
        final List<GroupPayload> payload = new ArrayList<GroupPayload>(groupCount);
        for (UserGroupDAO ug : userGroupDAOs) {
            final GroupDAO groupDAO = (GroupDAO) getStoreManager().createGroup(ug.getGroupId())._findByPrimaryId(GroupDAO.DISPLAY_NAME, GroupDAO.DESCRIPTION);
            if (groupDAO != null) {
                final GroupPayload group = new GroupPayload(groupDAO);
                group.addFromMap(ug);
                payload.add(group);
            }
        }

        return payload;
    }

    private Integer ensureCount(Integer count) {
        if (count == null || count > returnedObjectLimit) {
            count = returnedObjectLimit;
        }
        return count;
    }

    /**
     * Join, accept, or suspend a user to/from a group.
     * Also allows the association to be deleted from the database.
     * Allowed state transitions:
     * <Does not exist> -> P
     * P, S -> A
     * A -> S
     * A, P, S -> <Deleted = Does not exist>
     *
     * @param localeId
     * @param userId   The user id
     * @param groupId  The group id
     * @param newState If AuthorizedState.P.getDescription, the user will be joined to the group in a pending state.
     *                 If AuthorizedState.A.getDescription, the user will be activated in the group.
     *                 If AuthorizedState.S.getDescription, the user will be suspended in the group.
     *                 If AuthorizedState.DT.getDescription, the user/group association will be deleted from the database.
     *                 //     * @param validationCode Validation code for user (needed when user is in P (pending) or S (suspended) state.
     *                 //     *                       If not null, this is simply inserted into the DB for future reference.
     * @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws StateConflictException
     */
    public void updateUserStatus(LocaleId localeId, String userId, String groupId, String newState)
            throws InvalidRequestException, StateConflictException, ResourceNotFoundException, SystemErrorException {
        ensureReady();
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("missing userId", ErrorCodes.MISSING_USER_ID);
        }
        if (CommonUtilities.isEmptyString(groupId)) {
            throw new InvalidRequestException("missing groupId", ErrorCodes.MISSING_GROUP_ID);
        }
        if (CommonUtilities.isEmptyString(newState)) {
            throw new SystemErrorException("missing state", ErrorCodes.MISSING_AUTHORIZATION_STATE);
        } else {
            try {
                newState = AuthorizedState.valueOf(newState).toString();
            } catch (IllegalArgumentException e) {
                throw new InvalidRequestException("invalid action code in request", "action=" + newState, ErrorCodes.INVALID_ACTION_CODE);
            }
        }

        final boolean join = newState.equals(AuthorizedState.P.toString());
        final boolean activate = newState.equals(AuthorizedState.A.toString());
        final boolean suspend = newState.equals(AuthorizedState.S.toString());
        final boolean delete = newState.equals(AuthorizedState.D.toString());
        if (!getStoreManager().createUser(userId)._exists()) {
            throw new ResourceNotFoundException("not found user with userId=" + userId, ErrorCodes.NOT_FOUND_USER_ID);
        }
        final GroupDAO groupDAO = (GroupDAO) getStoreManager().createGroup(groupId)._findByPrimaryId(GroupDAO.STATE);
        if (groupDAO == null) {
            throw new ResourceNotFoundException("not found group with groupId=" + groupId, ErrorCodes.NOT_FOUND_GROUP_ID);
        }
        if ((join || activate) && !(groupDAO.getState().equals(AuthorizedState.A.toString()))) {
            throw new StateConflictException("groupId " + groupId + " is inactive. A group must be active for a user to join or become active in it", "groupId=" + groupId,
                    ErrorCodes.INVALID_STATE_USER_CANNOT_JOIN_INACTIVE_GROUP);
        }
        // TODO 1. use composite _find instead, and 2. assuming here that DB is fully consistent: if not, also check the groupId:
        final UserGroupDAO found = (UserGroupDAO) getStoreManager().createUserGroup(userId, groupId)._findByCompositeId(null, UserGroupDAO.USER_ID, UserGroupDAO.GROUP_ID);
        if (found != null) { // user in group
            final String userGroupId = found.getId();
            final Object state = found.getState();
            if (join) { // but user already joined
                throw new StateConflictException("userId=" + userId + " already joined groupId=" + groupId, ErrorCodes.USER_ALREADY_JOINED_GROUP);
            } else if (activate) {
                if (state.equals(AuthorizedState.P.toString()) || state.equals(AuthorizedState.S.toString())) {

                    // Mark user as active
                    final UserGroupDAO userGroup = getStoreManager().createUserGroup(userGroupId);
                    userGroup.setState(AuthorizedState.A.toString());
                    userGroup._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
                } else {
                    throw new StateConflictException("user cannot be activated when its state is '" + state + "'; userId " + userId + " groupId " + groupId, ErrorCodes.USER_CANNOT_BE_ACTIVATED_WHEN_STATE_IS_NOT_P_OR_S);
                }
            } else if (suspend) {
                if (state.equals(AuthorizedState.A.toString())) {

                    // Mark user as suspended
                    final UserGroupDAO userGroup = getStoreManager().createUserGroup(userGroupId);
                    userGroup.setState(AuthorizedState.S.toString());
                    userGroup._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

//                    final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.USER_TO_GROUP_STATE_CHANGE);
//                    tracker.setUserId(userId);
//                    tracker.setGroupId(groupId);
//                    tracker.setState(AuthorizedState.S.toString());
//                    TrackingManager.getInstance().track(LocaleId.en_us, tracker);

                } else {
                    throw new StateConflictException("user in state '" + state + "' cannot be suspended; userId " + userId + " groupId " + groupId, ErrorCodes.USER_CANNOT_BE_SUSPENDED_IN_STATE_OTHER_THAN_A);
                }
            } else if (delete) {  // hard delete!
                // TODO should we change state to DT (and register and other methods would need to be aware of this)?
                getStoreManager().createUserGroup(userGroupId)._deleteByPrimaryId();

//                final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.USER_TO_GROUP_STATE_CHANGE);
//                tracker.setUserId(userId);
//                tracker.setGroupId(groupId);
//                tracker.setState(AuthorizedState.D.toString());
//                TrackingManager.getInstance().track(LocaleId.en_us, tracker);
            }
        } else { // no user/group obj
            if (join || activate) {
                final UserGroupDAO userGroup = getStoreManager().createUserGroup();
                userGroup.initToDefaultValues(localeId);
                userGroup.setUserId(userId);
                userGroup.setGroupId(groupId);
//                if (validationCode != null) {
//                    userGroup.setValidationCode(validationCode);
//                }
                userGroup.setState(newState);
                userGroup._insert();

                if (activate) {
                    groupDAO.setUserCount(1);
                    groupDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
                }
            } else {
                throw new StateConflictException("requested action=" + newState + ", but user id=" + userId + " is not joined to groupId=" + groupId, ErrorCodes.USER_MUST_INITIALLY_JOIN_GROUP_IN_STATE_P);
            }
        }
    }

    /**
     * <p>Deletes all images for the user. This is done through a soft-delete marker on the affected media daos.</p>
     * <p>A batch job looks for soft-deleted media records, deletes the corresponding image(s) from S3,
     * and then hard-deletes (if necessary) the media record itself.</p>
     */
    public void deleteAllMediaForUser(String userId) throws SystemErrorException, ResourceNotFoundException {
        ensureReady();
        final UserDAO userDAO = (UserDAO) storeManager.createUser(userId)._findByPrimaryId(UserDAO.IMAGE_IDS);
        if (userDAO == null) {
            throw new ResourceNotFoundException("no user id '" + userId + "'");
        }
        deleteMediaIdsForUser(userId, userDAO, userDAO.getImageids());
    }

    public void deleteMediaIdsForUser(String userId, UserDAO userDAO, List<String> mediaIds) throws SystemErrorException {
        ensureReady();
        if (mediaIds != null && mediaIds.size() > 0) {
            for (String mediaId : mediaIds) {
                final MediaDAO media = storeManager.createMedia(mediaId);
                if (media._exists()) { // ignore missing media dao: it might have been deleted in a previous broken attempt
                    media.setDeleted(true); // soft-delete
                    try {
                        media._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
                    } catch (Exception e) {
                        // reference to media id will remain in user dao, so we can consistently recover
                        throw new SystemErrorException("Failed to delete media id '" + mediaId + "' for user id '" + userId + "'", e, ErrorCodes.FAILED_TO_DELETE_IMAGE);
                    }
                }
            }
            userDAO.setImageIds(null); // all successful: break the bonds
            userDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
        }
    }


    private void ensureReady() throws SystemErrorException {
        if (state != ManagerState.STARTED) {
            throw new SystemErrorException("System not ready", ErrorCodes.SERVER_NOT_INITIALIZED);
        }
    }

    // Indexer
    // zoie index stuff to be integrated later...

    public void InitializeUserSearch() {
        if (doIndex()) {
            createZoieSystem();
        }
    }

    private void maybeUpdateUserInIndex(UserDAO user) throws SystemErrorException {
        if (!doIndex()) { // dbg
            return;
        }
        if (CommonUtilities.isEmptyString(user.getId())) {
            throw new SystemErrorException("missing userId", user, ErrorCodes.MISSING_USER_ID);
        }

//        final List<UserDAO> result = searchUserIndex(UserDAO.ID, user.getId());
//        if (!result.isEmpty()) {
//            throw new StateConflictException("userId has already been indexed", user);
//        }

        final String batchVersion = "0";
        final DataEvent<UserDAO> event = new DataEvent<UserDAO>(user, batchVersion);
        final List<DataEvent<UserDAO>> events = new ArrayList<DataEvent<UserDAO>>(1);
        events.add(event);
        try {
            this.indexingSystem.consume(events);
            if (debug) {
                System.out.println("Added: " + user);
            }
        } catch (ZoieException e) {
            throw new SystemErrorException("user indexer has a problem", e, ErrorCodes.SERVER_INDEXING_ERROR);
        }
    }

    public List<UserDAO> searchUserIndex(LocaleId localeId, String fieldName, String query) throws SystemErrorException {
        final List<UserDAO> users = new ArrayList<UserDAO>();
        if (!doIndex()) {
            users.add(getStoreManager().createUser());
            return users;
        }
        List<ZoieIndexReader<BlahguaFilterIndexReader>> readerList = null;
        try {
            // get the IndexReaders
            readerList = this.indexingSystem.getIndexReaders();

            // BlahguaFilterIndexReader instances can be obtained by calling
            // ZoieIndexReader.getDecoratedReaders()
            final List<BlahguaFilterIndexReader> decoratedReaders = ZoieIndexReader.extractDecoratedReaders(readerList);
            final SubReaderAccessor<BlahguaFilterIndexReader> subReaderAccessor = ZoieIndexReader.getSubReaderAccessor(decoratedReaders);

            // combine the readers
            final MultiReader reader = new MultiReader(
                    readerList.toArray(new IndexReader[readerList.size()]),
                    false);
            // do search
            final IndexSearcher searcher = new IndexSearcher(reader);

            final TopDocs docs = searcher.search(buildQueryForField(fieldName, query, indexingSystem.getAnalyzer()), 10);

            final ScoreDoc[] scoreDocs = docs.scoreDocs;

            // convert to UID for each doc
            for (ScoreDoc scoreDoc : scoreDocs) {
                final int docid = scoreDoc.doc;
                final Document doc = searcher.doc(docid);
                users.add(UserDataIndexable.makeUserDAOFromDocument(doc));
                final SubReaderInfo<BlahguaFilterIndexReader> readerInfo = subReaderAccessor
                        .getSubReaderInfo(docid);

//				long uid = (long) ((ZoieIndexReader<BlahguaFilterIndexReader>) readerInfo.subreader.getInnerReader()).getUID(readerInfo.subdocid);
//				System.out.println(uid);
            }
            return users;

        } catch (Exception e) {
            throw new SystemErrorException("user indexer problem during search", e, ErrorCodes.SERVER_INDEXING_ERROR);
        } finally {
            if (readerList != null) {
                indexingSystem.returnIndexReaders(readerList);
            }
        }
    }

    private Query buildQueryForField(String fieldName, String query, Analyzer analyzer)
            throws ParseException {
        return new QueryParser(Version.LUCENE_35, fieldName, analyzer).parse(query);
    }

    private void createZoieSystem() {
        if (!doIndex()) {
            return;
        }
        final ZoieIndexableInterpreter<UserDAO> interpreter = new UserDataIndexableInterpreter();

        final IndexReaderDecorator<BlahguaFilterIndexReader> decorator = new BlahguaIndexReaderDecorator();

        final ZoieConfig config = makeIndexConfiguration();

        this.indexingSystem = new ZoieSystem<BlahguaFilterIndexReader, UserDAO>(new DefaultDirectoryManager(
                indexDir), interpreter, decorator, config);

        indexingSystem.start(); // ready to accept indexing events
    }

    // TODO this can be configured via Spring, but will be factored out to its own service so don't bother yet
    private ZoieConfig makeIndexConfiguration() {
        final ZoieConfig config = new ZoieConfig();
        config.setAnalyzer(new StandardAnalyzer(Version.LUCENE_35));
        config.setSimilarity(new DefaultSimilarity());
        config.setBatchSize(batchSize);
        config.setBatchDelay(batchDelay);
        config.setRtIndexing(true); // real-time indexing
        return config;
    }

    private StoreManager getStoreManager() {
        return storeManager;
    }

    private SystemManager getSystemManager() {
        return systemManager;
    }

    private MailManager getMailManager() {
        return mailManager;
    }
}


//    /**
//     * <p><b>Not in use.</b></p>
//     * <p> Attempt to recover the user account.</p>
//     *
//     * @param localeId
//     * @param operation    The recovery operation. Valid values are:
//     *                     "e": request recovery by email address. The methodKey must contain an email address.
//     *                     "r": request recovery by recovery code. The methodKey must be null and the recovery code must be provided.
//     * @param methodKey    Required if operation is "e": this should be the email address.
//     * @param recoveryCode Required if operation is "r": this should be the recovery code
//     * @see #recoverUserNew(main.java.com.eweware.service.base.i18n.LocaleId, javax.servlet.http.HttpServletRequest, String)
//     */
//    public UserProfilePayload recoverUser(LocaleId localeId, String operation, String methodKey, String recoveryCode) throws InvalidRequestException, StateConflictException, SystemErrorException {
//        if (operation == null) {
//            throw new InvalidRequestException("missing recovery operation", ErrorCodes.MISSING_QUERY_PARAMETER);
//        }
//
//        final boolean isEmailRecovery = operation.equals("e");
//        final boolean isRecoveryCodeRecovery = operation.equals("r");
//
//        if (isEmailRecovery && methodKey == null) {
//            throw new InvalidRequestException("missing method key (e.g., email address) parameter", ErrorCodes.MISSING_QUERY_PARAMETER);
//        } else if (isRecoveryCodeRecovery && recoveryCode == null) {
//            throw new InvalidRequestException("missing recovery code parameter", ErrorCodes.MISSING_QUERY_PARAMETER);
//        } else if (!isEmailRecovery && !isRecoveryCodeRecovery) {
//            throw new InvalidRequestException("invalid recovery request", ErrorCodes.INVALID_INPUT);
//        }
//
//        final UserProfileDAO profileDAO = storeManager.createUserProfile();
//        if (isRecoveryCodeRecovery) {
//            profileDAO.setRecoveryCode(recoveryCode);
//            final UserProfileDAO dao = (UserProfileDAO) profileDAO._findByCompositeId(new String[]{UserProfileDAO.ID, UserProfileDAO.USER_PROFILE_RECOVER_CODE_SET_METHOD}, UserProfileDAO.USER_PROFILE_RECOVERY_CODE);
//            if (dao == null) {
//                throw new StateConflictException("cannot recover user with given validation code", ErrorCodes.REQUEST_NOT_GRANTED);
//            }
//            if (dao.getRecoverySetMethod() == null || !dao.getRecoverySetMethod().equals(USER_SET_RECOVERY_CODE_METHOD)) { // delete one-use validation code if set by anyone but user
//                dao.setRecoveryCode(null);
//                dao._updateByPrimaryId(DAOUpdateType.ABSOLUTE_UPDATE);
//            }
//            return new UserProfilePayload(dao); // contains id
//        } else if (isEmailRecovery) {
//            profileDAO.setEmailAddress(methodKey);
//            final UserProfileDAO dao = (UserProfileDAO) profileDAO._findByCompositeId(new String[]{UserProfileDAO.ID}, UserProfileDAO.USER_PROFILE_EMAIL_ADDRESS);
//            if (dao == null) {
//                throw new StateConflictException("no user with specified methodKey=" + methodKey, ErrorCodes.NOT_FOUND_USER_ID);
//            }
//            recoveryCode = systemManager.makeShortRandomCode();
//            dao.setRecoveryCode(recoveryCode);
//            dao._updateByPrimaryId(DAOUpdateType.ABSOLUTE_UPDATE);
//
//            sendRecoveryCodeEmail(methodKey, recoveryCode);
//
//            // return nothing
//        }
//        return null;
//    }
//
//    /**
//     * <p>No longer used. (Part of alpha validation method.)</p>
//     * <p><b>Not in use.</b></p>
//     * @param emailAddress
//     * @param recoveryCode
//     * @throws InvalidRequestException
//     * @throws SystemErrorException
//     */
//    private void sendRecoveryCodeEmail(String emailAddress, final String recoveryCode) throws InvalidRequestException, SystemErrorException {
//        if (!EmailUserValidationMethod.emailPattern.matcher(emailAddress).matches()) {
//            throw new InvalidRequestException("Invalid syntax for email address=" + emailAddress, ErrorCodes.INVALID_INPUT);
//        } else {
//            try {  // won't error if MailManager has not started
//                MailManager.getInstance().send(emailAddress, makeRecoverySubject(emailAddress), makeRecoveryBody(recoveryCode));
//            } catch (Exception e) {
//                throw new SystemErrorException("Couldn't send recovery code email to user at " + emailAddress, e, ErrorCodes.VALIDATION_EMAIL_NOT_SENT_DUE_TO_MAIL_SYSTEM_ERROR);
//            }
//        }
//    }
//
//    /**
//     * <p><b>Not in use.</b></p>
//     */
//    private String makeRecoveryBody(String recoveryCode) {
//        final StringBuilder b = new StringBuilder("Dear Blahgua User,\n\nYou have asked for a recovery code for your account and here it is.\n\n");
//        b.append("CODE: " + recoveryCode);
//        b.append("\n\nYou can recover your account by following these methods:\n");
//        b.append("\nMethod 1: click on the following URL to recover your account in this machine: ");
//        b.append("http://app.blahgua.com/recovery.aspx?code=");
//        b.append(recoveryCode);
//        b.append("\n\nMethod 2: Follow these steps:\n\n1. Open Blahgua in any device or browser.");
//        b.append("\n2. Go to the Account Info section.");
//        b.append("\n3. Click on Account Recovery if it is not already opened.");
//        b.append("\n4. Enter the recovery code in the text box labeled 'Recovery Token'.");
//        b.append("\n5. Click on the Recover Account button.");
//        b.append("\n\nAfter step 5, you should have recovered your account in the device or browser.");
//        b.append("\n\nThanks for using Blahgua.");
//        return b.toString();
//    }
//
//    private String makeRecoverySubject(String emailAddress) {
//        return "Blahgua Recovery Code";
//    }


//    /**
//     * @param localeId
//     * @param start         Optional start count or null if there is no paging
//     * @param count         Optional max number of users to return
//     * @param sortFieldName
//     * @return Object  Returns list of users
//     * @throws main.java.com.eweware.service.base.error.SystemErrorException
//     *
//     */
//    public List<UserPayload> getUsers(LocaleId localeId, Integer start, Integer count, String sortFieldName) throws SystemErrorException {
//        count = ensureCount(count);
//        final UserDAO dao = storeManager.createUser();
//        final List<? extends BaseDAO> userDAOs = dao._findMany(start, count, sortFieldName);
//        final List<UserPayload> users = new ArrayList<UserPayload>(userDAOs.size());
//        for (BaseDAO item : userDAOs) {
//            users.add(new UserPayload(item));
//        }
//        return users;
//    }


//    /**
//     * <p><b>Not in use.</b></p>
//     * A user entered a validation code in a client. Looks for the
//     * code in a usergroup association and, if it is there, advances
//     * the user to the active (A) state. The code is deleted from
//     * the association to prevent it from being re-user by user or others.
//     *
//     * @param localeId
//     * @param validationCode
//     * @return UserPayload  Returns payload with the userId and the validated groupId.
//     *         If there is no error, the usergroup state becomes A (active).
//     */
//    public void validateUser(LocaleId localeId, String validationCode) throws InvalidRequestException, StateConflictException, SystemErrorException {
//        if (CommonUtilities.isEmptyString(validationCode)) {
//            throw new InvalidRequestException("missing validation code", ErrorCodes.MISSING_VALIDATION_CODE);
//        }
//        final UserGroupDAO searchDAO = getStoreManager().createUserGroup();
//        searchDAO.setValidationCode(validationCode);
//        final UserGroupDAO userGroupDAO = (UserGroupDAO) searchDAO._findByCompositeId(new String[]{UserGroupDAO.STATE, UserGroupDAO.GROUP_ID, UserGroupDAO.USER_ID}, UserGroupDAO.VALIDATION_CODE);
//        if (userGroupDAO == null) {
//            throw new InvalidRequestException("No pending user found for validation code '" + validationCode + "'. The code is incorrect; else it expired.", ErrorCodes.VALIDATION_CODE_INVALID_OR_EXPIRED);
//        }
//        final String state = userGroupDAO.getState();
//        try {
//            final AuthorizedState s = AuthorizedState.valueOf(state);
//            if (s != AuthorizedState.P && s != AuthorizedState.S) {
//                throw new StateConflictException("state=" + state + " for userId=" + userGroupDAO.getUserId() + " groupId=" + userGroupDAO.getGroupId() +
//                        " is neither " + AuthorizedState.P + " nor " + AuthorizedState.S, ErrorCodes.INVALID_STATE_CODE_IS_NEITHER_P_NOR_S);
//            }
//            userGroupDAO.setState(AuthorizedState.A.toString());
//            userGroupDAO.setValidationCode(null); // used up! TODO this doesn't remove the field, but that's what we want! index dropDups is just a kludge to get arond this
//            userGroupDAO._updateByPrimaryId(DAOUpdateType.ABSOLUTE_UPDATE);
//
//            final GroupDAO groupDAO = getStoreManager().createGroup(userGroupDAO.getGroupId());
//            groupDAO.setUserCount(1);
//            groupDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
//
////            final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.USER_TO_GROUP_STATE_CHANGE);
////            tracker.setUserId(userGroupDAO.getUserId());
////            tracker.setGroupId(userGroupDAO.getGroupId());
////            tracker.setState(userGroupDAO.getState());
////            TrackingManager.getInstance().track(LocaleId.en_us, tracker);
//
//        } catch (IllegalArgumentException e) {
//            throw new StateConflictException("invalid state=" + state + " in usergroup id=" + userGroupDAO.getId() + " for userId=" + userGroupDAO.getUserId() + " groupId=" + userGroupDAO.getGroupId(), e,
//                    ErrorCodes.INVALID_STATE_CODE);
//        }
//    }