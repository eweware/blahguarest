package main.java.com.eweware.service.mgr;

import main.java.com.eweware.service.base.CommonUtilities;
import main.java.com.eweware.service.base.date.DateUtils;
import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.impl.mongo.dao.UserDAOImpl;
import main.java.com.eweware.service.user.validation.EmailUserValidationMethod;
import main.java.com.eweware.service.base.mgr.ManagerState;
import main.java.com.eweware.service.user.validation.UserValidationMethod;
import main.java.com.eweware.service.base.payload.*;
import main.java.com.eweware.service.base.store.StoreManager;
import main.java.com.eweware.service.base.store.dao.*;
import main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema;
import main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import main.java.com.eweware.service.base.type.TrackerType;
import main.java.com.eweware.service.search.index.common.BlahguaFilterIndexReader;
import main.java.com.eweware.service.search.index.common.BlahguaIndexReaderDecorator;
import main.java.com.eweware.service.search.index.user.UserDataIndexable;
import main.java.com.eweware.service.search.index.user.UserDataIndexableInterpreter;
import main.java.com.eweware.service.user.validation.Login;
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

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceException;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author rk@post.harvard.edu
 *         <p/>
 *         TODO add monitors for mutating variables or lists with mutating elements
 *         TODO add transaction-level management (rollbacks)
 */
public class UserManager implements ManagerInterface {

    private static UserManager singleton;

    private static final String userSetRecoveryCodeMethod = "u";


    private final boolean debug;
    private final boolean doIndex;
    private StoreManager storeManager;
    private ManagerState state = ManagerState.UNINITIALIZED;
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
            System.out.println("*** User Index: " + this.indexDir.getAbsolutePath()+" ***");
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

    public UserPayload createUser(LocaleId localeId, String username, String password) throws InvalidRequestException, SystemErrorException {

        Login.checkUsername(username);
//        Login.checkPassword(password); TODO add this back when Dave's through testing

        final UserDAO userDAO = storeManager.createUser();
        userDAO.setUsername(username);
        if (userDAO._exists()) {
            throw new InvalidRequestException("user with username=" + username + " already exists", ErrorCodes.ALREADY_EXISTS_USER_WITH_USERNAME);
        }

        userDAO.initToDefaultValues(localeId);

        if (!CommonUtilities.isEmptyString(password)) {
            Login.checkPassword(password);
            try {
                final String[] saltedPassword = Login.createSaltedPassword(password);
                userDAO.setDigest(saltedPassword[0]);
                userDAO.setSalt(saltedPassword[1]);
            } catch (Exception e) {
                throw new SystemErrorException("Failed to handle password", ErrorCodes.SERVER_SEVERE_ERROR);
            }
        }

        userDAO._insert();

        if (doIndex()) {
            addUserToIndex(userDAO);
        }

//        final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.CREATE_USER);
//        tracker.setUserId(userDAO.getId());
//        TrackingManager.getInstance().track(LocaleId.en_us, tracker);

        return new UserPayload(userDAO);
    }

    /**
     * Logs in a user. If the user is authenticated, the session is marked as authenticated
     * for as long as it lasts.
     *
     * @param locale
     * @param username The username
     * @param password The password for authentication
     * @param request The http servlet request
     */
    public void loginUser(LocaleId locale, String username, String password, HttpServletRequest request) throws InvalidRequestException, SystemErrorException, InvalidAuthorizedStateException, ResourceNotFoundException {
        Login.checkUsername(username);
        Login.checkPassword(password);
        final UserDAO userDAO = storeManager.createUser();
        userDAO.setUsername(username);
        final UserDAO user = (UserDAO) userDAO._findByCompositeId(new String[]{UserDAO.DIGEST, UserDAO.SALT}, UserDAO.USERNAME);
        if (user == null) {
            throw new ResourceNotFoundException("No such user", ErrorCodes.UNAUTHORIZED_USER);
        }
        if (Login.authenticate(user.getDigest(), user.getSalt(), password)) {
            Login.markAuthenticated(request.getSession(true), true);
        } else {
            Login.markAuthenticated(request.getSession(true), false);
            throw new InvalidAuthorizedStateException("User not authorized", ErrorCodes.UNAUTHORIZED_USER);
        }
    }

    /**
     * Creates or updates the user profile.
     *
     * @param localeId
     * @param profile    The profile to set or change
     * @param createOnly If true, we are creating the profile.
     * @return UserProfilePayload  Returns the profile with its current settings if createOnly is true; else
     *         returns null (since PUT operations don't return the payload)
     * @throws InvalidRequestException
     * @throws SystemErrorException
     * @throws StateConflictException
     */
    public UserProfilePayload createOrUpdateUserProfile(LocaleId localeId, UserProfilePayload profile, boolean createOnly) throws InvalidRequestException, SystemErrorException, StateConflictException {
        if (profile == null) {
            throw new InvalidRequestException("missing profile payload", profile, ErrorCodes.MISSING_INPUT_PAYLOAD);
        }
        if (profile.getUserType() != null) {
            throw new InvalidRequestException("user type cannot be changed", profile, ErrorCodes.INVALID_UPDATE);
        }
        final String userId = profile.getId();
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("missing user id", profile, ErrorCodes.MISSING_USER_ID);
        }

        UserProfileDAO userProfileDAO = storeManager.createUserProfile(userId);
        final boolean update = userProfileDAO._exists();
        if (update && createOnly) {
            throw new StateConflictException("profile already exists for userId=" + userId, profile, ErrorCodes.ALREADY_EXISTS_USER_PROFILE);
        }
        if (createOnly) {
            userProfileDAO.initToDefaultValues(LocaleId.en_us); // userId will not be overwritten
        }
        userProfileDAO.addFromMap(profile, true);

        if (profile.getRecoveryCode() != null) {
            userProfileDAO.setRecoverySetMethod(userSetRecoveryCodeMethod);
        }

        try {
            if (update) {
                userProfileDAO._updateByPrimaryId(DAOUpdateType.ABSOLUTE_UPDATE);
            } else {
                userProfileDAO._insert();
            }
        } catch (DuplicateKeyException e) {
            throw new StateConflictException("duplicate key: " + e.getMessage(), ErrorCodes.DUPLICATE_KEY);
        }
        return createOnly ? new UserProfilePayload(userProfileDAO.toMap()) : null; // nothing returned on an update
    }

    /**
     * Attempt to recover the user account
     *
     * @param localeId
     * @param operation      The recovery operation. Valid values are:
     *                       "e": request recovery by email address. The methodKey must contain an email address.
     *                       "r": request recovery by recovery code. The methodKey must be null and the recovery code must be provided.
     * @param methodKey      Required if operation is "e": this should be the email address.
     * @param recoveryCode  Required if operation is "r": this should be the recovery code
     */
    public UserProfilePayload recoverUser(LocaleId localeId, String operation, String methodKey, String recoveryCode) throws InvalidRequestException, StateConflictException, SystemErrorException {
        if (operation == null) {
            throw new InvalidRequestException("missing recovery operation", ErrorCodes.MISSING_QUERY_PARAMETER);
        }

        final boolean isEmailRecovery = operation.equals("e");
        final boolean isRecoveryCodeRecovery = operation.equals("r");

        if (isEmailRecovery && methodKey == null) {
            throw new InvalidRequestException("missing method key (e.g., email address) parameter", ErrorCodes.MISSING_QUERY_PARAMETER);
        } else if (isRecoveryCodeRecovery && recoveryCode == null) {
            throw new InvalidRequestException("missing recovery code parameter", ErrorCodes.MISSING_QUERY_PARAMETER);
        } else if (!isEmailRecovery && !isRecoveryCodeRecovery) {
            throw new InvalidRequestException("invalid recovery request", ErrorCodes.INVALID_INPUT);
        }

        final UserProfileDAO profileDAO = storeManager.createUserProfile();
        if (isRecoveryCodeRecovery) {
            profileDAO.setRecoveryCode(recoveryCode);
            final UserProfileDAO dao = (UserProfileDAO) profileDAO._findByCompositeId(new String[]{UserProfileDAO.ID, UserProfileDAO.USER_PROFILE_RECOVER_CODE_SET_METHOD}, UserProfileDAO.USER_PROFILE_RECOVERY_CODE);
            if (dao == null) {
                throw new StateConflictException("cannot recover user with given validation code", ErrorCodes.REQUEST_NOT_GRANTED);
            }
            if (dao.getRecoverySetMethod() == null || !dao.getRecoverySetMethod().equals(userSetRecoveryCodeMethod)) { // delete one-use validation code if set by anyone but user
                dao.setRecoveryCode(null);
                dao._updateByPrimaryId(DAOUpdateType.ABSOLUTE_UPDATE);
            }
            return new UserProfilePayload(dao); // contains id
        } else if (isEmailRecovery) {
            profileDAO.setEmailAddress(methodKey);
            final UserProfileDAO dao = (UserProfileDAO) profileDAO._findByCompositeId(new String[]{UserProfileDAO.ID}, UserProfileDAO.USER_PROFILE_EMAIL_ADDRESS);
            if (dao == null) {
                throw new StateConflictException("no user with specified methodKey=" + methodKey, ErrorCodes.NOT_FOUND_USER_ID);
            }
            recoveryCode = SystemManager.getInstance().makeShortRandomCode();
            dao.setRecoveryCode(recoveryCode);
            dao._updateByPrimaryId(DAOUpdateType.ABSOLUTE_UPDATE);

            sendRecoveryCodeEmail(methodKey, recoveryCode);

            // return nothing
        }
        return null;
    }

    private void sendRecoveryCodeEmail(String emailAddress, final String recoveryCode) throws InvalidRequestException, SystemErrorException {
        if (!EmailUserValidationMethod.emailPattern.matcher(emailAddress).matches()) {
            throw new InvalidRequestException("Invalid syntax for email address=" + emailAddress, ErrorCodes.INVALID_INPUT);
        } else {
            // TODO definitely queue email call and config reply email
            try {  // won't error if MailManager has not started
                MailManager.getInstance().send(emailAddress, makeRecoverySubject(emailAddress), makeRecoveryBody(recoveryCode));
            } catch (Exception e) {
                throw new SystemErrorException("Couldn't send recovery code email to user at " + emailAddress, e, ErrorCodes.VALIDATION_EMAIL_NOT_SENT_DUE_TO_MAIL_SYSTEM_ERROR);
            }
        }
    }

    private String makeRecoveryBody(String recoveryCode) {
        final StringBuilder b = new StringBuilder("Dear Blahgua User,\n\nYou have asked for a recovery code for your account and here it is.\n\n");
        b.append("CODE: " + recoveryCode);
        b.append("\n\nYou can recover your account by following these methods:\n");
        b.append("\nMethod 1: click on the following URL to recover your account in this machine: ");
        b.append("http://app.blahgua.com/recovery.aspx?code=");
        b.append(recoveryCode);
        b.append("\n\nMethod 2: Follow these steps:\n\n1. Open Blahgua in any device or browser.");
        b.append("\n2. Go to the Account Info section.");
        b.append("\n3. Click on Account Recovery if it is not already opened.");
        b.append("\n4. Enter the recovery code in the text box labeled 'Recovery Token'.");
        b.append("\n5. Click on the Recover Account button.");
        b.append("\n\nAfter step 5, you should have recovered your account in the device or browser.");
        b.append("\n\nThanks for using Blahgua.");
        return b.toString();
    }

    private String makeRecoverySubject(String emailAddress) {
        return "Blahgua Recovery Code";
    }

    /**
     * Initiates registration of a user into a group.
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
        if (CommonUtilities.isEmptyString(groupId)) {
            throw new InvalidRequestException("groupId required to join user to a group", ErrorCodes.MISSING_GROUP_ID);
        }
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("userId required to join user to a group", ErrorCodes.MISSING_USER_ID);
        }
        if (!storeManager.createUser(userId)._exists()) {
            throw new InvalidRequestException("userId=" + userId + " does not exist", ErrorCodes.NOT_FOUND_USER_ID);
        }
        final GroupDAO groupDAO = (GroupDAO) storeManager.createGroup(groupId)._findByPrimaryId(GroupDAO.DISPLAY_NAME, GroupDAO.USER_VALIDATION_METHOD, GroupDAO.USER_VALIDATION_PARAMETERS);
        if (groupDAO == null) {
            throw new ResourceNotFoundException("no group exists with groupId=" + groupId, ErrorCodes.NOT_FOUND_GROUP_ID);
        }

        final UserValidationMethod vmeth = UserValidationMethod.getValidationMethod(groupDAO.getValidationMethod());
        if (vmeth == null) {
            throw new InvalidRequestException("group validation method is obsolete or invalid; groupId=" + groupId + " method='" + groupDAO.getValidationMethod() + "'");
        }
        vmeth.validateKey(validationKey, groupDAO.getValidationParameters());

        if (storeManager.createUserGroup(userId, groupId)._exists()) {
            throw new InvalidRequestException("userId=" + userId + " has already joined groupId=" + groupId, ErrorCodes.USER_ALREADY_JOINED_GROUP);
        }

        final AuthorizedState defaultState = vmeth.getDefaultAuthorizationState();
        final String validationCode = vmeth.startValidation(userId, groupId, groupDAO.getDisplayName(), validationKey);

        // Add user to the group with pending state, stashing validation code
        updateUserStatus(LocaleId.en_us, userId, groupId, defaultState.toString(), validationCode);

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
     * A user entered a validation code in a client. Looks for the
     * code in a usergroup association and, if it is there, advances
     * the user to the active (A) state. The code is deleted from
     * the association to prevent it from being re-user by user or others.
     *
     * @param localeId
     * @param validationCode
     * @return UserPayload  Returns payload with the userId and the validated groupId.
     *         If there is no error, the usergroup state becomes A (active).
     */
    public void validateUser(LocaleId localeId, String validationCode) throws InvalidRequestException, StateConflictException, SystemErrorException {
        if (CommonUtilities.isEmptyString(validationCode)) {
            throw new InvalidRequestException("missing validation code", ErrorCodes.MISSING_VALIDATION_CODE);
        }
        final UserGroupDAO searchDAO = storeManager.createUserGroup();
        searchDAO.setValidationCode(validationCode);
        final UserGroupDAO userGroupDAO = (UserGroupDAO) searchDAO._findByCompositeId(new String[]{UserGroupDAO.STATE, UserGroupDAO.GROUP_ID, UserGroupDAO.USER_ID}, UserGroupDAO.VALIDATION_CODE);
        if (userGroupDAO == null) {
            throw new InvalidRequestException("No pending user found for validation code '" + validationCode + "'. The code is incorrect; else it expired.", ErrorCodes.VALIDATION_CODE_INVALID_OR_EXPIRED);
        }
        final String state = userGroupDAO.getState();
        try {
            final AuthorizedState s = AuthorizedState.valueOf(state);
            if (s != AuthorizedState.P && s != AuthorizedState.S) {
                throw new StateConflictException("state=" + state + " for userId=" + userGroupDAO.getUserId() + " groupId=" + userGroupDAO.getGroupId() +
                        " is neither " + AuthorizedState.P + " nor " + AuthorizedState.S, ErrorCodes.INVALID_STATE_CODE_IS_NEITHER_P_NOR_S);
            }
            userGroupDAO.setState(AuthorizedState.A.toString());
            userGroupDAO.setValidationCode(null); // used up! TODO this doesn't remove the field, but that's what we want! index dropDups is just a kludge to get arond this
            userGroupDAO._updateByPrimaryId(DAOUpdateType.ABSOLUTE_UPDATE);

            final GroupDAO groupDAO = storeManager.createGroup(userGroupDAO.getGroupId());
            groupDAO.setUserCount(1);
            groupDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

//            final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.USER_TO_GROUP_STATE_CHANGE);
//            tracker.setUserId(userGroupDAO.getUserId());
//            tracker.setGroupId(userGroupDAO.getGroupId());
//            tracker.setState(userGroupDAO.getState());
//            TrackingManager.getInstance().track(LocaleId.en_us, tracker);

        } catch (IllegalArgumentException e) {
            throw new StateConflictException("invalid state=" + state + " in usergroup id=" + userGroupDAO.getId() + " for userId=" + userGroupDAO.getUserId() + " groupId=" + userGroupDAO.getGroupId(), e,
                    ErrorCodes.INVALID_STATE_CODE);
        }
    }

    /**
     * Updates user fields.
     *
     * @param localeId
     * @param userId   The user's id
     * @param updates  The fields to update.
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws InvalidRequestException
     */
    public void updateUser(LocaleId localeId, String userId, UserPayload updates) throws InvalidRequestException, ResourceNotFoundException, SystemErrorException {
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("userId is required", ErrorCodes.MISSING_USER_ID);
        }
        final UserDAO dao = storeManager.createUser(userId);
        if (!dao._exists()) { // TODO optimization: catch some ObjectNotExistsException from _update call below
            throw new ResourceNotFoundException("no user exists with userId ", userId, ErrorCodes.NOT_FOUND_USER_ID);
        }
        final String username = updates.getUsername();
        if (username != null) {
            dao.setUsername(username);
        }
        dao._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

        addUserToIndex(dao);
    }


    /**
     *
     * @param localeId
     * @param start    Optional start count or null if there is no paging
     * @param count    Optional max number of users to return
     * @param sortFieldName
     * @return Object  Returns list of users
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    public List<UserPayload> getUsers(LocaleId localeId, Integer start, Integer count, String sortFieldName) throws SystemErrorException {
        count = ensureCount(count);
        final UserDAO dao = storeManager.createUser();
        final List<? extends BaseDAO> userDAOs = dao._findMany(start, count, sortFieldName);
        final List<UserPayload> users = new ArrayList<UserPayload>(userDAOs.size());
        for (BaseDAO item : userDAOs) {
            users.add(new UserPayload(item));
        }
        return users;
    }

    /**
     * Returns a user's data by user Id
     *
     * @param localeId
     * @param userId   The userId to fetch with
     * @param byUsername  If true, the userId is actually the username
     * @param stats If true, return user stats
     * @param statsStartDate if stats is true, format is yymmdd (e.g., August 27, 2012 is 120827
     * @param statsEndDate   if stats is true, format is yymmdd (e.g., August 27, 2012 is 120827
     * @return
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws InvalidRequestException
     * @throws ResourceNotFoundException
     */
    public UserPayload getUserById(LocaleId localeId, String userId, boolean byUsername, Boolean stats, String statsStartDate, String statsEndDate) throws InvalidRequestException, ResourceNotFoundException, SystemErrorException {
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("invalid user id or username", byUsername ? ErrorCodes.INVALID_USERNAME : ErrorCodes.INVALID_USER_ID);
        }
        UserDAO dao = storeManager.createUser();

        if (byUsername) {dao.setUsername(userId);} else {dao.setId(userId);}

        dao = (UserDAO) (byUsername ? dao._findByCompositeId(null, UserDAO.USERNAME) : dao._findByPrimaryId());
        if (dao == null) {
            throw new ResourceNotFoundException("user with given id or username not found", ErrorCodes.NOT_FOUND_USER_ID);
        }

        if (stats) { // TODO it is an understatement to say that this is inefficient
            fetchAndAddUserTrackers(dao.getId(), statsStartDate, statsEndDate, dao);
        }

        return new UserPayload(dao);
    }

    public UserProfilePayload getUserProfileById(LocaleId localeId, String userId) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException {
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("missing user id", userId, ErrorCodes.MISSING_USER_ID);
        }
        final UserProfileDAO profileDAO = (UserProfileDAO) storeManager.createUserProfile(userId)._findByPrimaryId();
        if (profileDAO == null) {
            throw new ResourceNotFoundException("no user profile id=" + userId, ErrorCodes.NOT_FOUND_USER_PROFILE);
        }
        return new UserProfilePayload(profileDAO.toMap());
    }

    public UserProfileSchema getUserProfileSchema(LocaleId localeId) {
        return UserProfileSchema.getSchema(localeId);
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
            final UserTrackerDAO userTrackerDAO = (UserTrackerDAO) storeManager.createUserTracker(trackerId)._findByPrimaryId();
            if (userTrackerDAO != null) {
                trackers = new ArrayList<UserTrackerDAO>(1);
                trackers.add(userTrackerDAO);
            }
        } else { // range search
            final UserTrackerDAO userTrackerDAO = (UserTrackerDAO) storeManager.createUserTracker();
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
     * @throws ResourceNotFoundException    Thrown when the user can't be found
     */
    public void checkUserById(String userId, Object entity) throws ResourceNotFoundException, InvalidRequestException, SystemErrorException {
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("missing userId", ErrorCodes.MISSING_USER_ID);
        }
        if (!storeManager.createUser(userId)._exists()) {
            throw new ResourceNotFoundException("not found userId=" + userId, entity, ErrorCodes.NOT_FOUND_USER_ID);
        }
    }

    public UserGroupPayload getUserGroup(LocaleId localeId, String userId, String groupId) throws InvalidRequestException, ResourceNotFoundException, SystemErrorException {
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("missing userId", ErrorCodes.MISSING_USER_ID);
        }
        if (CommonUtilities.isEmptyString(groupId)) {
            throw new InvalidRequestException("missing groupId", ErrorCodes.MISSING_GROUP_ID);
        }
        final UserGroupDAO userGroupDAO = (UserGroupDAO) storeManager.createUserGroup(userId, groupId)._findByCompositeId(new String[]{UserGroupDAO.STATE}, UserGroupDAO.USER_ID, UserGroupDAO.GROUP_ID);
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
        final UserGroupDAO searchUserGroupDAO = storeManager.createUserGroup();
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
            final GroupDAO groupDAO = (GroupDAO) storeManager.createGroup(ug.getGroupId())._findByPrimaryId(GroupDAO.DISPLAY_NAME, GroupDAO.DESCRIPTION);
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
     * @param userId         The user id
     * @param groupId        The group id
     * @param newState       If AuthorizedState.P.getDescription, the user will be joined to the group in a pending state.
     *                       If AuthorizedState.A.getDescription, the user will be activated in the group.
     *                       If AuthorizedState.S.getDescription, the user will be suspended in the group.
     *                       If AuthorizedState.DT.getDescription, the user/group association will be deleted from the database.
     * @param validationCode Validation code for user (needed when user is in P (pending) or S (suspended) state.
     *                       If not null, this is simply inserted into the DB for future reference.
     * @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws StateConflictException
     */
    public void updateUserStatus(LocaleId localeId, String userId, String groupId, String newState, String validationCode) throws InvalidRequestException, StateConflictException, ResourceNotFoundException, SystemErrorException {
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
        if (!storeManager.createUser(userId)._exists()) {
            throw new ResourceNotFoundException("not found user with userId=" + userId, ErrorCodes.NOT_FOUND_USER_ID);
        }
        final GroupDAO groupDAO = (GroupDAO) storeManager.createGroup(groupId)._findByPrimaryId(GroupDAO.STATE);
        if (groupDAO == null) {
            throw new ResourceNotFoundException("not found group with groupId=" + groupId, ErrorCodes.NOT_FOUND_GROUP_ID);
        }
        if ((join || activate) && !(groupDAO.getState().equals(AuthorizedState.A.toString()))) {
            throw new StateConflictException("groupId " + groupId + " is inactive. A group must be active for a user to join or become active in it", "groupId=" + groupId,
                    ErrorCodes.INVALID_STATE_USER_CANNOT_JOIN_INACTIVE_GROUP);
        }
        // TODO 1. use composite _find instead, and 2. assuming here that DB is fully consistent: if not, also check the groupId:
        final UserGroupDAO found = (UserGroupDAO) storeManager.createUserGroup(userId, groupId)._findByCompositeId(null, UserGroupDAO.USER_ID, UserGroupDAO.GROUP_ID);
        if (found != null) { // user in group
            final String userGroupId = found.getId();
            final Object state = found.getState();
            if (join) { // but user already joined
                throw new StateConflictException("userId=" + userId + " already joined groupId=" + groupId, ErrorCodes.USER_ALREADY_JOINED_GROUP);
            } else if (activate) {
                if (state.equals(AuthorizedState.P.toString()) || state.equals(AuthorizedState.S.toString())) {

                    // Mark user as active
                    final UserGroupDAO userGroup = storeManager.createUserGroup(userGroupId);
                    userGroup.setState(AuthorizedState.A.toString());
                    userGroup._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
                } else {
                    throw new StateConflictException("user cannot be activated when its state is '" + state + "'; userId " + userId + " groupId " + groupId, ErrorCodes.USER_CANNOT_BE_ACTIVATED_WHEN_STATE_IS_NOT_P_OR_S);
                }
            } else if (suspend) {
                if (state.equals(AuthorizedState.A.toString())) {

                    // Mark user as suspended
                    final UserGroupDAO userGroup = storeManager.createUserGroup(userGroupId);
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
                storeManager.createUserGroup(userGroupId)._deleteByPrimaryId();

//                final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.USER_TO_GROUP_STATE_CHANGE);
//                tracker.setUserId(userId);
//                tracker.setGroupId(groupId);
//                tracker.setState(AuthorizedState.D.toString());
//                TrackingManager.getInstance().track(LocaleId.en_us, tracker);
            }
        } else { // no user/group obj
            if (join || activate) {
                final UserGroupDAO userGroup = storeManager.createUserGroup();
                userGroup.initToDefaultValues(localeId);
                userGroup.setUserId(userId);
                userGroup.setGroupId(groupId);
                if (validationCode != null) {
                    userGroup.setValidationCode(validationCode);
                }
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

    // Indexer
    // zoie index stuff to be integrated later...

    public void InitializeUserSearch() {
        if (doIndex()) {
            createZoieSystem();
        }
    }

    private void addUserToIndex(UserDAO user) throws SystemErrorException {
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
            users.add(storeManager.createUser());
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
}
