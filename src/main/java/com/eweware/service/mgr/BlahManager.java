package main.java.com.eweware.service.mgr;

import main.java.com.eweware.service.base.CommonUtilities;
import main.java.com.eweware.service.base.cache.Inbox;
import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.mgr.ManagerInterface;
import main.java.com.eweware.service.base.mgr.ManagerState;
import main.java.com.eweware.service.base.payload.*;
import main.java.com.eweware.service.base.store.StoreManager;
import main.java.com.eweware.service.base.store.dao.*;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.BlahSchema;
import main.java.com.eweware.service.base.store.dao.schema.CommentSchema;
import main.java.com.eweware.service.base.store.dao.schema.SchemaSpec;
import main.java.com.eweware.service.base.store.dao.tracker.TrackerOperation;
import main.java.com.eweware.service.base.store.dao.type.BlahTypeCategoryType;
import main.java.com.eweware.service.base.store.dao.type.DAOUpdateType;
import main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import main.java.com.eweware.service.base.type.TrackerType;
import main.java.com.eweware.service.mgr.aux.InboxHandler;
import main.java.com.eweware.service.mgr.type.PredictionExpirationType;
import main.java.com.eweware.service.mgr.type.PredictionVote;
import main.java.com.eweware.service.rest.session.BlahguaSession;
import main.java.com.eweware.service.search.index.blah.BlahCommentDataIndexable;
import main.java.com.eweware.service.search.index.blah.BlahCommentDataIndexableInterpreter;
import main.java.com.eweware.service.search.index.blah.BlahDataIndexable;
import main.java.com.eweware.service.search.index.blah.BlahDataIndexableInterpreter;
import main.java.com.eweware.service.search.index.common.BlahguaFilterIndexReader;
import main.java.com.eweware.service.search.index.common.BlahguaIndexReaderDecorator;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
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
import proj.zoie.impl.indexing.ZoieConfig;
import proj.zoie.impl.indexing.ZoieSystem;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 *         <p/>
 *         TODO add monitors for mutating variables or lists with mutating elements
 *         TODO add transaction-level management (rollbacks)
 */
public final class BlahManager implements ManagerInterface {

    private static final Logger logger = Logger.getLogger("BlahManager");

    private static final long TEN_MINUTES_BLAH_TYPE_CACHE_REFRESH_IN_MILLIS = 1000l * 60 * 10;
    private static final long THIRTY_MINUTES_IN_MILLIS = 1000l * 60 * 30;
    private static final String EMPTY_STRING = "";

    private boolean debug;
    private final boolean doIndex;
    private final File blahIndexDir;
    private final File commentIndexDir;
    private final int batchSize;
    private final long batchDelay;
    private final int maxOpensOrViewsPerUpdate;
    private final InboxHandler inboxHandler;

    /**
     * Maps an existing blah type id to its data
     */
    private Map<String, BlahTypeEntry> blahTypeIdToBlahTypeEntryMap = new HashMap<String, BlahTypeEntry>();
    private Object blahTypeIdToBlahTypeEntryMapLock = new Object(); // locks blahTypeIdToBlahTypeEntryMap
    private long lastTimeBlahTypesCached = System.currentTimeMillis() - TEN_MINUTES_BLAH_TYPE_CACHE_REFRESH_IN_MILLIS - 1;

    private ZoieSystem<BlahguaFilterIndexReader, BlahDAO> blahIndexingSystem;
    private ZoieSystem<BlahguaFilterIndexReader, CommentDAO> commentIndexingSystem;
    private ManagerState status;
    public static BlahManager singleton;
    private StoreManager storeManager;
    private TrackingManager trackingManager;
    private TrackingMgr trackingMgr;
    private UserManager userManager;
    private GroupManager groupManager;
    private final Integer returnedObjectLimit;

    public static BlahManager getInstance() throws SystemErrorException {
        if (BlahManager.singleton == null) {
            throw new SystemErrorException("blah manager not initialized", ErrorCodes.SERVER_NOT_INITIALIZED);
        }
        return BlahManager.singleton;
    }

    public BlahManager(Boolean debug, Boolean doIndex, String blahIndexDir, String commentIndexDir,
                       String batchSize, String batchDelay, int maxOpensOrViewsPerUpdate,
                       Integer returnedObjectLimit) {
        this.debug = (debug == Boolean.TRUE);
        this.doIndex = (doIndex == Boolean.TRUE);
        this.blahIndexDir = new File(blahIndexDir);
        this.commentIndexDir = new File(commentIndexDir);
        this.batchSize = Integer.parseInt(batchSize);
        this.batchDelay = Long.parseLong(batchDelay);
        this.maxOpensOrViewsPerUpdate = maxOpensOrViewsPerUpdate;
        this.returnedObjectLimit = returnedObjectLimit;
        try {
            this.inboxHandler = new InboxHandler();
        } catch (SystemErrorException e) {
            throw new WebServiceException(e);
        }
        BlahManager.singleton = this;
        this.status = ManagerState.INITIALIZED;

        if (doIndex) {
            System.out.println("*** BlahManager Initializing ***");
            ensureIndex(this.blahIndexDir);
            ensureIndex(this.commentIndexDir);
            System.out.println("*** BlahManager Blah Index: " + this.blahIndexDir.getAbsolutePath() + " ***");
            System.out.println("*** BlahManager Comment Index: " + this.commentIndexDir.getAbsolutePath() + " ***");
        } else {
            System.out.println("*** BlahManager search disabled ***");
        }

        System.out.println("*** BlahManager initialized ***");
    }

    public boolean doIndex() {
        return doIndex;
    }

    private void ensureIndex(File indexDir) {
        final File searchDir = indexDir.getParentFile();
        if (!searchDir.exists()) {
            System.out.println("Search directory '" + searchDir + "' doesn't exist. Creating it...");
            try {
                searchDir.mkdirs();
            } catch (Exception e) {    // fall through
            }
            if (!searchDir.exists()) {
                throw new WebServiceException("Couldn't create search directory index '" + searchDir + "'. UserManager aborting.");
            }
        }
    }

    public void start() {
        try {
            storeManager = MongoStoreManager.getInstance();
            trackingManager = TrackingManager.getInstance();
            userManager = UserManager.getInstance();
            groupManager = GroupManager.getInstance();
            trackingMgr = TrackingMgr.getInstance();

            if (doIndex()) {
                initializeBlahIndex();
            }

            refreshBlahTypesCache();

            this.status = ManagerState.STARTED;
            System.out.println("*** BlahManager started ***");

        } catch (Exception e) {
            throw new WebServiceException(e);
        }
    }

    public void shutdown() {
        if (doIndex()) {
            blahIndexingSystem.shutdown();
            commentIndexingSystem.shutdown();
        }
        this.status = ManagerState.SHUTDOWN;
        System.out.println("*** BlahManager shut down ***");
    }

    public ManagerState getState() {
        return this.status;
    }


    /**
     * Creates a new blah authored by user.
     *
     * @param localeId
     * @param authorId
     * @param entity   The request object  @return BlahPayload A blah payload including the new blah id
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws InvalidRequestException
     * @throws ResourceNotFoundException
     */
    public BlahPayload createBlah(LocaleId localeId, String authorId, BlahPayload entity) throws SystemErrorException, InvalidRequestException, ResourceNotFoundException, StateConflictException {

        // Check required fields
        if (CommonUtilities.isEmptyString(authorId)) {
            throw new InvalidRequestException("missing field authorId=" + authorId, entity, ErrorCodes.MISSING_USER_ID);
        }

        String text = entity.getText();
        String body = entity.getBody();
        final boolean hasBody = !CommonUtilities.isEmptyString(body);
        if (CommonUtilities.isEmptyString(text)) {
            if (!hasBody) {
            throw new InvalidRequestException("Blah without text line must have body" + text, entity, ErrorCodes.MISSING_TEXT_OR_BODY);
            }
            text = EMPTY_STRING;
        } else {
            text = CommonUtilities.scrapeMarkup(text);
        }
        entity.setText(text);

        if (hasBody) {
            body = CommonUtilities.scrapeMarkup(body);
            entity.setBody(body);
        }

        final String groupId = entity.getGroupId();
        if (CommonUtilities.isEmptyString(groupId)) {
            throw new InvalidRequestException("missing field groupId=" + groupId, ErrorCodes.MISSING_GROUP_ID);
        }

        final String typeId = entity.getTypeId();
        if (!isTypeIdValid(typeId)) {
            throw new InvalidRequestException("invalid blah type id '" + typeId + "'", ErrorCodes.MISSING_BLAH_TYPE_ID);
        }

        // Ensure user is active in group  // TODO authorized groups could be cached in session obj
        ensureUserActiveInGroup(authorId, groupId);

        verifyBadges(entity);

        // Create fresh blah to prevent injection
        final BlahDAO blahDAO = getStoreManager().createBlah();
        blahDAO.initToDefaultValues(localeId);
        blahDAO.addFromMap(entity, true); // removes fields not in schema
        // TODO maybe set fields explicitly instead of trusting request payload's data
        blahDAO.setAuthorId(authorId);

        if (isCategory(typeId, BlahTypeCategoryType.POLL)) {
            addPollData(text, blahDAO);
        } else if (isCategory(typeId, BlahTypeCategoryType.PREDICTION)) {
            addPredictionData(blahDAO, entity.getExpirationDate());
        }
        blahDAO._insert();

        updateGroupBlahCount(groupId, true);

        // Add to inboxes
        inboxHandler.spreadBlah(LocaleId.en_us, blahDAO, groupId);

        // Track it
        trackBlah(authorId, blahDAO);

//        final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.CREATE_BLAH);
//        tracker.setBlahId(blahDAO.getId());
//        tracker.setUserId(authorId);
//        tracker.setBlahAuthorId(authorId);
//        tracker.setGroupId(groupId);
//        trackingManager.track(LocaleId.en_us, tracker);

        if (doIndex()) {
            indexBlah(blahDAO);
        }

        return new BlahPayload(blahDAO);
    }

    private void verifyBadges(BlahPayload entity) throws SystemErrorException, InvalidRequestException {
        final List<String> badgeIds = entity.getBadgeIds();
        if (badgeIds != null && badgeIds.size() > 0) {
            final BadgeDAO badge = storeManager.createBadge();
            for (String badgeId : badgeIds) {
                badge.setId(badgeId);
                if (badge._count() == 0) {
                    throw new InvalidRequestException("badge id '" + badgeId + "' is invalid", ErrorCodes.INVALID_INPUT);
                }
            }
        }
    }

    /**
     * Checks and adds prediction-related data to a blah.
     *
     * @param blahDAO        The dao
     * @param expirationDate The expiration date
     */
    private void addPredictionData(BlahDAO blahDAO, Date expirationDate) throws InvalidRequestException {
        if (expirationDate == null) {
            throw new InvalidRequestException("missing expiration date", ErrorCodes.INVALID_INPUT);
        }
        try {
            final Date date = expirationDate; // main.java.com.eweware.service.base.date.DateUtils.fromISODateTimeToUTC(expirationDate);
            final long limit = System.currentTimeMillis() + THIRTY_MINUTES_IN_MILLIS;
            if (date.getTime() < limit) {
                throw new InvalidRequestException("expiration date must be at least 30 minutes into the future", ErrorCodes.INVALID_INPUT);
            }
            blahDAO.setExpirationDate(date);
//        } catch (java.text.ParseException e) {
//            throw new InvalidRequestException("invalid expiration date", expirationDate, ErrorCodes.INVALID_INPUT);
        } finally {

        }

    }


    private void addPollData(String text, BlahDAO blahDAO) throws InvalidRequestException, SystemErrorException {
        final List<PollOptionTextDAO> pollOptionsText = blahDAO.getPollOptionsText();
        if (pollOptionsText != null && !pollOptionsText.isEmpty()) {
            for (PollOptionTextDAO potdao : pollOptionsText) {     // TODO fix binding issue in commented-out method below
                final String tagLine = (String) potdao.get(PollOptionTextDAO.TAGLINE);
                if (tagLine == null) {
                    throw new InvalidRequestException("missing poll option tagline", ErrorCodes.INVALID_INPUT);
                } else if (tagLine.length() != 0) {
                    potdao.put(PollOptionTextDAO.TAGLINE, CommonUtilities.scrapeMarkup(tagLine));
                }
                final String pollText = (String) potdao.get(PollOptionTextDAO.TEXT);
                if (text != null && text.length() != 0) {
                    potdao.put(PollOptionTextDAO.TEXT, CommonUtilities.scrapeMarkup(pollText));
                }
            }
            int count = pollOptionsText.size();
            blahDAO.setPollOptionCount(count);
            final List<Integer> vcs = new ArrayList<Integer>(count);
            while (count-- > 0) {
                vcs.add(0);
            }
            blahDAO.setPollOptionVotes(vcs);
        }
    }

    /**
     * TODO tracker should maintain this count
     *
     * @param groupId   The group id
     * @param increment True if count should be incremented by one, else decremented by one.
     * @throws SystemErrorException
     */
    private void updateGroupBlahCount(String groupId, boolean increment) throws SystemErrorException {
        final GroupDAO groupDAO = getStoreManager().createGroup(groupId);
        groupDAO.setBlahCount(increment ? 1 : -1);
        groupDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
    }

    /**
     * Creates a tracker object for this blah transaction
     *
     * @param authorId The blah's author id
     * @param blahDAO  The blah
     * @throws SystemErrorException
     * @throws ResourceNotFoundException
     * @throws InvalidRequestException
     */
    private void trackBlah(String authorId, BlahDAO blahDAO) throws SystemErrorException, ResourceNotFoundException, InvalidRequestException {
        final boolean isBlah = true;
        final boolean isNewObject = true;
        final String objectId = blahDAO.getId();
        final String subObjectId = null;
        final boolean voteUp = false;
        final boolean voteDown = false;
        final Integer viewCount = null;
        final Integer openCount = null;
        getTrackingManager().trackObject(TrackerOperation.CREATE_BLAH, authorId, authorId, isBlah, isNewObject, objectId, subObjectId, voteUp, voteDown, null, viewCount, openCount);
    }

    /**
     * Ensures that specified user is active in a group.
     *
     * @param userId  The user id
     * @param groupId The group id
     * @throws SystemErrorException
     * @throws StateConflictException Thrown if the user isn't active in the specified group.
     */
    private void ensureUserActiveInGroup(String userId, String groupId) throws SystemErrorException, StateConflictException {
        UserGroupDAO userGroupDAO = getStoreManager().createUserGroup();
        userGroupDAO.setUserId(userId);
        userGroupDAO.setGroupId(groupId);
        userGroupDAO = (UserGroupDAO) userGroupDAO._findByCompositeId(new String[]{UserGroupDAO.STATE}, UserGroupDAO.USER_ID, UserGroupDAO.GROUP_ID);
        if (userGroupDAO == null || !userGroupDAO.getState().equals(AuthorizedState.A.toString())) {
            throw new StateConflictException("userId=" + userId + " is not active in groupId=" + groupId, ErrorCodes.USER_NOT_JOINED_GROUP);
        }
    }

    /**
     * Ensures that the blah type is valid.
     *
     * @param typeId The blah type id
     * @return True if the blah type is valid
     * @throws InvalidRequestException Thrown if the blah type id is empty or null
     * @throws SystemErrorException
     */
    private boolean isTypeIdValid(String typeId) throws InvalidRequestException, SystemErrorException {
        if (CommonUtilities.isEmptyString(typeId)) {
            throw new InvalidRequestException("missing typeId");
        }
        synchronized (blahTypeIdToBlahTypeEntryMapLock) {
            ensureBlahTypesCached();
            return blahTypeIdToBlahTypeEntryMap.get(typeId) != null;
        }
    }

    public void pollVote(LocaleId localeId, String blahId, String userId, Integer pollOptionIndex) throws InvalidRequestException, SystemErrorException, StateConflictException, ResourceNotFoundException {

        if (blahId == null) {
            throw new InvalidRequestException("request missing blah id", ErrorCodes.MISSING_BLAH_ID);
        }
        if (pollOptionIndex == null || pollOptionIndex < 0 || pollOptionIndex > PollOptionDAOConstants.MAX_POLL_OPTIONS) {
            throw new InvalidRequestException("invalid poll index; maximum is " + PollOptionDAOConstants.MAX_POLL_OPTIONS + " but was=" + pollOptionIndex, ErrorCodes.INVALID_INPUT);
        }
        final BlahDAO blahDAO = getBlahById_unsafe(blahId, BlahDAO.POLL_OPTION_COUNT, BlahDAO.TYPE_ID);
        if (blahDAO == null) {
            throw new InvalidRequestException("blahId '" + blahId + "' doesn't exist", ErrorCodes.NOT_FOUND_BLAH_ID);
        }
        if (!isCategory(blahDAO.getTypeId(), BlahTypeCategoryType.POLL)) {
            throw new InvalidRequestException("Blah id '" + blahId + "' is not a poll category blah", ErrorCodes.INVALID_UPDATE);
        }

        final UserBlahInfoData userBlahInfoData = ensureUserDidNotVoteOnPoll(blahId, userId);

        final Integer noPollCount = -1;
        final Integer pollCount = CommonUtilities.safeGetInteger(blahDAO.getPollOptionCount(), noPollCount);
        if (pollCount == noPollCount) {
            throw new InvalidRequestException("There are no poll options in this blah", ErrorCodes.SERVER_RECOVERABLE_ERROR);
        }
        if (pollOptionIndex >= pollCount) {
            throw new InvalidRequestException("poll index is out of range: it must be less than " + pollCount, ErrorCodes.INVALID_INPUT);
        }
        blahDAO.addPollOptionVote_immediate(pollOptionIndex);

        userBlahInfoData.dao.setPollVoteIndex(pollOptionIndex);
        userBlahInfoData.dao.setPollVoteTimestamp(new Date());
        if (userBlahInfoData.exists) {
            userBlahInfoData.dao._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
        } else {
            userBlahInfoData.dao._insert();
        }

        getTrackingManager().trackObject(TrackerOperation.UPDATE_BLAH, userId, userId, true, false, blahId, null, false, false, pollOptionIndex, null, null);
    }

    public void predictionVote(String userId, String blahId, String preOrPostExpiration, String vote)
            throws SystemErrorException, InvalidRequestException {

        if (userId == null) {
            throw new SystemErrorException("context issue", ErrorCodes.SESSION_ERROR);
        }
        final PredictionExpirationType expirationType = PredictionExpirationType.find(preOrPostExpiration);
        if (expirationType == null) {
            throw new InvalidRequestException("missing or invalid prediction expiration type", ErrorCodes.INVALID_INPUT);
        }
        final boolean preExpirationVote = (expirationType == PredictionExpirationType.PRE_EXPIRATION);
        final PredictionVote predictionVote = PredictionVote.find(vote);
        if (predictionVote == null) {
            throw new InvalidRequestException("missing or invalid vote", ErrorCodes.INVALID_INPUT);
        }
        final BlahDAO blahDAO = (BlahDAO) storeManager.createBlah(blahId)._findByPrimaryId(BlahDAO.EXPIRATION_DATE, BlahDAO.TYPE_ID, BlahDAO.AUTHOR_ID);
        if (blahDAO == null) {
            throw new InvalidRequestException("invalid blah id", ErrorCodes.INVALID_INPUT);
        }
        if (blahDAO.getAuthorId().equals(userId)) {
            throw new InvalidRequestException("cannot vote on own prediction", ErrorCodes.USER_CANNOT_UPDATE_ON_OWN_BLAH);
        }

        if (!isCategory(blahDAO.getTypeId(), BlahTypeCategoryType.PREDICTION)) {
            throw new InvalidRequestException("not a prediction blah", ErrorCodes.INVALID_INPUT);
        }

        final Date expirationDate = blahDAO.getExpirationDate();

        final UserBlahInfoData userBlahInfoData = ensurePredictionConsistent(userId, blahId, preExpirationVote, expirationDate);

        final BlahDAO updateBlahDAO = storeManager.createBlah(blahId);

        switch (predictionVote) {
            case YES:
                if (preExpirationVote) {
                    updateBlahDAO.setPredictionAgreeCount(1);
                } else {
                    updateBlahDAO.setPredictionResultCorrectCount(1);
                }
                break;
            case NO:
                if (preExpirationVote) {
                    updateBlahDAO.setPredictionDisagreeCount(1);
                } else {
                    updateBlahDAO.setPredictionResultIncorrectCount(1);
                }
                break;
            case UNCLEAR:
                if (preExpirationVote) {
                    updateBlahDAO.setPredictionUnclearCount(1);
                } else {
                    updateBlahDAO.setPredictionResultUnclearCount(1);
                }
                break;
            default:
                throw new SystemErrorException("prediction vote option not supported", predictionVote, ErrorCodes.SERVER_SEVERE_ERROR);
        }

        // Update Blah dao
        if (preExpirationVote) {
            userBlahInfoData.dao.setPredictionVote(vote);
        } else {
            userBlahInfoData.dao.setPredictionResultVote(vote);
        }
        updateBlahDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

        // Update blah user info dao
        if (userBlahInfoData.exists) {
            userBlahInfoData.dao._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
        } else {
            userBlahInfoData.dao._insert();
        }
    }

    private UserBlahInfoData ensurePredictionConsistent(String userId, String blahId, boolean preExpirationVote, Date expirationDate) throws SystemErrorException, InvalidRequestException {
        if (expirationDate == null) {
            throw new SystemErrorException("prediction missing expiration date", ErrorCodes.SERVER_DATA_INCONSISTENT);
        }
        if (preExpirationVote && expirationDate.before(new Date())) {
            throw new InvalidRequestException("prediction expired", ErrorCodes.INVALID_INPUT);
        } else if (!preExpirationVote && expirationDate.after(new Date())) {
            throw new InvalidRequestException("prediction has not expired", ErrorCodes.INVALID_INPUT);
        }

        // Check whether user already voted
        final UserBlahInfoDAO userBlahInfoDAO = storeManager.createUserBlahInfo(userId, blahId);
        final String[] fieldsToReturnHint = {preExpirationVote ? UserBlahInfoDAO.PREDICTION_VOTE : UserBlahInfoDAO.PREDICTION_RESULT_VOTE};
        final UserBlahInfoDAO dao = (UserBlahInfoDAO) userBlahInfoDAO._findByCompositeId(fieldsToReturnHint, UserBlahInfoDAO.USER_ID, UserBlahInfoDAO.BLAH_ID);
        if (dao != null) {
            if ((preExpirationVote && dao.getPredictionVote() != null)
                    || (!preExpirationVote && dao.getPredictionResultVote() != null)) {
                throw new InvalidRequestException("user already voted on prediction", ErrorCodes.INVALID_INPUT);
            }
            userBlahInfoDAO.setId(dao.getId());
        }
        return new UserBlahInfoData(userBlahInfoDAO, dao != null);
    }

    public UserPayload getAuthorFromComment(LocaleId en_us, String commentId)
            throws InvalidRequestException, SystemErrorException, ResourceNotFoundException {
        if (CommonUtilities.isEmptyString(commentId)) {
            throw new InvalidRequestException("missing comment id", ErrorCodes.MISSING_COMMENT_ID);
        }
        final CommentDAO commentDAO = (CommentDAO) getStoreManager().createComment(commentId)._findByPrimaryId(CommentDAO.AUTHOR_ID);
        if (commentDAO == null) {
            throw new ResourceNotFoundException("no such comment", ErrorCodes.NOT_FOUND_COMMENT_ID);
        }
        final String authorId = commentDAO.getAuthorId();
        if (authorId == null) {
            throw new SystemErrorException("no author for comment", ErrorCodes.MISSING_AUTHOR_ID);
        }
        return getUserManager().getUserInfo(en_us, authorId, false, null, null);
    }

    public UserPayload getAuthorFromBlah(LocaleId en_us, String blahId)
            throws InvalidRequestException, ResourceNotFoundException, SystemErrorException {
        if (CommonUtilities.isEmptyString(blahId)) {
            throw new InvalidRequestException("missing blah id", ErrorCodes.MISSING_BLAH_ID);
        }
        final BlahDAO blahDAO = (BlahDAO) getStoreManager().createBlah(blahId)._findByPrimaryId(BlahDAO.AUTHOR_ID);
        if (blahDAO == null) {
            throw new ResourceNotFoundException("no such blah", ErrorCodes.NOT_FOUND_BLAH_ID);
        }
        final String authorId = blahDAO.getAuthorId();
        if (authorId == null) {
            throw new SystemErrorException("no author for blah", ErrorCodes.MISSING_AUTHOR_ID);
        }
        return getUserManager().getUserInfo(en_us, authorId, false, null, null);
    }


    /**
     * <p> Returns true if this blah type id is of the specified category type.</p>
     *
     * @param blahTypeId   The blah type id
     * @param categoryType The category type
     * @return True if this blah is of the specified category type
     */
    private boolean isCategory(String blahTypeId, BlahTypeCategoryType categoryType) {
        BlahTypeEntry entry = null;
        synchronized (blahTypeIdToBlahTypeEntryMapLock) {
            entry = blahTypeIdToBlahTypeEntryMap.get(blahTypeId);
        }
        return (entry != null && entry.categoryType == categoryType);
    }

    /**
     * <p>Returns polling information for user for specified blah.</p>
     *
     * @param localeId The locale
     * @param blahId   The blah id
     * @param userId   The user id
     * @return The polling information for this blah and user combination
     */
    public UserBlahInfoPayload getPollVoteInfo(LocaleId localeId, String blahId, String userId) throws SystemErrorException {
        final UserBlahInfoDAO dao = (UserBlahInfoDAO) getStoreManager().createUserBlahInfo(userId, blahId)._findByCompositeId(
                new String[]{UserBlahInfoDAO.POLL_VOTE_INDEX, UserBlahInfoDAO.POLL_VOTE_TIMESTAMP}, UserBlahInfoDAO.USER_ID, UserBlahInfoDAO.BLAH_ID);
        if (dao != null) {
            return new UserBlahInfoPayload(dao);
        }
        return new UserBlahInfoPayload(userId, blahId);
    }

    /**
     * <p>Returns prediction vote information for user for specified blah.</p>
     * @param userId
     * @param blahId
     * @return
     * @throws SystemErrorException
     */
    public UserBlahInfoPayload getPredictionVoteInfo(String userId, String blahId) throws SystemErrorException {
        final UserBlahInfoDAO dao = (UserBlahInfoDAO) getStoreManager().createUserBlahInfo(userId, blahId)._findByCompositeId(
                new String[]{UserBlahInfoDAO.PREDICTION_RESULT_VOTE, UserBlahInfoDAO.PREDICTION_VOTE}, UserBlahInfoDAO.USER_ID, UserBlahInfoDAO.BLAH_ID);
        if (dao != null) {
            return new UserBlahInfoPayload(dao);
        }
        return new UserBlahInfoPayload(userId, blahId);

    }



    private StoreManager getStoreManager() {
        return storeManager;
    }

    private TrackingManager getTrackingManager() {
        return trackingManager;
    }

    private UserManager getUserManager() {
        return userManager;
    }


    private class UserBlahInfoData {
        private final UserBlahInfoDAO dao;
        private final boolean exists;

        UserBlahInfoData(UserBlahInfoDAO dao, boolean exists) {
            this.dao = dao;
            this.exists = exists;
        }
    }

    /**
     * Throws a state exception if the user already voted in this poll.
     *
     * @param blahId The blah id (a poll blah)
     * @param userId The user id
     * @return An otherwise empty user blah info dao with the blahId and userId already filled in
     * @throws SystemErrorException
     * @throws StateConflictException
     */
    private UserBlahInfoData ensureUserDidNotVoteOnPoll(String blahId, String userId) throws SystemErrorException, StateConflictException {
        final UserBlahInfoDAO userBlahInfo = getStoreManager().createUserBlahInfo(userId, blahId);
        final UserBlahInfoDAO dao = (UserBlahInfoDAO) userBlahInfo._findByCompositeId(new String[]{UserBlahInfoDAO.ID, UserBlahInfoDAO.POLL_VOTE_INDEX}, UserBlahInfoDAO.USER_ID, UserBlahInfoDAO.BLAH_ID);
        if (dao != null) {
            if (dao.getPollVoteIndex() != null) {
                throw new StateConflictException("userId '" + userId + "' already voted on poll for blahId '" + blahId + "'", ErrorCodes.ALREADY_VOTED_ON_POLL);
            }
            userBlahInfo.setId(dao.getId());
        }
        return new UserBlahInfoData(userBlahInfo, dao != null);
    }

    /**
     * Simply logs views and opens for anonymous users.
     *
     * @param en_us
     * @param entity
     */
    public void updateBlahViewsOrOpensByAnonymousUser(LocaleId en_us, BlahPayload entity, String blahId) throws InvalidRequestException, SystemErrorException {

        final int maxViewIncrements = maxOpensOrViewsPerUpdate;
        final Integer viewCount = CommonUtilities.checkValueRange(entity.getViews(), 0, maxViewIncrements, entity);
        final Integer openCount = CommonUtilities.checkValueRange(entity.getOpens(), 0, maxViewIncrements, entity);
        if (viewCount == 0 && openCount == 0) {
            return;
        }

        final BlahDAO blahDAO = (BlahDAO) getStoreManager().createBlah(blahId);
        if (!blahDAO._exists()) {
            throw new InvalidRequestException("Invalid blah '" + blahId + "'", ErrorCodes.INVALID_INPUT);
        }
        if (viewCount != 0) {
            blahDAO.setViews(viewCount);
        }
        if (openCount != 0) {
            blahDAO.setOpens(openCount);
        }
        blahDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

        trackingMgr.trackBlahUpdate(blahId, null, null, viewCount, openCount, null);
    }

    /**
     * Allows a user to update a blah's vote, views, and/or opens, in any combination.
     * Ignored if there is no promotion/demotion, views or opens in request.
     *
     *
     * @param localeId
     * @param entity
     * @param blahId
     * @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException
     */
    public void updateBlahPromotionViewOrOpens(LocaleId localeId, BlahPayload entity, String blahId) throws InvalidRequestException, StateConflictException, SystemErrorException, ResourceNotFoundException {
        if (!CommonUtilities.isEmptyString(entity.getText()) || !CommonUtilities.isEmptyString(entity.getBody())) {
            throw new InvalidRequestException("user may not edit blah text or body", entity, ErrorCodes.CANNOT_EDIT_TEXT);
        }
        if (CommonUtilities.isEmptyString(blahId)) {
            throw new InvalidRequestException("missing blah id", entity, ErrorCodes.MISSING_BLAH_ID);
        }
        final String userId = entity.getAuthorId();
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("missing update user id", entity, ErrorCodes.MISSING_AUTHOR_ID);
        }

        final Integer promotionOrDemotion = CommonUtilities.checkDiscreteValue(entity.getUserPromotion(), entity);

        final int maxViewIncrements = maxOpensOrViewsPerUpdate;
        final Integer viewCount = CommonUtilities.checkValueRange(entity.getViews(), 0, maxViewIncrements, entity);
        final Integer openCount = CommonUtilities.checkValueRange(entity.getOpens(), 0, maxViewIncrements, entity);
        if (promotionOrDemotion == 0 && viewCount == 0 && openCount == 0) {
            return; // don't complain
        }

        if (!getStoreManager().createUser(userId)._exists()) {
            throw new ResourceNotFoundException("user not found; userId=" + userId, ErrorCodes.NOT_FOUND_USER_ID);
        }

        final boolean createdComment = false;
        final BlahDAO updateBlahDAO = updateBlahInternal(LocaleId.en_us, blahId, userId, promotionOrDemotion, viewCount, openCount, createdComment);

        // Track it
        final boolean isBlah = true;
        final boolean isNewObject = false;
        final String objectId = blahId;
        final String subObjectId = null;
        final boolean promoted = (promotionOrDemotion.intValue() > 0);
        final boolean demoted = (promotionOrDemotion.intValue() < 0);
        getTrackingManager().trackObject(TrackerOperation.UPDATE_BLAH, userId, updateBlahDAO.getAuthorId(), isBlah, isNewObject, objectId, subObjectId, promoted, demoted, null, viewCount, openCount);

        if (doIndex()) {
            indexBlah(updateBlahDAO);
        }

//        final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.UPDATE_BLAH);
//        tracker.setBlahId(blahId);
//        tracker.setUserId(userId);
//        tracker.setBlahAuthorId(authorId);
//        if (vote != 0) {
//            tracker.setVote(vote);
//        }
//        if (viewCount != 0) {
//            tracker.setViews(viewCount);
//        }
//        if (openCount != 0) {
//            tracker.setOpens(openCount);
//        }
//        trackingManager.track(LocaleId.en_us, tracker);
    }

    /**
     * Called when updating a blah's votes|opens|views or when creating a comment.
     * Updates userBlahInfoDAO and blahDAOs.
     *
     * @param localeId
     * @param blahId          The blah's id
     * @param userId          The user's id
     * @param promotionOrDemotion            The promotion or demotion (always one of 0, 1, -1)
     * @param viewCount       The view count (always one of 0, 1, -1)
     * @param openCount       The open count (always one of 0, 1, -1)
     * @param creatingComment True if this is called when a comment is created
     * @return BlahDAO  The blah DAO including the updates plus the author id
     * @throws SystemErrorException
     * @throws ResourceNotFoundException
     * @throws StateConflictException
     * @throws InvalidRequestException
     */
    private BlahDAO updateBlahInternal(LocaleId localeId, String blahId, String userId, Integer promotionOrDemotion, Integer viewCount, Integer openCount, boolean creatingComment) throws SystemErrorException, ResourceNotFoundException, StateConflictException, InvalidRequestException {
        BlahDAO blahDAO = getStoreManager().createBlah(blahId);
        blahDAO = (BlahDAO) blahDAO._findByPrimaryId(BlahDAO.AUTHOR_ID, BlahDAO.GROUP_ID, BlahDAO.TYPE_ID);
        if (blahDAO == null) {
            throw new ResourceNotFoundException("blah not found; blahId=" + blahId, ErrorCodes.NOT_FOUND_BLAH_ID);
        }
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("userId is missing", ErrorCodes.MISSING_USER_ID);
        }
        final String authorId = blahDAO.getAuthorId();
        final boolean userIsBlahAuthor = userId.equals(authorId);
        if (userIsBlahAuthor && (promotionOrDemotion != 0)) {
            throw new StateConflictException("userId=" + userId + " may not promote/demote own blahId=" + blahId, ErrorCodes.USER_CANNOT_UPDATE_ON_OWN_BLAH);
        }

        final String[] fieldsToReturnHint = new String[]{UserBlahInfoDAO.PROMOTION};
        final UserBlahInfoDAO userBlahHistory = (UserBlahInfoDAO) getStoreManager().createUserBlahInfo(userId, blahId)._findByCompositeId(fieldsToReturnHint, UserBlahInfoDAO.USER_ID, UserBlahInfoDAO.BLAH_ID);
        final boolean insert = (userBlahHistory == null);

        final UserBlahInfoDAO userBlahInfoDAO = getStoreManager().createUserBlahInfo();
        if (insert) {
            userBlahInfoDAO.initToDefaultValues(localeId);
        } else {
            userBlahInfoDAO.setId(userBlahHistory.getId());
        }
        if (promotionOrDemotion != 0) {
            if (!insert && userBlahHistory.getPromotedOrDemoted() != null && userBlahHistory.getPromotedOrDemoted() != 0) {
                throw new InvalidRequestException("userId=" + userId + " has already voted on blahId=" + blahId, ErrorCodes.USER_ALREADY_VOTED_ON_BLAH_ID);
            }
            userBlahInfoDAO.setPromotedOrDemoted(promotionOrDemotion > 0 ? 1 : -1);
        }
        if (viewCount != 0) {
            userBlahInfoDAO.setViews(viewCount);
        }
        if (openCount != 0) {
            userBlahInfoDAO.setOpens(openCount);
        }
        if (creatingComment) {
            userBlahInfoDAO.setComments(1);
        }

        if (insert) {
            userBlahInfoDAO.setGroupId(blahDAO.getGroupId()); // original group id
            userBlahInfoDAO.setBlahTypeId(blahDAO.getTypeId());
            userBlahInfoDAO.setUserId(userId);
            userBlahInfoDAO.setBlahId(blahId);
            userBlahInfoDAO._insert();
        } else {
            userBlahInfoDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
        }

        final BlahDAO blah = getStoreManager().createBlah(blahId);
        if (promotionOrDemotion > 0) {
            blah.setPromotedCount(1);
        } else if (promotionOrDemotion < 0) {
            blah.setDemotedCount(1);
        }
        if (viewCount != 0) {
            blah.setViews(viewCount);
        }  // incremental
        if (openCount != 0) {
            blah.setOpens(openCount);
        }  // incremental
        if (creatingComment) {
            blah.setComments(1);
        }  // incremental
        blah._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

        blah.setAuthorId(authorId);

        return blah;
    }

    /**
     * Permanently deletes the blahs and its comments from the DB and the index.
     * Transaction cost:
     * 1. delete comments
     * 2. delete blah
     * 3. remove comments from index
     * 4. remove blah from index
     * <p/>
     * TODO draconic: should archive them if needed, though old blahs should really just fade away
     *
     * @param localeId
     * @param blahId
     * @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    public void deleteBlah(LocaleId localeId, String blahId) throws InvalidRequestException, SystemErrorException {
        if (CommonUtilities.isEmptyString(blahId)) {
            throw new InvalidRequestException("missing blah id", ErrorCodes.MISSING_BLAH_ID);
        }

        // Fetch comment ids
        final CommentDAO searchCommentDAO = getStoreManager().createComment();
        searchCommentDAO.setBlahId(blahId);

        // Fetch comments to delete
        final List<CommentDAO> commentDAOs = (List<CommentDAO>) searchCommentDAO._findManyByCompositeId(null, null, null, new String[]{CommentDAO.ID}, CommentDAO.BLAH_ID);

        // Delete comments and blahs
        searchCommentDAO._deleteByCompositeId(CommentDAO.BLAH_ID); // multiple deletes
        final BlahDAO blah = getStoreManager().createBlah(blahId);
        decrementGroupBlahCount(blah);
        blah._deleteByPrimaryId();


        if (doIndex()) {
            for (CommentDAO commentDAO : commentDAOs) {
                deleteCommentFromIndex(commentDAO);
            }
            deleteBlahFromIndex(blahId); // TODO when queued, this will automatically delete dependent comments
        }
    }

    private void decrementGroupBlahCount(BlahDAO blah) throws SystemErrorException {
        final BlahDAO blahDAO = (BlahDAO) blah._findByPrimaryId(BlahDAO.GROUP_ID);
        final String groupId = blahDAO.getGroupId();
        final GroupDAO group = getStoreManager().createGroup(groupId);
        group.setBlahCount(-1);
        group._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
    }

    public List<BlahPayload> getBlahs(LocaleId localeId, String userId, String authorId, String typeId, Integer start, Integer count, String sortFieldName) throws SystemErrorException, InvalidRequestException {
        // userId, typeId and sortFieldName ignore (set to null by client) for now
        count = ensureCount(count);
        final BlahDAO blahDAO = getStoreManager().createBlah();
        final boolean hasAuthor = !CommonUtilities.isEmptyString(authorId);
        if (hasAuthor) {
            blahDAO.setAuthorId(authorId);
        }
        final boolean hasType = !CommonUtilities.isEmptyString(typeId);
        if (hasType) {
            blahDAO.setTypeId(typeId);
        }

        return toBlahPayload(userId, (List<BlahDAO>) ((hasAuthor && hasType) ? blahDAO._findManyByCompositeId(start, count, sortFieldName, null, BlahDAO.AUTHOR_ID, BlahDAO.TYPE_ID) :
                hasAuthor ? blahDAO._findManyByCompositeId(start, count, sortFieldName, null, BlahDAO.AUTHOR_ID) :
                        (hasType) ? blahDAO._findManyByCompositeId(start, count, sortFieldName, null, BlahDAO.TYPE_ID) :
                                blahDAO._findMany(start, count, sortFieldName)));
    }

    private Integer ensureCount(Integer count) {
        if (count == null || count > returnedObjectLimit) {
            count = returnedObjectLimit;
        }
        return count;
    }

    private List<BlahPayload> toBlahPayload(String userId, List<? extends BlahDAO> blahDAOs) throws SystemErrorException {
        final List<BlahPayload> payload = new ArrayList<BlahPayload>(blahDAOs.size());
        final boolean hasUserId = !CommonUtilities.isEmptyString(userId);
        for (BaseDAO dao : blahDAOs) {
            final BlahPayload blahPayload = new BlahPayload(dao);
            payload.add(blahPayload);
            if (hasUserId) {  // TODO this is a temporary add-on: data should be pre-aggregated for query
                addUserBlahInfoToPayload(userId, blahPayload.getId(), blahPayload);
            }
        }
        return payload;
    }

    // TODO cache with hourly TTL candidate
    public List<BlahTypePayload> getBlahTypes(LocaleId localeId) throws SystemErrorException {
        ensureBlahTypesCached();
        return getBlahTypes();
    }

    private void ensureBlahTypesCached() throws SystemErrorException {
        if (((lastTimeBlahTypesCached - System.currentTimeMillis()) > TEN_MINUTES_BLAH_TYPE_CACHE_REFRESH_IN_MILLIS)) {
            refreshBlahTypesCache();
        }
    }

    private class BlahTypeEntry {
        private final BlahTypeDAO blahTypeDAO;
        private final BlahTypeCategoryType categoryType;

        BlahTypeEntry(BlahTypeDAO blahTypeDAO, BlahTypeCategoryType categoryType) {
            this.blahTypeDAO = blahTypeDAO;
            this.categoryType = categoryType;
        }
    }

    private void refreshBlahTypesCache() throws SystemErrorException {
        final List<BlahTypeDAO> blahTypeDAOs = (List<BlahTypeDAO>) getStoreManager().createBlahType()._findMany(null, null, null);
        final HashMap<String, BlahTypeEntry> map = new HashMap<String, BlahTypeEntry>(blahTypeDAOs.size());
        for (BlahTypeDAO dao : blahTypeDAOs) {
            if (dao.getCategoryId() == null) {
                throw new SystemErrorException("Blah type id '" + dao.getId() + " missing category id", ErrorCodes.SERVER_SEVERE_ERROR);
            }
            final BlahTypeCategoryType categoryId = BlahTypeCategoryType.findByCategoryId(dao.getCategoryId());
            if (categoryId == null) {
                throw new SystemErrorException("Blah type dao has an invalid category id '" + dao.getCategoryId() + "'. Should be one of " + BlahTypeCategoryType.values(), ErrorCodes.SERVER_SEVERE_ERROR);
            }
            map.put(dao.getId(), new BlahTypeEntry(dao, categoryId));
        }
        synchronized (blahTypeIdToBlahTypeEntryMapLock) {
            blahTypeIdToBlahTypeEntryMap = map;
            lastTimeBlahTypesCached = System.currentTimeMillis();
        }
        System.out.println(new Date() + ": Blah type cache refreshed");
    }

    public List<BlahTypePayload> getBlahTypes() throws SystemErrorException {
        synchronized (blahTypeIdToBlahTypeEntryMapLock) {
            final List<BlahTypePayload> bt = new ArrayList<BlahTypePayload>(blahTypeIdToBlahTypeEntryMap.size());
            for (BlahTypeEntry entry : blahTypeIdToBlahTypeEntryMap.values()) {
                bt.add(new BlahTypePayload(entry.blahTypeDAO.toMap()));
            }
            return bt;
        }
    }

    /**
     * Used for testing purposes: forces refresh of all caches.
     *
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    public void refreshCaches() throws SystemErrorException {
        refreshBlahTypesCache();
    }

    /**
     * Returns the blah by blah id.
     *
     * @param localeId
     * @param blahId         The blah's id
     * @param userId         Optional: if a user id is included, the user's votes, views, and opens for the blah will be included in the blah.
     * @param stats          Optional: if true, then add the blah trackers for the specified to/from dates
     * @param statsStartDate Optional: if stats=true, then this is the start of the date range (inclusive of date)
     *                       to search for stats. If statsEndDate is not null and stats=true, then this field is required.
     * @param statsEndDate   Optional: if stats=true, then this is the end of the date range (inclusive of end date)
     *                       to search for stats
     * @return BlahPayload  The blah information
     * @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException
     */
    public BlahPayload getBlahById(LocaleId localeId, String blahId, String userId, boolean stats, String statsStartDate, String statsEndDate)
            throws InvalidRequestException, SystemErrorException, ResourceNotFoundException {

        if (CommonUtilities.isEmptyString(blahId)) {
            throw new InvalidRequestException("missing blah id", ErrorCodes.MISSING_BLAH_ID);
        }

        final BlahDAO blahDAO = getStoreManager().createBlah();
        blahDAO.setId(blahId);
        final BaseDAO found = blahDAO._findByPrimaryId();
        if (found == null) {
            throw new ResourceNotFoundException("blah not found", "blahId=" + blahId, ErrorCodes.NOT_FOUND_BLAH_ID);
        }

        // TODO both fetches below are unacceptably inefficient
        final BlahPayload blahPayload = new BlahPayload(found);

        // If user is in session, we include the user's stats for this blah
        if (!CommonUtilities.isEmptyString(userId)) { // TODO separate this into its own API call
            addUserBlahInfoToPayload(userId, blahId, blahPayload);
        }
        if (stats) { // if stats are requested, we include the blah's stats   TODO separate this into its own API call
            fetchAndAddBlahTrackers(blahId, statsStartDate, statsEndDate, blahPayload);
        }
        return blahPayload;
    }

    private BlahDAO getBlahById_unsafe(String blahId, String... fieldsToReturnHint) throws SystemErrorException {
        final BlahDAO blahDAO = getStoreManager().createBlah(blahId);
        if (fieldsToReturnHint != null && fieldsToReturnHint.length > 0) {
            return (BlahDAO) blahDAO._findByPrimaryId();
        }
        return (BlahDAO) blahDAO._findByPrimaryId(fieldsToReturnHint);
    }

    private void fetchAndAddBlahTrackers(String blahId, String statsStartDate, String statsEndDate, BlahPayload blahPayload) throws InvalidRequestException, SystemErrorException {
        if (statsStartDate == null && statsEndDate != null) {
            throw new InvalidRequestException("stats start date (s) must be provided if an end date (e) is specified", ErrorCodes.INVALID_INPUT);
        }

        // blah tracker ids are: <blahId><2-digit year><2-digit month><2-digit day of month> (e.g., 5031b25d036408e9b4160b95120820)
        // Next two calls also parse the string to make sure we've got a real date
        Calendar startDate = main.java.com.eweware.service.base.date.DateUtils.convertToCalendar(statsStartDate);
        Calendar endDate = main.java.com.eweware.service.base.date.DateUtils.convertToCalendar(statsEndDate);
//        logger.info(statsStartDate+" startDate=" + new Date(startDate.getTimeInMillis()));
//        logger.info(statsEndDate+" endDate=" + new Date(endDate.getTimeInMillis()));

        // We've made sure that the dates can be parsed as expected and are available as calendar instances for comparison
        List<BlahTrackerPayload> trackers = null;
        if (DateUtils.isSameDay(startDate, endDate)) { // fetch single
//            logger.info("Same day");
            final String trackerId = TrackingManager.makeTrackerIdExternal(TrackerType.BLAH, blahId, startDate);
            final BlahTrackerDAO blahTrackerDAO = (BlahTrackerDAO) getStoreManager().createBlahTracker(trackerId)._findByPrimaryId();
            if (blahTrackerDAO != null) {
                trackers = new ArrayList<BlahTrackerPayload>(1);
                trackers.add(new BlahTrackerPayload(blahTrackerDAO.toMap()));
            }
        } else { // range search
            final BlahTrackerDAO blahTrackerDAO = getStoreManager().createBlahTracker();
            final String from = blahId + statsStartDate;
            final String to = (statsEndDate == null) ? null : blahId + statsEndDate;
//            logger.info("from=" + from + "  to=" + to);
            final boolean sorted = true;
            final List<? extends BaseDAO> trackerDAOs = blahTrackerDAO._findRangeSingleField(sorted, BlahTrackerDAO.ID, from, true, to, true);
            trackers = new ArrayList<BlahTrackerPayload>(trackerDAOs.size());
            for (BaseDAO dao : trackerDAOs) {
                trackers.add(new BlahTrackerPayload(dao.toMap()));
            }
        }
        blahPayload.setStats(trackers == null ? new ArrayList<BlahTrackerPayload>(0) : trackers);
    }

    public static String extractYearMonthFromTrackerDate(String trackerDate) {
        return trackerDate.substring(0, 4);
    }

    // TODO this is a temporary add-on: data should be pre-aggregated for query
    private void addUserBlahInfoToPayload(String userId, String blahId, BlahPayload blahPayload) throws SystemErrorException {
        final UserBlahInfoDAO userBlahDAO = (UserBlahInfoDAO) getStoreManager().createUserBlahInfo(userId, blahId)._findByCompositeId(
                new String[]{UserBlahInfoDAO.PROMOTION, UserBlahInfoDAO.VIEWS, UserBlahInfoDAO.OPENS},
                UserBlahInfoDAO.USER_ID, UserBlahInfoDAO.BLAH_ID);
        if (userBlahDAO != null) {
            blahPayload.setUserPromotion(userBlahDAO.getPromotedOrDemoted());
            blahPayload.setUserViews(userBlahDAO.getViews());
            blahPayload.setUserOpens(userBlahDAO.getOpens());
        }
    }

    public String getAuthorIdForBlah(String blahId) throws SystemErrorException, ResourceNotFoundException {
        if (CommonUtilities.isEmptyString(blahId)) {
            throw new SystemErrorException("missing blah id", ErrorCodes.MISSING_BLAH_ID);
        }
        BlahDAO blahDAO = getStoreManager().createBlah(blahId);
        blahDAO = (BlahDAO) blahDAO._findByPrimaryId(BlahDAO.AUTHOR_ID);
        if (blahDAO == null) {
            throw new ResourceNotFoundException("did not find blahId=" + blahId, ErrorCodes.NOT_FOUND_BLAH_ID);
        }
        return blahDAO.getAuthorId();
    }


    public String getAuthorIdForComment(String commentId) throws SystemErrorException, ResourceNotFoundException {
        if (CommonUtilities.isEmptyString(commentId)) {
            throw new SystemErrorException("missing comment id", ErrorCodes.MISSING_COMMENT_ID);
        }
        CommentDAO commentDAO = getStoreManager().createComment(commentId);
        commentDAO = (CommentDAO) commentDAO._findByPrimaryId(CommentDAO.AUTHOR_ID);
        if (commentDAO == null) {
            throw new ResourceNotFoundException("did not find commentId=" + commentId, ErrorCodes.NOT_FOUND_COMMENT_ID);
        }
        return commentDAO.getAuthorId();
    }

    /**
     * Used to check whether the blah exists.
     *
     * @param blahId The blah's id
     * @param entity Any entity payload to use in a rest not found exception
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException
     */
    private void checkBlahById(String blahId, Object entity) throws SystemErrorException, ResourceNotFoundException {
        if (CommonUtilities.isEmptyString(blahId)) {
            throw new SystemErrorException("missing blah id", entity, ErrorCodes.MISSING_BLAH_ID);
        }
        if (!getStoreManager().createBlah(blahId)._exists()) {
            throw new ResourceNotFoundException("not found blahId=" + blahId, entity, ErrorCodes.NOT_FOUND_BLAH_ID);
        }
    }

    /**
     * When a comment is created, it may optionally include a vote for the blah upon which
     * it comments. The author of the blah is permitted to comment on it, but his vote is ignored.
     *
     * @param localeId
     * @param commentAuthorId
     * @param request         @return
     * @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException
     * @throws StateConflictException
     */
    public CommentPayload createComment(LocaleId localeId, String commentAuthorId, CommentPayload request) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException, StateConflictException {

        // Check parameters
        if (request.getCommentVotes() != null) {
            throw new InvalidRequestException("cannot vote for comment when creating it", request, ErrorCodes.CANNOT_VOTE_ON_COMMENT_WHEN_CREATING_IT);
        }
        final String blahId = request.getBlahId();
        if (CommonUtilities.isEmptyString(blahId)) {
            throw new InvalidRequestException("missing blah id", request, ErrorCodes.MISSING_BLAH_ID);
        }
        if (CommonUtilities.isEmptyString(commentAuthorId)) {
            throw new InvalidRequestException("missing authorId", request, ErrorCodes.MISSING_AUTHOR_ID);
        }
        String text = request.getText();
        if (CommonUtilities.isEmptyString(text)) {
            throw new InvalidRequestException("missing text", request, ErrorCodes.MISSING_TEXT);
        }
        text = CommonUtilities.scrapeMarkup(text);

        getUserManager().checkUserById(commentAuthorId, request);
        final Integer blahVote = CommonUtilities.checkDiscreteValue(request.getBlahVote(), request);
        boolean votedForBlah = (blahVote != 0);

        // Check that blah exists and if this comment includes a vote that the comment author is not the blah's author
        final BlahDAO blahDAO = getStoreManager().createBlah(blahId);
        final BlahDAO blah = (BlahDAO) blahDAO._findByPrimaryId(BlahDAO.AUTHOR_ID);
        if (blah == null) {
            throw new InvalidRequestException("no blahId=" + blahId + " exists", request, ErrorCodes.INVALID_INPUT);
        }
        if (votedForBlah && blah.getAuthorId().equals(commentAuthorId)) { // Check if comment author is also blah author: voting not allowed
            throw new InvalidRequestException("authorId=" + commentAuthorId + " (author of the blahId=" + blahId + ") cannot vote on own blah", request, ErrorCodes.USER_CANNOT_UPDATE_ON_OWN_BLAH);
        }

        // Create comment
        CommentDAO commentDAO = getStoreManager().createComment();
        commentDAO.initToDefaultValues(localeId);
        commentDAO.setBlahId(blahId);
        commentDAO.setText(text);
        commentDAO.setAuthorId(commentAuthorId);
        if (votedForBlah) {
            commentDAO.setBlahVote(blahVote);
        }
        commentDAO.setStrength(0.0);
        commentDAO.setCreated(new Date());
        commentDAO._insert();

        final int viewCount = 0;
        final int openCount = 0;
        final boolean createdComment = true;
        updateBlahInternal(LocaleId.en_us, blahId, commentAuthorId, blahVote, viewCount, openCount, createdComment);

        final boolean isBlah = false;
        final boolean isNewObject = true;
        final String objectId = commentDAO.getId();
        final String subObjectId = blahId;
        final boolean voteUp = (blahVote > 0);
        final boolean voteDown = (blahVote < 0);
        getTrackingManager().trackObject(TrackerOperation.CREATE_COMMENT, commentAuthorId, commentAuthorId, isBlah, isNewObject, objectId, subObjectId, voteUp, voteDown, null, request.getViews(), request.getOpens());

//        final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.CREATE_COMMENT);
//        tracker.setBlahAuthorId(blahAuthorId);
//        tracker.setCommentAuthorId(commentAuthorId);
//        tracker.setUserId(commentAuthorId);
//        tracker.setBlahId(blahId);
//        if (blahVote != 0) {
//            tracker.setVote(blahVote);
//        }
//        trackingManager.track(LocaleId.en_us, tracker);

        if (doIndex()) {
            indexComment(commentDAO); // index new comment
        }

        return new CommentPayload(commentDAO);
    }

    /**
     * The following fields are tracked: votes for comment (blah vote can't be changed after a comment is created),
     * and number of times comment was viewed or opened.
     * <p/>
     * TODO check injection problems: e.g., blahId or authorId changed, etc...
     *
     *
     * @param localeId
     * @param entity   The client request
     * @param userId
     *@param commentId  @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException
     * @throws StateConflictException
     */
    public void updateComment(LocaleId localeId, CommentPayload entity, String userId, String commentId) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException, StateConflictException {

        if (CommonUtilities.isEmptyString(commentId)) {
            throw new InvalidRequestException("missing comment id", entity, ErrorCodes.MISSING_COMMENT_ID);
        }
        if (!CommonUtilities.isEmptyString(entity.getText())) {
            throw new InvalidRequestException("user may not edit comment text", entity, ErrorCodes.CANNOT_EDIT_TEXT);
        }
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("missing user id", entity, ErrorCodes.MISSING_AUTHOR_ID);
        }
        if (entity.getBlahVote() != null) {
            throw new InvalidRequestException("user cannot vote on blah when updating a comment; userId=" +
                    userId + " commentId=" + entity.getId() + "blahId=" + entity.getBlahId(), ErrorCodes.CANNOT_VOTE_ON_BLAH_WHEN_UPDATING_COMMENT);
        }

        final Integer voteForComment = CommonUtilities.checkDiscreteValue(entity.getCommentVotes(), entity);
        final boolean didVoteForComment = (voteForComment != 0);
        final Integer views = CommonUtilities.checkValueRange(entity.getViews(), 0, maxOpensOrViewsPerUpdate, entity);
        final Integer opens = CommonUtilities.checkValueRange(entity.getOpens(), 0, maxOpensOrViewsPerUpdate, entity);
        if (!didVoteForComment &&
                (views == 0) &&
                (opens == 0)) {
            return; // nothing to update
        }

        getUserManager().checkUserById(userId, entity);

        final CommentDAO commentDAO = (CommentDAO) getStoreManager().createComment(commentId)._findByPrimaryId(CommentDAO.BLAH_ID, CommentDAO.AUTHOR_ID);
        if (commentDAO == null) {
            throw new ResourceNotFoundException("No commentId=" + commentId, entity, ErrorCodes.NOT_FOUND_COMMENT_ID);
        }
        final String blahId = commentDAO.getBlahId();
        final String commentAuthorId = commentDAO.getAuthorId();

        BlahDAO blahDAO = getStoreManager().createBlah(blahId);
        blahDAO = (BlahDAO) blahDAO._findByPrimaryId(BlahDAO.AUTHOR_ID);
        if (blahDAO == null) {
            throw new ResourceNotFoundException("no blahId=" + blahId + " exists to comment upon; commentId=" + commentId, ErrorCodes.NOT_FOUND_BLAH_ID);
        }
        final String blahAuthorId = blahDAO.getAuthorId();
        if (didVoteForComment && blahAuthorId.equals(userId)) {
            throw new InvalidRequestException("authorId=" + userId + " (author of the blahId=" + blahId + ") cannot vote on comments to author's own blah", entity, ErrorCodes.USER_CANNOT_VOTE_ON_COMMENTS_TO_ONES_OWN_BLAH);
        }
        final UserCommentInfoDAO userCommentInfoDAO = getStoreManager().createUserCommentInfo(userId, commentId);
        final UserCommentInfoDAO foundUserCommentInfo = (UserCommentInfoDAO) userCommentInfoDAO._findByCompositeId(new String[]{UserCommentInfoDAO.VOTE}, UserCommentInfoDAO.USER_ID, UserCommentInfoDAO.COMMENT_ID);
        if (didVoteForComment && foundUserCommentInfo != null && foundUserCommentInfo.getVote() != null && foundUserCommentInfo.getVote() != 0) {
            throw new InvalidRequestException("authorId=" + userId + " already voted for this comment; commentId=" + commentId, ErrorCodes.USER_ALREADY_VOTED_FOR_COMMENT);
        }
        if (didVoteForComment && commentAuthorId.equals(userId)) {
            throw new InvalidRequestException("comment author cannot vote for own comment; authorId=" + userId + " commentId=" + commentId, ErrorCodes.USER_CANNOT_VOTE_ON_OWN_COMMENT);
        }
        if (foundUserCommentInfo == null) {
            userCommentInfoDAO.initToDefaultValues(localeId);
        }
        if (voteForComment != 0) {
            userCommentInfoDAO.setVote(voteForComment);
        }
        if (views != 0) {
            userCommentInfoDAO.setViews(views);
        }
        if (opens != 0) {
            userCommentInfoDAO.setOpens(opens);
        }
        if (foundUserCommentInfo == null) {
            userCommentInfoDAO._insert();
        } else {
            userCommentInfoDAO.setId(foundUserCommentInfo.getId());
            userCommentInfoDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
        }

        // Update comment
        final CommentDAO commentUpdateDAO = getStoreManager().createComment(commentId);
        if (voteForComment != 0) {
            commentUpdateDAO.setCommentVotes(voteForComment);
            if (voteForComment > 0) {
                commentUpdateDAO.setCommentUpVotes(1);
            } else {
                commentUpdateDAO.setCommentDownVotes(1);
            }
        }
        if (views != 0) {
            commentUpdateDAO.setViews(views);
        }
        if (opens != 0) {
            commentUpdateDAO.setOpens(opens);
        }
        commentUpdateDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

        final boolean isBlah = false;
        final boolean isNewObject = false;
        final String objectId = commentId;
        final String subObjectId = blahId;
        final boolean voteUp = (voteForComment == 1);
        final boolean voteDown = (voteForComment == -1);
        getTrackingManager().trackObject(TrackerOperation.UPDATE_COMMENT, userId, commentAuthorId, isBlah, isNewObject, objectId, subObjectId, voteUp, voteDown, null, views, opens);

        if (doIndex()) {
            indexComment(commentUpdateDAO);
        }

//        final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.UPDATE_COMMENT);
//        tracker.setUserId(userId);
//        tracker.setCommentAuthorId(commentAuthorId);
//        tracker.setBlahAuthorId(blahAuthorId);
//        if (voteForComment != 0) {
//            tracker.setVote(voteForComment);
//        }
//        if (views != 0) {
//            tracker.setViews(views);
//        }
//        if (opens != 0) {
//            tracker.setOpens(opens);
//        }
//        trackingManager.track(LocaleId.en_us, tracker);
    }

    // TODO draconic: should archive them if needed, though old comments should really just fade away
    public void deleteComment(LocaleId localeId, String commentId) throws InvalidRequestException, SystemErrorException {
        if (CommonUtilities.isEmptyString(commentId)) {
            throw new InvalidRequestException("missing comment id", ErrorCodes.MISSING_COMMENT_ID);
        }
        final CommentDAO commentDAO = getStoreManager().createComment(commentId);
        commentDAO._deleteByPrimaryId();

        if (doIndex()) {
            deleteCommentFromIndex(commentDAO);
        }
    }

    public CommentPayload getCommentById(LocaleId localeId, boolean authenticated, String commentId, String userId, boolean stats, String statsStartDate, String statsEndDate) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException {
        if (CommonUtilities.isEmptyString(commentId)) {
            throw new InvalidRequestException("missing comment id", ErrorCodes.MISSING_COMMENT_ID);
        }
        final CommentDAO commentDAO = (CommentDAO) getStoreManager().createComment(commentId)._findByPrimaryId();
        if (commentDAO == null) {
            throw new ResourceNotFoundException("blah comment not found", "commentId=" + commentId, ErrorCodes.NOT_FOUND_COMMENT_ID);
        }
        final CommentPayload entity = new CommentPayload(commentDAO);
        if (!CommonUtilities.isEmptyString(userId)) {
            addUserCommentInfoToPayload(entity, commentId, userId);
        }
        if (stats) {
            fetchAndAddCommentTrackers(commentId, statsStartDate, statsEndDate, entity);
        }

        // TODO expensive! see WRS-252
        CommonUtilities.maybeAddUserNickname(storeManager, authenticated, commentDAO.getAuthorId(), entity);

        return entity;
    }

    private void addUserCommentInfoToPayload(CommentPayload comment, String commentId, String userId) throws SystemErrorException {
        final UserCommentInfoDAO dao = (UserCommentInfoDAO) getStoreManager().createUserCommentInfo(userId, commentId)._findByCompositeId(
                new String[]{UserCommentInfoDAO.VOTE, UserCommentInfoDAO.VIEWS, UserCommentInfoDAO.OPENS},
                UserCommentInfoDAO.USER_ID, UserCommentInfoDAO.COMMENT_ID);
        if (dao != null) {
            comment.setUserVote(dao.getVote());
            comment.setUserViews(dao.getViews());
            comment.setUserOpens(dao.getOpens());
        }
    }

    private void fetchAndAddCommentTrackers(String commentId, String statsStartDate, String statsEndDate, CommentPayload commentEntity) throws InvalidRequestException, SystemErrorException {
        if (statsStartDate == null && statsEndDate != null) {
            throw new InvalidRequestException("stats start date (s) must be provided if an end date (e) is specified", ErrorCodes.INVALID_INPUT);
        }

        // comment tracker ids are: <commentId><2-digit year><2-digit month><2-digit day of month> (e.g., 5031b25d036408e9b4160b95120820)
        Calendar startDate = main.java.com.eweware.service.base.date.DateUtils.convertToCalendar(statsStartDate);
        Calendar endDate = main.java.com.eweware.service.base.date.DateUtils.convertToCalendar(statsEndDate);

        // We've made sure that the dates can be parsed as expected and are available as calendar instances for comparison
        List<CommentTrackerPayload> trackers = null;
        if (DateUtils.isSameDay(startDate, endDate)) { // fetch single
            final String trackerId = TrackingManager.makeTrackerIdExternal(TrackerType.COMMENT, commentId, startDate);
            final CommentTrackerDAO commentTrackerDAO = (CommentTrackerDAO) getStoreManager().createCommentTracker(trackerId)._findByPrimaryId();
            if (commentTrackerDAO != null) {
                trackers = new ArrayList<CommentTrackerPayload>(1);
                trackers.add(new CommentTrackerPayload(commentTrackerDAO.toMap()));
            }
        } else { // range search
            final CommentTrackerDAO commentTrackerDAO = getStoreManager().createCommentTracker();
            final String from = commentId + statsStartDate;
            final String to = (statsEndDate == null) ? null : commentId + statsEndDate;
            final boolean sorted = true;
            final List<? extends BaseDAO> trackerDAOs = commentTrackerDAO._findRangeSingleField(sorted, CommentTrackerDAO.ID, from, true, to, true);
            trackers = new ArrayList<CommentTrackerPayload>(trackerDAOs.size());
            for (BaseDAO dao : trackerDAOs) {
                trackers.add(new CommentTrackerPayload(dao.toMap()));
            }
        }
        commentEntity.setStats(trackers == null ? new ArrayList<CommentTrackerPayload>(0) : trackers);

    }

    public List<CommentPayload> getComments(LocaleId localeId, boolean authenticated, String blahId, String userId, String authorId, Integer start, Integer count, String sortFieldName) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException {
        count = ensureCount(count);
        final boolean forBlah = !CommonUtilities.isEmptyString(blahId);
        if (forBlah) {
            checkBlahById(blahId, blahId);
        }
        final CommentDAO commentDAO = getStoreManager().createComment();
        if (forBlah) {
            commentDAO.setBlahId(blahId);
        }
        final boolean forAuthor = !CommonUtilities.isEmptyString(authorId);
        if (forAuthor) {
            commentDAO.setAuthorId(authorId);
        }
        final List<CommentDAO> commentDAOs;
        if (forBlah && forAuthor) {
            commentDAOs = (List<CommentDAO>) commentDAO._findManyByCompositeId(start, count, sortFieldName, null, CommentDAO.BLAH_ID, CommentDAO.AUTHOR_ID);
        } else if (forAuthor) {
            commentDAOs = (List<CommentDAO>) commentDAO._findManyByCompositeId(start, count, sortFieldName, null, CommentDAO.AUTHOR_ID);
        } else if (forBlah) {
            commentDAOs = (List<CommentDAO>) commentDAO._findManyByCompositeId(start, count, sortFieldName, null, CommentDAO.BLAH_ID);
        } else {
            commentDAOs = (List<CommentDAO>) commentDAO._findMany(start, count, sortFieldName);
        }

        final List<CommentPayload> comments = new ArrayList<CommentPayload>(commentDAOs.size());
        for (CommentDAO dao : commentDAOs) {
            final CommentPayload commentPayload = new CommentPayload(dao);
            // TODO expensive! see WRS-252
            CommonUtilities.maybeAddUserNickname(storeManager, authenticated, dao.getAuthorId(), commentPayload);
            comments.add(commentPayload);
        }
        if (!CommonUtilities.isEmptyString(userId)) {
            for (CommentPayload comment : comments) {
                addUserCommentInfoToPayload(comment, comment.getId(), userId);
            }
        }
        return comments;
    }

    // TODO replace get user inbox with this: right now, trusts client that inbox is accessible by user
    // TODO 1. if the groupId is not anonymous, check whether the user is authenticated and, if so, check whether he joined the group.
    // TODO 2. require the group id
    public List<InboxBlahPayload> getInbox(LocaleId localeId, String groupId, HttpServletRequest request, Integer inboxNumber,
                                           String blahTypeId, Integer start, Integer count, String sortFieldName, Integer sortDirection)
            throws SystemErrorException, InvalidAuthorizedStateException, InvalidRequestException, ResourceNotFoundException {

        if (groupId == null) {
            throw new InvalidRequestException("Missing group id", ErrorCodes.MISSING_GROUP_ID);
        }

        count = ensureCount(count);
        if (sortDirection == null || (sortDirection != 1 && sortDirection != -1)) {
            sortDirection = -1;
        }

        checkGroupAccess(request, groupId);

        // Cycle through inboxes
        final Integer maxInbox = inboxHandler.getMaxInbox(groupId);
        final Integer unknown = -1;
        if (maxInbox == unknown) {
            // we don't know the max: attempt to get the first inbox (getting an inbox from the inbox cache retrieves the max, if any)
            inboxNumber = 0;
        } else {
            if (inboxNumber == null) { // if no inbox number is requested, find last
                Integer lastInbox = BlahguaSession.getLastInboxNumber(request, groupId);
                inboxNumber = (lastInbox == null) ? 0 : (++lastInbox);  // if we have last, increment it; else start at first inbox
                if (inboxNumber >= maxInbox) { // rewind if past the maximum number of inboxes; else go to next
                    inboxNumber = 0;
                }
            }
        }
        final Inbox inbox = inboxHandler.getInboxFromCache(groupId, inboxNumber, blahTypeId, start, count, sortFieldName, sortDirection);

        BlahguaSession.setLastInboxNumber(request, groupId, inboxNumber);

        if (inbox == null) {
            logger.warning("Got no mailbox for groupId '" + groupId + "' inbox #" + inboxNumber + " when maxInbox=" + maxInbox);
            return new ArrayList<InboxBlahPayload>(0);
        }


        return inbox.getItems();
    }

    /**
     * <p>Checks whether the group may be accessed in this session.</p>
     *
     * @param request The http request (unchecked!)
     * @param groupId The group id (unchecked!)
     * @throws SystemErrorException If there's a problem. It may be that
     *                              there's a race condition such that a session is being destroyed while
     *                              this call is in progress. Instead of using locks (inefficient), we
     *                              just take this risk: client should just retry.
     * @throws InvalidAuthorizedStateException
     *                              If the specified group
     *                              is not open and the user does not have access to it because
     *                              either he's not logged in and/or has not joined the group.
     */
    private void checkGroupAccess(HttpServletRequest request, String groupId) throws SystemErrorException, InvalidAuthorizedStateException, ResourceNotFoundException {

        if (!storeManager.createGroup(groupId)._exists()) {
            throw new ResourceNotFoundException("Group id '" + groupId + "' does not exist");
        }

        final boolean isOpenGroup = groupManager.isOpenGroup(groupId);

        if (!isOpenGroup) {
            final boolean authenticated = BlahguaSession.isAuthenticated(request);
            if (!authenticated) {
                throw new InvalidAuthorizedStateException("user not authorized to access inbox", ErrorCodes.UNAUTHORIZED_USER);
            }
            String userId = BlahguaSession.getUserId(request);
            if (userId == null) { // could be a race condition: client should retry
                throw new SystemErrorException("No userId for session", ErrorCodes.SERVER_RECOVERABLE_ERROR);
            }
            final UserGroupDAO userGroupDAO = getStoreManager().createUserGroup();
            userGroupDAO.setUserId(userId);
            userGroupDAO.setGroupId(groupId);
            userGroupDAO.setState(AuthorizedState.A.toString());
            if (!userGroupDAO._exists()) {
                throw new InvalidAuthorizedStateException("user not authorized to access inbox for groupId=" + groupId);
            }
        }
    }

    // Indexing -----------------

    private void initializeBlahIndex() {
        if (doIndex()) {
            startBlahIndex();
            startCommentIndex();
        }
    }

    private void startCommentIndex() {
        if (doIndex()) {
            final IndexReaderDecorator<BlahguaFilterIndexReader> decorator = new BlahguaIndexReaderDecorator();
            final ZoieConfig config = makeIndexConfiguration();
            System.out.println("Creating Zoie index in directory " + commentIndexDir.getAbsolutePath());
            this.commentIndexingSystem = new ZoieSystem<BlahguaFilterIndexReader, CommentDAO>(new DefaultDirectoryManager(commentIndexDir), new BlahCommentDataIndexableInterpreter(), decorator, config);
            commentIndexingSystem.start(); // ready to accept indexing events
        }
    }

    private void startBlahIndex() {
        if (doIndex()) {
            final IndexReaderDecorator<BlahguaFilterIndexReader> decorator = new BlahguaIndexReaderDecorator();
            final ZoieConfig config = makeIndexConfiguration();
            System.out.println("Creating Zoie index in directory " + blahIndexDir.getAbsolutePath());
            this.blahIndexingSystem = new ZoieSystem<BlahguaFilterIndexReader, BlahDAO>(new DefaultDirectoryManager(blahIndexDir), new BlahDataIndexableInterpreter(), decorator, config);
            blahIndexingSystem.start(); // ready to accept indexing events
        }
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


    /**
     * Indexes or updates a blah. The blah must contain its id field.
     * If the blah has already been indexed, it will simply be updated.
     * TODO This operation should work off a queue.
     * TODO If we only index the blah's text, updating it shouldn't be necessary as long as text can't be edited
     *
     * @param blahDAO
     * @return
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    private void indexBlah(BlahDAO blahDAO) throws SystemErrorException {
        if (doIndex()) {
            doIndexBlah((BlahDAO) maybeUpdateForIndex(blahDAO, true));
        }
    }

    private void doIndexBlah(BlahDAO blah) throws SystemErrorException {
        if (doIndex()) {
            final String batchVersion = "0"; // TODO huh?
            final DataEvent<BlahDAO> event = new DataEvent<BlahDAO>(blah, batchVersion);
            final List<DataEvent<BlahDAO>> events = new ArrayList<DataEvent<BlahDAO>>(1);
            events.add(event);
            try {
                this.blahIndexingSystem.consume(events);
                if (debug) {
                    System.out.println("Indexed blah: " + blah);
                }
            } catch (ZoieException e) {
                throw new SystemErrorException("Indexing error", e, ErrorCodes.SERVER_INDEXING_ERROR);
            }
        }
    }

    /**
     * Deletes the blah from the index.
     *
     * @param blahId The blah's id.
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    private void deleteBlahFromIndex(String blahId) throws SystemErrorException {
        if (doIndex()) {
            final BlahDAO blah = getStoreManager().createBlah(blahId);
            blah.setDeleted(Boolean.TRUE);
            indexBlah(blah);
        }
    }

    /**
     * Indexes or updates a comment. The comment's id field must be filled.
     * TODO This operation should work off a queue.
     * TODO If we only index the comment's text, updating it shouldn't be necessary as long as text can't be edited
     *
     * @param comment
     * @return
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    private void indexComment(CommentDAO comment) throws SystemErrorException {
        if (doIndex()) {
            doIndexComment((CommentDAO) maybeUpdateForIndex(comment, false));
        }
    }

    private void doIndexComment(CommentDAO comment) throws SystemErrorException {
        if (doIndex()) {
            final String batchVersion = "0"; // TODO huh?
            final DataEvent<CommentDAO> event = new DataEvent<CommentDAO>(comment, batchVersion);
            final List<DataEvent<CommentDAO>> events = new ArrayList<DataEvent<CommentDAO>>(1);
            events.add(event);
            try {
                this.commentIndexingSystem.consume(events);
                if (debug) {
                    System.out.println("Indexed comment: " + comment);
                }

            } catch (ZoieException e) {
                throw new SystemErrorException("indexing error", e, ErrorCodes.SERVER_INDEXING_ERROR);
            }
        }
    }

    private void deleteCommentFromIndex(CommentDAO commentDAO) throws SystemErrorException {
        if (doIndex()) {
            if (CommonUtilities.isEmptyString(commentDAO.getId())) {
                throw new SystemErrorException("missing comment id in " + this, ErrorCodes.SERVER_INDEXING_ERROR);
            }
            commentDAO.setDeleted(Boolean.TRUE);
            indexComment(commentDAO);
        }
    }

//    /**
//     * Deletes the blah from the index.
//     *
//     * @param commentId The comment's id.
//     * @throws main.java.com.eweware.service.base.error.SystemErrorException
//     *
//     */
//    private void deleteCommentFromIndex(String commentId) throws SystemErrorException {
//        if (CommonUtilities.isEmptyString(commentId)) {
//            throw new SystemErrorException("missing comment id in " + this, ErrorCodes.SERVER_INDEXING_ERROR);
//        }
//        final CommentDAO comment = storeManager.createComment(commentId);
//        comment.setDeleted(Boolean.TRUE);
//        indexComment(comment);
//    }

    /**
     * Gets all documents from the index
     * TODO add start, count
     *
     * @param maxResults
     * @param indexingSystem
     * @param searchBlahs
     * @return
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    public List<BasePayload> getFromIndex(Integer maxResults, ZoieSystem<BlahguaFilterIndexReader, ?> indexingSystem, boolean searchBlahs) throws SystemErrorException {
        List<ZoieIndexReader<BlahguaFilterIndexReader>> readerList = null;
        try {
            readerList = indexingSystem.getIndexReaders();
            final MultiReader reader = new MultiReader(readerList.toArray(new IndexReader[readerList.size()]), false);
            maxResults = Math.min(reader.maxDoc(), maxResults);
            final List<BasePayload> payload = new ArrayList<BasePayload>(maxResults);
            for (int i = 0; i < maxResults; i++) {
                final Document doc = reader.document(i);
                final BasePayload blahOrComment = searchBlahs ? BlahDataIndexable.fromBlahDocument(doc) : BlahCommentDataIndexable.fromCommentDocument(doc);
                payload.add(blahOrComment);
            }
            return payload;
        } catch (IOException e) {
            throw new SystemErrorException("IO error while retrieving from blahs/comment index", e, ErrorCodes.SERVER_SEVERE_ERROR);
        } finally {
            if (readerList != null) {
                indexingSystem.returnIndexReaders(readerList);
            }
        }
    }


    /**
     * Searches the index
     *
     * @param localeId
     * @param fieldName Name of field to search. Else, searches all indexed fields.
     * @param query     The query
     * @param subset    A specific subset of the data to search (either "b" for blahs or "c" for comments).
     *                  Default: searches blahs.
     * @return
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    public List<BasePayload> search(LocaleId localeId, String fieldName, String query, String subset, Integer maxResults) throws SystemErrorException {

        if (maxResults == null) {
            maxResults = 20; // TODO
        }
        if (subset == null || (!subset.equals("a") && (!subset.equals("c")))) {
            subset = "b";
        }
        return searchBlahs(true, fieldName, query, maxResults);
    }

    private List<BasePayload> searchBlahs(boolean searchBlahs, String fieldName, String query, Integer maxResults) throws SystemErrorException {
        if (!doIndex()) {
            return new ArrayList<BasePayload>(0);
        }
        if (CommonUtilities.isEmptyString(fieldName)) {
            fieldName = BlahDAO.TEXT;
        }

        final List<BasePayload> payload = new ArrayList<BasePayload>();
        List<ZoieIndexReader<BlahguaFilterIndexReader>> readerList = null;
        ZoieSystem<BlahguaFilterIndexReader, ?> indexingSystem = null;
        try {
            indexingSystem = searchBlahs ? blahIndexingSystem : commentIndexingSystem;

            if (CommonUtilities.isEmptyString(query)) {
                return getFromIndex(maxResults, indexingSystem, searchBlahs);
            }

            // get the IndexReaders
            readerList = indexingSystem.getIndexReaders();

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
            final Query q = buildQuery(fieldName, query, indexingSystem.getAnalyzer());
            final TopDocs docs = searcher.search(q, 10);

            final ScoreDoc[] scoreDocs = docs.scoreDocs;

            // convert to UID for each doc
            for (ScoreDoc scoreDoc : scoreDocs) {
                final int docid = scoreDoc.doc;
                final Document doc = searcher.doc(docid);
                payload.add(searchBlahs ? BlahDataIndexable.fromBlahDocument(doc) : BlahCommentDataIndexable.fromCommentDocument(doc));
                final SubReaderInfo<BlahguaFilterIndexReader> readerInfo = subReaderAccessor.getSubReaderInfo(docid);

//				long uid = (long) ((ZoieIndexReader<BlahguaFilterIndexReader>) readerInfo.subreader.getInnerReader()).getUID(readerInfo.subdocid);
//				System.out.println(uid);
            }

//            searcher.close();  // TODO should not be necessary due to returnIndexReaders in finally, but not sure

            return payload; // blahs.toArray(new BlahPayload[blahs.size()]);

        } catch (Exception e) {
            throw new SystemErrorException(e);
        } finally {
            if (readerList != null) {
                indexingSystem.returnIndexReaders(readerList);
            }
        }
    }

    private Query buildQuery(String fieldNames, String query, Analyzer analyzer)
            throws ParseException {
        final String[] fields = fieldNames.split(",");
        // TODO validateKey field names
        if (fields.length == 1) {
            return new QueryParser(Version.LUCENE_35, fields[0], analyzer).parse(query);
        }
        final int count = fields.length;
        final StringBuilder b = new StringBuilder();
        for (int i = 0; i < count; i++) {
            final String fieldName = fields[i];
            b.append(fieldName);
            b.append(":\"");
            b.append(query);
            b.append("\"");
            if (i != count - 1) {
                b.append(" AND ");
            }
        }
        return new QueryParser(Version.LUCENE_35, fields[0], analyzer).parse(b.toString());
    }

    /**
     * Given an blah or comment DAO, if the DAO is an update (i.e., if it has already
     * been indexed) it returns a copy of the DAO suitable for insertion into the index.
     * In particular, numeric fields are treated as increment operations rather than
     * as absolute values. If the DAO is a new object (i.e., not indexed), it is simply returned.
     *
     * @param dao    The CommentDAO or BlahDAO
     * @param isBlah True if the DAO is a BlahDAO (else it should be a CommentDAO
     * @return BaseDAO  Returns either the original BlahDAO/CommentDAO or, if it's an update,
     *         a copy of the DAO suitable for insertion into the index.
     * @throws SystemErrorException
     */
    private BaseDAO maybeUpdateForIndex(BaseDAO dao, boolean isBlah) throws SystemErrorException {
        if (!doIndex()) {
            return dao;
        }
        final Document doc = getDocument(dao.getId(), isBlah);
        if (doc == null) {
            return dao;
        } else { // it's an update
            // Create a copy to be used just for the index
            final BaseDAO updateDAO = isBlah ? getStoreManager().createBlah(dao) : getStoreManager().createComment(dao);
            Map<String, Object> update = new HashMap<String, Object>();
            for (Map.Entry<String, Object> entry : updateDAO.toMap().entrySet()) {
                final String fieldName = entry.getKey();
                if (fieldName.equals(BaseDAO.ID)) {
                    continue;
                }
                final BaseSchema schema = isBlah ? BlahSchema.getSchema(LocaleId.en_us) : CommentSchema.getSchema(LocaleId.en_us);
                final SchemaSpec spec = schema.getSpec(fieldName);
                Object newValue = entry.getValue();
                if (spec.isNumeric()) {
                    if (newValue != null) { // increment numeric value
                        final String oldVal = doc.get(fieldName);
                        if (spec.isInteger()) {
                            final Integer oldValue = (oldVal == null) ? 0 : CommonUtilities.getValueAsInteger(oldVal);
                            newValue = CommonUtilities.getValueAsInteger(newValue) + oldValue;
                        } else if (spec.isReal()) {
                            final Double oldValue = (oldVal == null) ? 0.0 : CommonUtilities.getValueAsDouble(oldVal);
                            newValue = CommonUtilities.getValueAsDouble(newValue) + oldValue;
                        } else {
                            throw new SystemErrorException("Unknown numeric data type=" + spec.getDataType(), ErrorCodes.SERVER_CONFIGURATION_ERROR);
                        }
                        update.put(fieldName, newValue);
                    }
                } else { // insert or override non-numeric field
                    update.put(fieldName, newValue);
                }
            }
            for (Fieldable field : doc.getFields()) {  // restore unchanged fields
                if (!update.containsKey(field.name())) {
                    update.put(field.name(), field.stringValue());
                }
            }
            final boolean validateAndConvert = false; // all fields from the Lucene doc are strings, so don't do this
            updateDAO.addFromMap(update, validateAndConvert);
            return updateDAO;
        }
    }

    /**
     * @param blahOrCommentId A blah or comment id
     * @param isBlahIndex     True if the id is a blah id (else it is a comment id)
     * @return Document Returns a Lucene document for the specified id or null if the document doesn't exist.
     * @throws SystemErrorException
     */
    private Document getDocument(String blahOrCommentId, boolean isBlahIndex) throws SystemErrorException {
        List<ZoieIndexReader<BlahguaFilterIndexReader>> readerList = null;
        ZoieSystem<BlahguaFilterIndexReader, ?> system = null;
        try {
            system = isBlahIndex ? this.blahIndexingSystem : this.commentIndexingSystem;

            // get the IndexReaders
            readerList = system.getIndexReaders();

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
            final Query q = buildQuery(BaseDAO.ID, blahOrCommentId, system.getAnalyzer());
            final TopDocs docs = searcher.search(q, 1);

            final ScoreDoc[] scoreDocs = docs.scoreDocs;

            // convert to UID for each doc
            for (ScoreDoc scoreDoc : scoreDocs) {
                final int docid = scoreDoc.doc;
                return searcher.doc(docid);
            }

            return null;

        } catch (Exception e) {
            throw new SystemErrorException(e);
        } finally {
            if (readerList != null && system != null) {
                system.returnIndexReaders(readerList);
            }
        }
    }
}
