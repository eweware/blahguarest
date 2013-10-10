package com.eweware.service.mgr;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.eweware.service.base.CommonUtilities;
import com.eweware.service.base.error.*;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.mgr.ManagerInterface;
import com.eweware.service.base.mgr.ManagerState;
import com.eweware.service.base.payload.*;
import com.eweware.service.base.store.StoreManager;
import com.eweware.service.base.store.dao.*;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.dao.schema.BlahSchema;
import com.eweware.service.base.store.dao.schema.CommentSchema;
import com.eweware.service.base.store.dao.schema.SchemaSpec;
import com.eweware.service.base.store.dao.tracker.TrackerOperation;
import com.eweware.service.base.store.dao.type.BlahTypeCategoryType;
import com.eweware.service.base.store.dao.type.DAOUpdateType;
import com.eweware.service.base.store.dao.type.MediaReferendType;
import com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import com.eweware.service.base.type.TrackerType;
import com.eweware.service.mgr.auxiliary.InboxData;
import com.eweware.service.mgr.auxiliary.InboxHandler;
import com.eweware.service.mgr.type.PredictionExpirationType;
import com.eweware.service.mgr.type.PredictionVote;
import com.eweware.service.rest.session.BlahguaSession;
import com.eweware.service.search.index.blah.BlahCommentDataIndexable;
import com.eweware.service.search.index.blah.BlahCommentDataIndexableInterpreter;
import com.eweware.service.search.index.blah.BlahDataIndexable;
import com.eweware.service.search.index.blah.BlahDataIndexableInterpreter;
import com.eweware.service.search.index.common.BlahguaFilterIndexReader;
import com.eweware.service.search.index.common.BlahguaIndexReaderDecorator;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 */
public final class BlahManager implements ManagerInterface {

    private static final Logger logger = Logger.getLogger(BlahManager.class.getName());

    private static final long TEN_MINUTES_BLAH_TYPE_CACHE_REFRESH_IN_MILLIS = 1000l * 60 * 10;
    private static final long THIRTY_MINUTES_IN_MILLIS = 1000l * 60 * 30;
    private static final String EMPTY_STRING = "";
    private static final int MAXIMUM_BLAH_BODY_LENGTH = 2000;
    private static final int MAXIMUM_COMMENT_LENGTH = 1500;
    private static final int MAXIMUM_BLAH_HEADLINE_LENGTH = 64;

    private final boolean _doIndex;
    private final File _blahIndexDir;
    private final File _commentIndexDir;
    private final int _batchSize;
    private final long _batchDelay;
    private final long _maxOpensOrViewsPerUpdate;
    private final InboxHandler _inboxHandler;

    /**
     * Maps an existing blah type id to its data
     */
    private Map<String, BlahTypeEntry> _blahTypeIdToBlahTypeEntryMap = new HashMap<String, BlahTypeEntry>();
    private Object _blahTypeIdToBlahTypeEntryMapLock = new Object(); // locks _blahTypeIdToBlahTypeEntryMap
    private long _lastTimeBlahTypesCached = System.currentTimeMillis() - TEN_MINUTES_BLAH_TYPE_CACHE_REFRESH_IN_MILLIS - 1;

    private ZoieSystem<BlahguaFilterIndexReader, BlahDAO> _blahIndexingSystem;
    private ZoieSystem<BlahguaFilterIndexReader, CommentDAO> _commentIndexingSystem;
    private ManagerState _state = ManagerState.UNKNOWN;
    public static BlahManager singleton;
    private StoreManager _storeManager;
    private TrackingManager _trackingManager;
    private TrackingMgr _trackingMgr;
    private UserManager _userManager;
    private GroupManager _groupManager;
    private final Integer _returnedObjectLimit;

    public static BlahManager getInstance() throws SystemErrorException {
        if (BlahManager.singleton == null) {
            throw new SystemErrorException("blah manager not initialized", ErrorCodes.SERVER_NOT_INITIALIZED);
        }
        return BlahManager.singleton;
    }

    public BlahManager(Boolean doIndex, String blahIndexDir, String commentIndexDir,
                       String batchSize, String batchDelay, int maxOpensOrViewsPerUpdate,
                       Integer returnedObjectLimit) {
        _doIndex = (doIndex == Boolean.TRUE);
        _blahIndexDir = new File(blahIndexDir);
        _commentIndexDir = new File(commentIndexDir);
        _batchSize = Integer.parseInt(batchSize);
        _batchDelay = Long.parseLong(batchDelay);
        _maxOpensOrViewsPerUpdate = maxOpensOrViewsPerUpdate;
        _returnedObjectLimit = returnedObjectLimit;
        try {
            _inboxHandler = new InboxHandler();
        } catch (SystemErrorException e) {
            throw new WebServiceException(e);
        }
        BlahManager.singleton = this;
        _state = ManagerState.INITIALIZED;

        if (doIndex) {
            System.out.println("*** BlahManager Initializing ***");
            ensureIndex(_blahIndexDir);
            ensureIndex(_commentIndexDir);
            System.out.println("*** BlahManager Blah Index: " + _blahIndexDir.getAbsolutePath() + " ***");
            System.out.println("*** BlahManager Comment Index: " + _commentIndexDir.getAbsolutePath() + " ***");
        } else {
            System.out.println("*** BlahManager search disabled ***");
        }

        System.out.println("*** BlahManager initialized ***");
    }

    public boolean doIndex() {
        return _doIndex;
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
            _storeManager = MongoStoreManager.getInstance();
            _trackingManager = TrackingManager.getInstance();
            _userManager = UserManager.getInstance();
            _groupManager = GroupManager.getInstance();
            _trackingMgr = TrackingMgr.getInstance();

            if (doIndex()) {
                initializeBlahIndex();
            }

            refreshBlahTypesCache();

            _state = ManagerState.STARTED;
            System.out.println("*** BlahManager started ***");

        } catch (Exception e) {
            throw new WebServiceException(e);
        }
    }

    public void shutdown() {
        if (doIndex()) {
            _blahIndexingSystem.shutdown();
            _commentIndexingSystem.shutdown();
        }
        _state = ManagerState.SHUTDOWN;
        System.out.println("*** BlahManager shut down ***");
    }

    public ManagerState getState() {
        return _state;
    }


    /**
     * Creates a new blah authored by user.
     *
     * @param localeId
     * @param authorId
     * @param entity   The request object  @return BlahPayload A blah payload including the new blah id
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     * @throws InvalidRequestException
     * @throws ResourceNotFoundException
     */
    public BlahPayload createBlah(LocaleId localeId, String authorId, BlahPayload entity) throws SystemErrorException, InvalidRequestException, ResourceNotFoundException, StateConflictException {
        ensureReady();
        // Check required fields
        if (CommonUtilities.isEmptyString(authorId)) {
            throw new InvalidRequestException("missing field authorId=" + authorId, entity, ErrorCodes.MISSING_USER_ID);
        }

        final String typeId = entity.getTypeId();
        if (!isTypeIdValid(typeId)) {
            throw new InvalidRequestException("invalid blah type id '" + typeId + "'", ErrorCodes.MISSING_BLAH_TYPE_ID);
        }
        final boolean isPoll = isBlahTypeCategory(typeId, BlahTypeCategoryType.POLL);
        final boolean isPrediction = (!isPoll && isBlahTypeCategory(typeId, BlahTypeCategoryType.PREDICTION));
        String text = entity.getText();
        if ((isPoll || isPrediction) && CommonUtilities.isEmptyString(text)) {
            throw new InvalidRequestException("Blah text line required for polls and predictions", ErrorCodes.MISSING_TEXT);
        }

        final List<String> mediaIds = entity.getImageIds();
        final boolean hasMedia = (mediaIds != null && mediaIds.size() > 0);
        if (hasMedia) {
            final String mediaId = mediaIds.get(0);
            final MediaDAO mediaDAO = getStoreManager().createMedia(mediaId);
            if (!mediaDAO._exists()) {
                throw new ResourceNotFoundException("Media id '" + mediaId + "' not found", ErrorCodes.MEDIA_NOT_FOUND);
            }
            mediaDAO.setReferendType(MediaReferendType.B.toString());
            mediaDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
        }

        text = cleanupBlahTextString(text);
        if (!CommonUtilities.isEmptyString(text)) {
            if (text.length() > MAXIMUM_BLAH_HEADLINE_LENGTH) {
                throw new InvalidRequestException("Blah text line cannot exceed 1024 chars", ErrorCodes.MAXIMUM_TEXT_FIELD_LENGTH_EXCEEDED);
            }
            entity.setText(text);
        }
        String body = entity.getBody();
        if (!CommonUtilities.isEmptyString(body)) {
            body = cleanupBlahTextString(body);
            if (body.length() > MAXIMUM_BLAH_BODY_LENGTH) {
                throw new InvalidRequestException("Blah body text cannot exceed 1024 chars", ErrorCodes.MAXIMUM_TEXT_FIELD_LENGTH_EXCEEDED);
            }
            entity.setBody(body);
        }

        final String groupId = entity.getGroupId();
        if (CommonUtilities.isEmptyString(groupId)) {
            throw new InvalidRequestException("missing field groupId=" + groupId, ErrorCodes.MISSING_GROUP_ID);
        }

        // Ensure user is active in group
        ensureUserActiveInGroup(authorId, groupId);

        verifyBadges(entity);

        // Create fresh blah to prevent injection
        final BlahDAO blahDAO = getStoreManager().createBlah();
        blahDAO.initToDefaultValues(localeId);
        blahDAO.addFromMap(entity, true); // removes fields not in schema
        blahDAO.setAuthorId(authorId);
        if (hasMedia) {
            blahDAO.setImageIds(mediaIds);
        }
        if (isPoll) {
            addPollData(text, blahDAO);
        } else {
            if (isPrediction) {
                addPredictionData(blahDAO, entity.getExpirationDate());
            }
        }
        blahDAO._insert();

        updateGroupBlahCount(groupId, true);

        // Add to inboxes
        _inboxHandler.spreadBlahToRecents(LocaleId.en_us, blahDAO, groupId);

        // Track it
        trackBlah(authorId, blahDAO);

//        final TrackerDAO tracker = _storeManager.createTracker(TrackerOperation.CREATE_BLAH);
//        tracker.setBlahId(blahDAO.getId());
//        tracker.setUserId(authorId);
//        tracker.setBlahAuthorId(authorId);
//        tracker.setGroupId(groupId);
//        _trackingManager.track(LocaleId.en_us, tracker);

        if (doIndex()) {
            indexBlah(blahDAO);
        }

        return new BlahPayload(blahDAO);
    }



    /**
     * <p>If there is no text, it returns an empty string; else it cleans up
     * the text by scraping off any possible HTML markup</p>
     *
     * @param text
     * @return <p>A valid blah text string (suitable for text line or body)</p>
     * @throws SystemErrorException
     */
    private String cleanupBlahTextString(String text) throws SystemErrorException {
        if (CommonUtilities.isEmptyString(text)) {
            text = EMPTY_STRING;
        } else {
            text = CommonUtilities.scrapeMarkup(text);
        }
        return text;
    }

    private void verifyBadges(BlahPayload entity) throws SystemErrorException, InvalidRequestException {
        final List<String> badgeIds = entity.getBadgeIds();
        if (badgeIds != null && badgeIds.size() > 0) {
            final BadgeDAO badge = _storeManager.createBadge();
            for (String badgeId : badgeIds) {
                badge.setId(badgeId);
                if (badge._count() == 0L) {
                    throw new InvalidRequestException("badge id '" + badgeId + "' is invalid", ErrorCodes.INVALID_INPUT);
                }
            }
        }
    }

    private void verifyCommentBadges(CommentPayload entity) throws SystemErrorException, InvalidRequestException {
        final List<String> badgeIds = entity.getBadgeIds();
        if (badgeIds != null && badgeIds.size() > 0) {
            final BadgeDAO badge = _storeManager.createBadge();
            for (String badgeId : badgeIds) {
                badge.setId(badgeId);
                if (badge._count() == 0L) {
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
            final Date date = expirationDate; // com.eweware.service.base.date.DateUtils.fromISODateTimeToUTC(expirationDate);
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


    /**
     * Adds the poll options to the poll blah.
     *
     * @param text    The first line of text
     * @param blahDAO The blah
     * @throws InvalidRequestException
     * @throws SystemErrorException
     */
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
            long count = pollOptionsText.size();
            blahDAO.setPollOptionCount(count);
            final List<Long> vcs = new ArrayList<Long>();
            while (count-- > 0) {
                vcs.add(0L);
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
        groupDAO.setBlahCount(increment ? 1L : -1L);
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
        final Long viewCount = null;
        final Long openCount = null;
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
        synchronized (_blahTypeIdToBlahTypeEntryMapLock) {
            ensureBlahTypesCached();
            return _blahTypeIdToBlahTypeEntryMap.get(typeId) != null;
        }
    }

    public void pollVote(LocaleId localeId, String blahId, String userId, Long pollOptionIndex) throws InvalidRequestException, SystemErrorException, StateConflictException, ResourceNotFoundException {
        ensureReady();
        if (blahId == null) {
            throw new InvalidRequestException("request missing blah id", ErrorCodes.MISSING_BLAH_ID);
        }
        if (pollOptionIndex == null || pollOptionIndex < 0L || pollOptionIndex > PollOptionDAOConstants.MAX_POLL_OPTIONS) {
            throw new InvalidRequestException("invalid poll index; maximum is " + PollOptionDAOConstants.MAX_POLL_OPTIONS + " but was=" + pollOptionIndex, ErrorCodes.INVALID_INPUT);
        }
        final BlahDAO blahDAO = getBlahById_unsafe(blahId, BlahDAO.POLL_OPTION_COUNT, BlahDAO.TYPE_ID, BlahDAO.AUTHOR_ID);
        if (blahDAO == null) {
            throw new InvalidRequestException("blahId '" + blahId + "' doesn't exist", ErrorCodes.NOT_FOUND_BLAH_ID);
        }
        if (!isBlahTypeCategory(blahDAO.getTypeId(), BlahTypeCategoryType.POLL)) {
            throw new InvalidRequestException("Blah id '" + blahId + "' is not a poll category blah", ErrorCodes.INVALID_UPDATE);
        }

        final UserBlahInfoData userBlahInfoData = ensureUserDidNotVoteOnPoll(blahId, userId);

        final Long noPollCount = -1L;
        final Long pollCount = CommonUtilities.safeGetLong(blahDAO.getPollOptionCount(), noPollCount);
        if (pollCount == noPollCount) {
            throw new InvalidRequestException("There are no poll options in this blah", ErrorCodes.SERVER_RECOVERABLE_ERROR);
        }
        if (pollOptionIndex >= pollCount) {
            throw new InvalidRequestException("poll index is out of range: it must be less than " + pollCount, ErrorCodes.INVALID_INPUT);
        }
        blahDAO.addPollOptionVote_immediate(pollOptionIndex);

        userBlahInfoData._dao.setPollVoteIndex(pollOptionIndex);
        userBlahInfoData._dao.setPollVoteTimestamp(new Date());
        if (userBlahInfoData._exists) {
            userBlahInfoData._dao._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
        } else {
            userBlahInfoData._dao.setAuthorId(blahDAO.getAuthorId());
            userBlahInfoData._dao._insert();
        }

        getTrackingManager().trackObject(TrackerOperation.UPDATE_BLAH, userId, userId, true, false, blahId, null, false, false, pollOptionIndex, null, null);
    }

    public void predictionVote(String userId, String blahId, String preOrPostExpiration, String vote)
            throws SystemErrorException, InvalidRequestException {
        ensureReady();
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
        final BlahDAO blahDAO = (BlahDAO) _storeManager.createBlah(blahId)._findByPrimaryId(BlahDAO.EXPIRATION_DATE, BlahDAO.TYPE_ID, BlahDAO.AUTHOR_ID);
        if (blahDAO == null) {
            throw new InvalidRequestException("invalid blah id", ErrorCodes.INVALID_INPUT);
        }
        if (blahDAO.getAuthorId().equals(userId)) {
            throw new InvalidRequestException("cannot vote on own prediction", ErrorCodes.USER_CANNOT_UPDATE_ON_OWN_BLAH);
        }

        if (!isBlahTypeCategory(blahDAO.getTypeId(), BlahTypeCategoryType.PREDICTION)) {
            throw new InvalidRequestException("not a prediction blah", ErrorCodes.INVALID_INPUT);
        }

        final Date expirationDate = blahDAO.getExpirationDate();

        final UserBlahInfoData userBlahInfoData = ensurePredictionConsistent(userId, blahId, preExpirationVote, expirationDate);

        final BlahDAO updateBlahDAO = _storeManager.createBlah(blahId);

        switch (predictionVote) {
            case YES:
                if (preExpirationVote) {
                    updateBlahDAO.setPredictionAgreeCount(1L);
                } else {
                    updateBlahDAO.setPredictionResultCorrectCount(1L);
                }
                break;
            case NO:
                if (preExpirationVote) {
                    updateBlahDAO.setPredictionDisagreeCount(1L);
                } else {
                    updateBlahDAO.setPredictionResultIncorrectCount(1L);
                }
                break;
            case UNCLEAR:
                if (preExpirationVote) {
                    updateBlahDAO.setPredictionUnclearCount(1L);
                } else {
                    updateBlahDAO.setPredictionResultUnclearCount(1L);
                }
                break;
            default:
                throw new SystemErrorException("prediction vote option not supported", predictionVote, ErrorCodes.SERVER_SEVERE_ERROR);
        }

        // Update Blah dao
        if (preExpirationVote) {
            userBlahInfoData._dao.setPredictionVote(vote);
        } else {
            userBlahInfoData._dao.setPredictionResultVote(vote);
        }
        updateBlahDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

        // Update blah user info dao
        if (userBlahInfoData._exists) {
            userBlahInfoData._dao._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
        } else {
            userBlahInfoData._dao.setAuthorId(blahDAO.getAuthorId());
            userBlahInfoData._dao._insert();
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
        final UserBlahInfoDAO userBlahInfoDAO = _storeManager.createUserBlahInfo(userId, blahId);
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
        ensureReady();
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
        ensureReady();
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
    private boolean isBlahTypeCategory(String blahTypeId, BlahTypeCategoryType categoryType) {
        BlahTypeEntry entry = null;
        synchronized (_blahTypeIdToBlahTypeEntryMapLock) {
            entry = _blahTypeIdToBlahTypeEntryMap.get(blahTypeId);
        }
        return (entry != null && entry._categoryType == categoryType);
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
        ensureReady();
        final UserBlahInfoDAO dao = (UserBlahInfoDAO) getStoreManager().createUserBlahInfo(userId, blahId)._findByCompositeId(
                new String[]{UserBlahInfoDAO.POLL_VOTE_INDEX, UserBlahInfoDAO.POLL_VOTE_TIMESTAMP}, UserBlahInfoDAO.USER_ID, UserBlahInfoDAO.BLAH_ID);
        if (dao != null) {
            return new UserBlahInfoPayload(dao);
        }
        return new UserBlahInfoPayload(userId, blahId);
    }

    /**
     * <p>Returns prediction vote information for user for specified blah.</p>
     *
     * @param userId
     * @param blahId
     * @return
     * @throws SystemErrorException
     */
    public UserBlahInfoPayload getPredictionVoteInfo(String userId, String blahId) throws SystemErrorException {
        ensureReady();
        final UserBlahInfoDAO dao = (UserBlahInfoDAO) getStoreManager().createUserBlahInfo(userId, blahId)._findByCompositeId(
                new String[]{UserBlahInfoDAO.PREDICTION_RESULT_VOTE, UserBlahInfoDAO.PREDICTION_VOTE}, UserBlahInfoDAO.USER_ID, UserBlahInfoDAO.BLAH_ID);
        if (dao != null) {
            return new UserBlahInfoPayload(dao);
        }
        return new UserBlahInfoPayload(userId, blahId);

    }


    private StoreManager getStoreManager() {
        return _storeManager;
    }

    private TrackingManager getTrackingManager() {
        return _trackingManager;
    }

    private UserManager getUserManager() {
        return _userManager;
    }

    private class UserBlahInfoData {
        private final UserBlahInfoDAO _dao;
        private final boolean _exists;

        UserBlahInfoData(UserBlahInfoDAO dao, boolean exists) {
            _dao = dao;
            _exists = exists;
        }
    }

    /**
     * Throws a _state exception if the user already voted in this poll.
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
        ensureReady();
        final long maxViewIncrements = _maxOpensOrViewsPerUpdate;
        final Long viewCount = CommonUtilities.checkValueRange(entity.getViews(), 0L, maxViewIncrements, entity);
        final Long openCount = CommonUtilities.checkValueRange(entity.getOpens(), 0L, maxViewIncrements, entity);
        if (viewCount == 0L && openCount == 0L) {
            return;
        }

        final BlahDAO blahDAO = (BlahDAO) getStoreManager().createBlah(blahId);
        if (!blahDAO._exists()) {
            throw new InvalidRequestException("Invalid blah '" + blahId + "'", ErrorCodes.INVALID_INPUT);
        }
        if (viewCount != 0L) {
            blahDAO.setViews(viewCount);
        }
        if (openCount != 0L) {
            blahDAO.setOpens(openCount);
        }
        blahDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

        _trackingMgr.trackBlahUpdate(blahId, null, null, viewCount, openCount, null);
    }

    /**
     * Allows a user to update a blah's vote, views, and/or opens, in any combination.
     * Ignored if there is no promotion/demotion, views or opens in request.
     *
     * @param localeId
     * @param entity
     * @param blahId
     * @throws InvalidRequestException
     * @throws com.eweware.service.base.error.SystemErrorException
     * @throws com.eweware.service.base.error.InvalidAuthorizedStateException
     *
     * @throws ResourceNotFoundException
     */
    public void updateBlah(LocaleId localeId, BlahPayload entity, String blahId) throws InvalidRequestException, InvalidAuthorizedStateException, StateConflictException, SystemErrorException, ResourceNotFoundException {
        ensureReady();

        if (CommonUtilities.isEmptyString(blahId)) {
            throw new InvalidRequestException("missing blah id", entity, ErrorCodes.MISSING_BLAH_ID);
        }
        final String userId = entity.getAuthorId();
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("missing update user id", entity, ErrorCodes.MISSING_AUTHOR_ID);
        }

        if (!getStoreManager().createUser(userId)._exists()) {
            throw new ResourceNotFoundException("user not found; userId=" + userId, ErrorCodes.NOT_FOUND_USER_ID);
        }

        final boolean createdComment = false;
        final BlahDAO updateBlahDAO = updateBlahInternal(LocaleId.en_us, blahId, userId, entity);

        // Track it
        final boolean isBlah = true;
        final boolean isNewObject = false;
        final String objectId = blahId;
        final String subObjectId = null;

        if (doIndex()) {
            indexBlah(updateBlahDAO);
        }
    }

    /**
     * Allows a user to update a blah's vote, views, and/or opens, in any combination.
     * Ignored if there is no promotion/demotion, views or opens in request.
     *
     * @param localeId
     * @param entity
     * @param blahId
     * @throws InvalidRequestException
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException
     */
    public void updateBlahPromotionViewOrOpens(LocaleId localeId, BlahPayload entity, String blahId) throws InvalidRequestException, StateConflictException, SystemErrorException, ResourceNotFoundException {
        ensureReady();
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

        final Long promotionOrDemotion = CommonUtilities.checkDiscreteValue(entity.getUserPromotion(), entity);

        final long maxViewIncrements = _maxOpensOrViewsPerUpdate;
        final Long viewCount = CommonUtilities.checkValueRange(entity.getViews(), 0L, maxViewIncrements, entity);
        final Long openCount = CommonUtilities.checkValueRange(entity.getOpens(), 0L, maxViewIncrements, entity);
        if (promotionOrDemotion == 0L && viewCount == 0L && openCount == 0L) {
            return; // don't complain
        }

        if (!getStoreManager().createUser(userId)._exists()) {
            throw new ResourceNotFoundException("user not found; userId=" + userId, ErrorCodes.NOT_FOUND_USER_ID);
        }

        final boolean createdComment = false;
        final BlahDAO updateBlahDAO = updateBlahStatsInternal(LocaleId.en_us, blahId, userId, promotionOrDemotion, viewCount, openCount, createdComment);

        // Track it
        final boolean isBlah = true;
        final boolean isNewObject = false;
        final String objectId = blahId;
        final String subObjectId = null;
        final boolean promoted = (promotionOrDemotion.intValue() > 0L);
        final boolean demoted = (promotionOrDemotion.intValue() < 0L);
        getTrackingManager().trackObject(TrackerOperation.UPDATE_BLAH, userId, updateBlahDAO.getAuthorId(), isBlah, isNewObject, objectId, subObjectId, promoted, demoted, null, viewCount, openCount);

        if (doIndex()) {
            indexBlah(updateBlahDAO);
        }
    }

    /**
     * <p>Updates multiple blah view and open counts</p>
     *
     * @param localeId
     * @param userId   The user id who is viewing/opening the blahs, or null if the user is anonymous.
     * @param viewsMap Map: key is a blah id and value is the number of views of that blah
     * @param opensMap Map: key is a blah id and value is the number of opens of that blah
     */
    public void updateBlahCounts(LocaleId localeId, String userId, Map<String, Long> viewsMap, Map<String, Long> opensMap) throws ResourceNotFoundException, SystemErrorException, InvalidRequestException {

        final Map<String, OpenViewCount> blahCountMap = aggregateBlahCounts(viewsMap, opensMap);
        for (Map.Entry<String, OpenViewCount> entry : blahCountMap.entrySet()) {
            final String blahId = entry.getKey();
            final OpenViewCount counts = entry.getValue();

            if (counts.opens > 0 || counts.views > 0) {

                final BlahDAO blahDAO = (BlahDAO) getStoreManager().createBlah(blahId)._findByPrimaryId(BlahDAO.AUTHOR_ID);   // Get blah's author id

                if (blahDAO == null) {
                    logger.warning("Blah id '" + blahId + "' referenced but not found in updateBlahCounts");
                    return;
                }

                if (counts.opens > 0) {
                    blahDAO.put(BlahDAOConstants.OPENS, counts.opens);
                }
                if (counts.views > 0) {
                    blahDAO.put(BlahDAOConstants.VIEWS, counts.views);
                }
                blahDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

                // Update user/blah info and track it
                if (userId != null) {
                    final UserBlahInfoDAO userBlahInfoDAO = getStoreManager().createUserBlahInfo(userId, blahId);
                    final boolean userBlahInfoDAOExists = userBlahInfoDAO._exists();
                    if (counts.opens > 0) {
                        userBlahInfoDAO.put(UserBlahInfoDAOConstants.OPENS, counts.opens);
                    }
                    if (counts.views > 0) {
                        userBlahInfoDAO.put(UserBlahInfoDAOConstants.VIEWS, counts.views);
                    }

                    // Need to upsert due to possible collisions with simultaneous queries
                    final MongoStoreManager store = (MongoStoreManager) getStoreManager();
                    final DBCollection col = store.getCollection(store.getUserBlahInfoCollectionName());
                    if (!userBlahInfoDAOExists) {
                        setRequiredFieldsForUserBlahInfo(userBlahInfoDAO, userId, blahId, blahDAO.getAuthorId(), blahDAO.getTypeId());
                    }
                    final BasicDBObject query = new BasicDBObject(UserBlahInfoDAOConstants.USER_ID, userId);
                    query.put(UserBlahInfoDAOConstants.BLAH_ID, blahId);
                    userBlahInfoDAO._upsert(query);

                }
                getTrackingManager().trackObject(TrackerOperation.UPDATE_BLAH, userId, blahDAO.getAuthorId(), true, false, blahId, null, false, false, null,
                        counts.views, counts.opens);
            }

        }
    }

    private class OpenViewCount {
        private Long opens = 0L;
        private Long views = 0L;
    }

    /** Aggregates the blah's opens and views into one map.
     * TODO: change API so that this is not necessary
     */
    private Map<String, OpenViewCount> aggregateBlahCounts(Map<String, Long> viewsMap, Map<String, Long> opensMap) {
        // map blah id to its open and view counts
        final Map<String, OpenViewCount> blahCountsMap = new HashMap<String, OpenViewCount>();
        aggregateBlahCounts1(blahCountsMap, opensMap, true);
        aggregateBlahCounts1(blahCountsMap, viewsMap, false);
        return blahCountsMap;
    }

    private void aggregateBlahCounts1(Map<String, OpenViewCount> blahCountsMap, Map<String, Long> map, boolean opens) {
        if (map != null && map.size() > 0) {
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                final String blahId = entry.getKey();
                final Long count = entry.getValue();
                if (count > 0) {
                    OpenViewCount openViewCount = blahCountsMap.get(blahId);
                    if (openViewCount == null) {
                        openViewCount = new OpenViewCount();
                        blahCountsMap.put(blahId, openViewCount);
                    }
                    if (opens) {
                        openViewCount.opens++;
                    } else {
                        openViewCount.views++;
                    }
                }
            }
        }
    }


    /**
     * Called when updating a blah's votes|opens|views or when creating a comment.
     * Updates userBlahInfoDAO and blahDAOs.
     *
     * @param localeId
     * @param blahId              The blah's id
     * @param userId              The user's id
     * @param promotionOrDemotion The promotion or demotion (always one of 0, 1, -1)
     * @param viewCount           The view count (always one of 0, 1, -1)
     * @param openCount           The open count (always one of 0, 1, -1)
     * @param creatingComment     True if this is called when a comment is created
     * @return BlahDAO  The blah DAO including the updates plus the author id
     * @throws SystemErrorException
     * @throws ResourceNotFoundException
     * @throws StateConflictException
     * @throws InvalidRequestException
     */
    private BlahDAO updateBlahStatsInternal(LocaleId localeId, String blahId, String userId, Long promotionOrDemotion,
                                       Long viewCount, Long openCount, boolean creatingComment)
            throws SystemErrorException, ResourceNotFoundException, StateConflictException, InvalidRequestException {
        final BlahDAO blahDAO = (BlahDAO) getStoreManager().createBlah(blahId)._findByPrimaryId(BlahDAO.AUTHOR_ID, BlahDAO.GROUP_ID, BlahDAO.TYPE_ID);
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
        if (promotionOrDemotion != 0L) {
            if (!insert && userBlahHistory.getPromotedOrDemoted() != null && userBlahHistory.getPromotedOrDemoted() != 0L) {
                throw new InvalidRequestException("userId=" + userId + " has already voted on blahId=" + blahId, ErrorCodes.USER_ALREADY_VOTED_ON_BLAH_ID);
            }
            userBlahInfoDAO.setPromotedOrDemoted(promotionOrDemotion > 0 ? 1L : -1L);
        }
        if (viewCount != 0L) {
            userBlahInfoDAO.setViews(viewCount);
        }
        if (openCount != 0L) {
            userBlahInfoDAO.setOpens(openCount);
        }
        if (creatingComment) {
            userBlahInfoDAO.setComments(1L);
        }

        userBlahInfoDAO.setGroupId(blahDAO.getGroupId()); // original group id
        if (insert) {
            setRequiredFieldsForUserBlahInfo(userBlahInfoDAO, userId, blahId, authorId, blahDAO.getTypeId());
            userBlahInfoDAO._insert();
        } else {
            userBlahInfoDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
        }

        final BlahDAO blah = getStoreManager().createBlah(blahId);
        if (promotionOrDemotion > 0L) {
            blah.setPromotedCount(1L);
        } else if (promotionOrDemotion < 0L) {
            blah.setDemotedCount(1L);
        }
        if (viewCount != 0L) {
            blah.setViews(viewCount);
        }  // incremental
        if (openCount != 0L) {
            blah.setOpens(openCount);
        }  // incremental
        if (creatingComment) {
            blah.setComments(1L);
        }  // incremental
        blah._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

        blah.setAuthorId(authorId);

        return blah;
    }

    /**
     * Called when updating a blah's votes|opens|views or when creating a comment.
     * Updates userBlahInfoDAO and blahDAOs.
     *
     * @param localeId
     * @param blahId              The blah's id
     * @param userId              The user's id
     * @param entity             The blah payload to update
     * @return BlahDAO  The blah DAO including the updates plus the author id
     * @throws SystemErrorException
     * @throws ResourceNotFoundException
     * @throws StateConflictException
     * @throws InvalidAuthorizedStateException
     * @throws InvalidRequestException
     */
    private BlahDAO updateBlahInternal(LocaleId localeId, String blahId, String userId, BlahPayload entity)
            throws SystemErrorException, ResourceNotFoundException, StateConflictException, InvalidAuthorizedStateException, InvalidRequestException {
        final BlahDAO blahDAO = (BlahDAO) getStoreManager().createBlah(blahId)._findByPrimaryId(BlahDAO.AUTHOR_ID, BlahDAO.GROUP_ID, BlahDAO.TYPE_ID);
        if (blahDAO == null) {
            throw new ResourceNotFoundException("blah not found; blahId=" + blahId, ErrorCodes.NOT_FOUND_BLAH_ID);
        }
        if (CommonUtilities.isEmptyString(userId)) {
            throw new InvalidRequestException("userId is missing", ErrorCodes.MISSING_USER_ID);
        }
        final String authorId = blahDAO.getAuthorId();
        final boolean userIsBlahAuthor = userId.equals(authorId);
        if (!userIsBlahAuthor) {
            throw new InvalidAuthorizedStateException("userId=" + userId + " may not edit blahId=" + blahId, ErrorCodes.UNAUTHORIZED_USER);
        }

        final String typeId = entity.getTypeId();
        if (!isTypeIdValid(typeId)) {
            throw new InvalidRequestException("invalid blah type id '" + typeId + "'", ErrorCodes.MISSING_BLAH_TYPE_ID);
        }
        final boolean isPoll = isBlahTypeCategory(typeId, BlahTypeCategoryType.POLL);
        final boolean isPrediction = (!isPoll && isBlahTypeCategory(typeId, BlahTypeCategoryType.PREDICTION));
        String text = entity.getText();
        if ((isPoll || isPrediction) && CommonUtilities.isEmptyString(text)) {
            throw new InvalidRequestException("Blah text line required for polls and predictions", ErrorCodes.MISSING_TEXT);
        }

        final List<String> mediaIds = entity.getImageIds();
        final boolean hasMedia = (mediaIds != null && mediaIds.size() > 0);
        if (hasMedia) {
            final String mediaId = mediaIds.get(0);
            final MediaDAO mediaDAO = getStoreManager().createMedia(mediaId);
            if (!mediaDAO._exists()) {
                throw new ResourceNotFoundException("Media id '" + mediaId + "' not found", ErrorCodes.MEDIA_NOT_FOUND);
            }
            mediaDAO.setReferendType(MediaReferendType.B.toString());
            mediaDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
        }

        text = cleanupBlahTextString(text);
        if (!CommonUtilities.isEmptyString(text)) {
            if (text.length() > MAXIMUM_BLAH_HEADLINE_LENGTH) {
                throw new InvalidRequestException("Blah text line cannot exceed 1024 chars", ErrorCodes.MAXIMUM_TEXT_FIELD_LENGTH_EXCEEDED);
            }
            entity.setText(text);
        }
        String body = entity.getBody();
        if (!CommonUtilities.isEmptyString(body)) {
            body = cleanupBlahTextString(body);
            if (body.length() > MAXIMUM_BLAH_BODY_LENGTH) {
                throw new InvalidRequestException("Blah body text cannot exceed 1024 chars", ErrorCodes.MAXIMUM_TEXT_FIELD_LENGTH_EXCEEDED);
            }
            entity.setBody(body);
        }

        final String groupId = entity.getGroupId();
        if (CommonUtilities.isEmptyString(groupId)) {
            throw new InvalidRequestException("missing field groupId=" + groupId, ErrorCodes.MISSING_GROUP_ID);
        }

        // Ensure user is active in group
        ensureUserActiveInGroup(authorId, groupId);

        verifyBadges(entity);

        // Create fresh blah to prevent injection
        final BlahDAO blah = getStoreManager().createBlah(blahId);

        blah.addFromMap(entity, true); // removes fields not in schema

        if (hasMedia) {
            blah.setImageIds(mediaIds);
        }
        if (isPoll) {
            addPollData(text, blah);
        } else {
            if (isPrediction) {
                addPredictionData(blah, entity.getExpirationDate());
            }
        }


        blah._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

        return blah;
    }

    private void setRequiredFieldsForUserBlahInfo(UserBlahInfoDAO userBlahInfoDAO, String userId, String blahId, String authorId, String blahTypeId) {
        userBlahInfoDAO.setBlahTypeId(blahTypeId);
        userBlahInfoDAO.setUserId(userId);
        userBlahInfoDAO.setBlahId(blahId);
        userBlahInfoDAO.setAuthorId(authorId);
    }

    public List<BlahPayload> getBlahs(LocaleId localeId, String userId, String authorId, String typeId, Integer start, Integer count, String sortFieldName) throws SystemErrorException, InvalidRequestException {
        ensureReady();
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
        if (count == null || count > _returnedObjectLimit) {
            count = _returnedObjectLimit;
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

    public List<BlahTypePayload> getBlahTypes(LocaleId localeId) throws SystemErrorException {
        ensureReady();
        ensureBlahTypesCached();
        return getBlahTypes();
    }

    private void ensureBlahTypesCached() throws SystemErrorException {
        if (((_lastTimeBlahTypesCached - System.currentTimeMillis()) > TEN_MINUTES_BLAH_TYPE_CACHE_REFRESH_IN_MILLIS)) {
            refreshBlahTypesCache();
        }
    }

    private class BlahTypeEntry {
        private final BlahTypeDAO _blahTypeDAO;
        private final BlahTypeCategoryType _categoryType;

        BlahTypeEntry(BlahTypeDAO blahTypeDAO, BlahTypeCategoryType categoryType) {
            _blahTypeDAO = blahTypeDAO;
            _categoryType = categoryType;
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
        synchronized (_blahTypeIdToBlahTypeEntryMapLock) {
            _blahTypeIdToBlahTypeEntryMap = map;
            _lastTimeBlahTypesCached = System.currentTimeMillis();
        }
        System.out.println(new Date() + ": Blah type cache refreshed");
    }

    public List<BlahTypePayload> getBlahTypes() throws SystemErrorException {
        ensureReady();
        synchronized (_blahTypeIdToBlahTypeEntryMapLock) {
            final List<BlahTypePayload> bt = new ArrayList<BlahTypePayload>(_blahTypeIdToBlahTypeEntryMap.size());
            for (BlahTypeEntry entry : _blahTypeIdToBlahTypeEntryMap.values()) {
                bt.add(new BlahTypePayload(entry._blahTypeDAO.toMap()));
            }
            return bt;
        }
    }

    /**
     * Used for testing purposes: forces refresh of all caches.
     *
     * @throws com.eweware.service.base.error.SystemErrorException
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
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException
     */
    public BlahPayload getBlahById(LocaleId localeId, String blahId, String userId, boolean stats, String statsStartDate, String statsEndDate)
            throws InvalidRequestException, SystemErrorException, ResourceNotFoundException {
        ensureReady();
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
        Calendar startDate = com.eweware.service.base.date.DateUtils.convertToCalendar(statsStartDate);
        Calendar endDate = com.eweware.service.base.date.DateUtils.convertToCalendar(statsEndDate);
//        logger.finer(statsStartDate+" startDate=" + new Date(startDate.getTimeInMillis()));
//        logger.finer(statsEndDate+" endDate=" + new Date(endDate.getTimeInMillis()));

        // We've made sure that the dates can be parsed as expected and are available as calendar instances for comparison
        List<BlahTrackerPayload> trackers = null;
        if (DateUtils.isSameDay(startDate, endDate)) { // fetch single
//            logger.finer("Same day");
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
//            logger.finer("from=" + from + "  to=" + to);
            final boolean sorted = true;
            final List<? extends BaseDAO> trackerDAOs = blahTrackerDAO._findRangeSingleField(sorted, BlahTrackerDAO.ID, from, true, to, true);
            trackers = new ArrayList<BlahTrackerPayload>(trackerDAOs.size());
            for (BaseDAO dao : trackerDAOs) {
                trackers.add(new BlahTrackerPayload(dao.toMap()));
            }
        }
        blahPayload.setStats(trackers == null ? new ArrayList<BlahTrackerPayload>(0) : trackers);
    }

//    public static String extractYearMonthFromTrackerDate(String trackerDate) {
//        return trackerDate.substring(0, 4);
//    }

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
        ensureReady();
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
        ensureReady();
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
     * @throws com.eweware.service.base.error.SystemErrorException
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
     * @param entity          @return
     * @throws InvalidRequestException
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException
     * @throws StateConflictException
     */
    public CommentPayload
    createComment(LocaleId localeId, String commentAuthorId, CommentPayload entity) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException, StateConflictException {
        ensureReady();

        // Check parameters
        if (entity.getCommentVotes() != null) {
            throw new InvalidRequestException("cannot vote for comment when creating it", entity, ErrorCodes.CANNOT_VOTE_ON_COMMENT_WHEN_CREATING_IT);
        }
        final String blahId = entity.getBlahId();
        if (CommonUtilities.isEmptyString(blahId)) {
            throw new InvalidRequestException("missing blah id", entity, ErrorCodes.MISSING_BLAH_ID);
        }
        if (CommonUtilities.isEmptyString(commentAuthorId)) {
            throw new InvalidRequestException("missing authorId", entity, ErrorCodes.MISSING_AUTHOR_ID);
        }
        String text = entity.getText();
        if (CommonUtilities.isEmptyString(text)) {
            throw new InvalidRequestException("missing text", entity, ErrorCodes.MISSING_TEXT);
        }
        text = CommonUtilities.scrapeMarkup(text);
        if (text.length() > MAXIMUM_COMMENT_LENGTH) {
            throw new InvalidRequestException("Comment text length exceeded maximum", ErrorCodes.MAXIMUM_TEXT_FIELD_LENGTH_EXCEEDED);
        }

        final List<String> mediaIds = entity.getImageIds();
        final boolean hasMedia = (mediaIds != null && mediaIds.size() > 0);
        if (hasMedia) {
            final String mediaId = mediaIds.get(0); // assumption: should only have one
            final MediaDAO mediaDAO = getStoreManager().createMedia(mediaId);
            if (!mediaDAO._exists()) {
                throw new ResourceNotFoundException("Media id '" + mediaId + "' not found", ErrorCodes.MEDIA_NOT_FOUND);
            }
            mediaDAO.setReferendType(MediaReferendType.C.toString());
            mediaDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
        }

        getUserManager().checkUserById(commentAuthorId, entity);
        final Long blahVote = CommonUtilities.checkDiscreteValue(entity.getBlahVote(), entity);
        boolean votedForBlah = (blahVote != 0L);

        // Check that blah exists and if this comment includes a vote that the comment author is not the blah's author
        final BlahDAO blahDAO = getStoreManager().createBlah(blahId);
        final BlahDAO blah = (BlahDAO) blahDAO._findByPrimaryId(BlahDAO.AUTHOR_ID);
        if (blah == null) {
            throw new InvalidRequestException("no blahId=" + blahId + " exists", entity, ErrorCodes.INVALID_INPUT);
        }
        if (votedForBlah && blah.getAuthorId().equals(commentAuthorId)) { // Check if comment author is also blah author: voting not allowed
            throw new InvalidRequestException("authorId=" + commentAuthorId + " (author of the blahId=" + blahId + ") cannot vote on own blah", entity, ErrorCodes.USER_CANNOT_UPDATE_ON_OWN_BLAH);
        }

        // Create comment
        CommentDAO commentDAO = getStoreManager().createComment();
        commentDAO.addFromMap(entity, true);
        verifyCommentBadges(entity);
        commentDAO.setBlahId(blahId);
        commentDAO.setText(text);
        commentDAO.setAuthorId(commentAuthorId);
        if (hasMedia) {
            commentDAO.setImageIds(mediaIds);
        }
        if (votedForBlah) {
            commentDAO.setBlahVote(blahVote);
        }
        commentDAO.setStrength(0.0);

        commentDAO.setCreated(new Date());
        commentDAO._insert();

        final long viewCount = 0L;
        final long openCount = 0L;
        final boolean createdComment = true;
        updateBlahStatsInternal(LocaleId.en_us, blahId, commentAuthorId, blahVote, viewCount, openCount, createdComment);

        final boolean isBlah = false;
        final boolean isNewObject = true;
        final String objectId = commentDAO.getId();
        final String subObjectId = blahId;
        final boolean voteUp = (blahVote > 0L);
        final boolean voteDown = (blahVote < 0L);
        getTrackingManager().trackObject(TrackerOperation.CREATE_COMMENT, commentAuthorId, commentAuthorId, isBlah, isNewObject, objectId, subObjectId, voteUp, voteDown, null, entity.getViews(), entity.getOpens());

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
     * @param localeId
     * @param entity    The client request
     * @param userId
     * @param commentId @throws InvalidRequestException
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException
     * @throws StateConflictException
     */
    public void updateComment(LocaleId localeId, CommentPayload entity, String userId, String commentId) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException, StateConflictException {
        ensureReady();
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

        final Long voteForComment = CommonUtilities.checkDiscreteValue(entity.getCommentVotes(), entity);
        final boolean didVoteForComment = (voteForComment != 0L);
        final Long views = CommonUtilities.checkValueRange(entity.getViews(), 0L, _maxOpensOrViewsPerUpdate, entity);
        final Long opens = CommonUtilities.checkValueRange(entity.getOpens(), 0L, _maxOpensOrViewsPerUpdate, entity);
        if (!didVoteForComment &&
                (views == 0L) &&
                (opens == 0L)) {
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
        if (didVoteForComment && foundUserCommentInfo != null && foundUserCommentInfo.getVote() != null && foundUserCommentInfo.getVote() != 0L) {
            throw new InvalidRequestException("authorId=" + userId + " already voted for this comment; commentId=" + commentId, ErrorCodes.USER_ALREADY_VOTED_FOR_COMMENT);
        }
        if (didVoteForComment && commentAuthorId.equals(userId)) {
            throw new InvalidRequestException("comment author cannot vote for own comment; authorId=" + userId + " commentId=" + commentId, ErrorCodes.USER_CANNOT_VOTE_ON_OWN_COMMENT);
        }
        if (foundUserCommentInfo == null) {
            userCommentInfoDAO.initToDefaultValues(localeId);
        }
        if (voteForComment != 0L) {
            userCommentInfoDAO.setVote(voteForComment);
        }
        if (views != 0L) {
            userCommentInfoDAO.setViews(views);
        }
        if (opens != 0L) {
            userCommentInfoDAO.setOpens(opens);
        }
        if (foundUserCommentInfo == null) {
            userCommentInfoDAO.setAuthorId(commentAuthorId);
            userCommentInfoDAO._insert();
        } else {
            userCommentInfoDAO.setId(foundUserCommentInfo.getId());
            userCommentInfoDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
        }

        // Update comment
        final CommentDAO commentUpdateDAO = getStoreManager().createComment(commentId);
        if (voteForComment != 0L) {
            commentUpdateDAO.setCommentVotes(voteForComment);
            if (voteForComment > 0L) {
                commentUpdateDAO.setCommentUpVotes(1L);
            } else {
                commentUpdateDAO.setCommentDownVotes(1L);
            }
        }
        if (views != 0L) {
            commentUpdateDAO.setViews(views);
        }
        if (opens != 0L) {
            commentUpdateDAO.setOpens(opens);
        }
        commentUpdateDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

        final boolean isBlah = false;
        final boolean isNewObject = false;
        final String objectId = commentId;
        final String subObjectId = blahId;
        final boolean voteUp = (voteForComment == 1L);
        final boolean voteDown = (voteForComment == -1L);
        getTrackingManager().trackObject(TrackerOperation.UPDATE_COMMENT, userId, commentAuthorId, isBlah, isNewObject, objectId, subObjectId, voteUp, voteDown, null, views, opens);

        if (doIndex()) {
            indexComment(commentUpdateDAO);
        }
    }

    public CommentPayload getCommentById(LocaleId localeId, boolean authenticated, String commentId, String userId, boolean stats, String statsStartDate, String statsEndDate) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException {
        ensureReady();
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

        // TODO expensive! Use local cache with somewhat short expiration date
        CommonUtilities.maybeAddUserNickname(_storeManager, authenticated, commentDAO.getAuthorId(), entity);

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
        Calendar startDate = com.eweware.service.base.date.DateUtils.convertToCalendar(statsStartDate);
        Calendar endDate = com.eweware.service.base.date.DateUtils.convertToCalendar(statsEndDate);

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
        ensureReady();
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
            CommonUtilities.maybeAddUserNickname(_storeManager, authenticated, dao.getAuthorId(), commentPayload);
            comments.add(commentPayload);
        }
        if (!CommonUtilities.isEmptyString(userId)) {
            for (CommentPayload comment : comments) {
                addUserCommentInfoToPayload(comment, comment.getId(), userId);
            }
        }
        return comments;
    }

    public List<Map<String, Object>> getInboxNew(LocaleId localeId, String groupId, HttpServletRequest request, Integer inboxNumber)
            throws SystemErrorException, InvalidAuthorizedStateException, InvalidRequestException, ResourceNotFoundException, StateConflictException {

        ensureReady();

        if (groupId == null) {
            throw new InvalidRequestException("Missing group id", ErrorCodes.MISSING_GROUP_ID);
        }

        checkGroupAccess(request, groupId);

        final Integer lastInboxNumber = (inboxNumber == null) ? BlahguaSession.getLastInboxNumber(request, groupId) : null;

        final InboxData inbox = _inboxHandler.getNextInbox(groupId, inboxNumber, lastInboxNumber, null);

        BlahguaSession.setLastInboxNumber(request, groupId, inbox.getInboxNumber());

        return inbox.getInboxItems();
    }

    public List<Map<String, Object>> getRecentsInbox(String groupId, HttpServletRequest request)
            throws SystemErrorException, InvalidRequestException, InvalidAuthorizedStateException, StateConflictException, ResourceNotFoundException {

        ensureReady();

        if (groupId == null) {
            throw new InvalidRequestException("Missing group id", ErrorCodes.MISSING_GROUP_ID);
        }

        checkGroupAccess(request, groupId);

        final InboxData inbox = _inboxHandler.getRecentsInbox(groupId, null);

        if (inbox.getInboxItems().size() == 0) {
            logger.warning("Got no recent inbox items for groupId '" + groupId + "'");
        }

        return inbox.getInboxItems();
    }



    /**
     * Deletes a blah.
     *
     * @param en_us
     * @param blahId
     * @param userId
     */
    public void deleteBlah(LocaleId en_us, String blahId, String userId) throws InvalidRequestException, SystemErrorException, InvalidAuthorizedStateException {
        ensureReady();



        final BlahDAO blahDAO = (BlahDAO) getStoreManager().createBlah(blahId)._findByPrimaryId(BlahDAO.AUTHOR_ID);   // Get blah's author id

        if (blahDAO == null) {
            logger.warning("Blah id '" + blahId + "' referenced but not found in deleteBlah");
            return;
        }

        if (!blahDAO.getAuthorId().equals(userId)) {
            throw new InvalidAuthorizedStateException("Unauthorized attempt to delete other user's blah", ErrorCodes.UNAUTHORIZED_USER);
        }

        blahDAO.setStrength(-1.0);

        blahDAO._updateByPrimaryId(DAOUpdateType.ABSOLUTE_UPDATE);

    }




    /**
     * <p>Associates an image with a comment.</p>
     * <p>This method does NOT delete any existing images for the comment.</p>
     *
     * @param commentId The comment id
     * @param mediaId   The media id
     * @throws SystemErrorException
     * @throws ResourceNotFoundException
     * @see com.eweware.service.base.store.dao.type.MediaReferendType
     */
    public void associateImageWithComment(String commentId, String mediaId) throws SystemErrorException, ResourceNotFoundException {
        final CommentDAO comment = getStoreManager().createComment(commentId);
        if (!comment._exists()) {
            throw new ResourceNotFoundException("No comment id '" + commentId + "' exists", ErrorCodes.NOT_FOUND_COMMENT_ID);
        }
        comment.setImageIds(Arrays.asList(new String[]{mediaId}));
        comment._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
    }


    /**
     * <p>Checks whether the group may be accessed in this session.</p>
     * <p>Open groups may be accessed by any user. Other groups require the
     * user to be authenticated (already logged in) and the user must have
     * registered in the group.</p>
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
    private void checkGroupAccess(HttpServletRequest request, String groupId) throws SystemErrorException, InvalidAuthorizedStateException, ResourceNotFoundException, StateConflictException {

        // TODO cache static group info
        if (!_storeManager.createGroup(groupId)._exists()) {
            throw new ResourceNotFoundException("Group id '" + groupId + "' does not exist");
        }
        final boolean isOpenGroup = _groupManager.isOpenGroup(groupId);

        if (!isOpenGroup) {
            String userId = BlahguaSession.ensureAuthenticated(request, true);
            final UserGroupDAO userGroupDAO = getStoreManager().createUserGroup();
            userGroupDAO.setUserId(userId);
            userGroupDAO.setGroupId(groupId);
            userGroupDAO.setState(AuthorizedState.A.toString());
            if (!userGroupDAO._exists()) {
                throw new StateConflictException("user id '" + userId + "' not registered in group. Not authorized to access inbox for groupId=" + groupId, ErrorCodes.USER_HAS_NOT_JOINED_GROUP);
            }
        }
    }

    private void ensureReady() throws SystemErrorException {
        if (_state != ManagerState.STARTED) {
            throw new SystemErrorException("System not ready", ErrorCodes.SERVER_NOT_INITIALIZED);
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
            System.out.println("Creating Zoie index in directory " + _commentIndexDir.getAbsolutePath());
            _commentIndexingSystem = new ZoieSystem<BlahguaFilterIndexReader, CommentDAO>(new DefaultDirectoryManager(_commentIndexDir), new BlahCommentDataIndexableInterpreter(), decorator, config);
            _commentIndexingSystem.start(); // ready to accept indexing events
        }
    }

    private void startBlahIndex() {
        if (doIndex()) {
            final IndexReaderDecorator<BlahguaFilterIndexReader> decorator = new BlahguaIndexReaderDecorator();
            final ZoieConfig config = makeIndexConfiguration();
            System.out.println("Creating Zoie index in directory " + _blahIndexDir.getAbsolutePath());
            _blahIndexingSystem = new ZoieSystem<BlahguaFilterIndexReader, BlahDAO>(new DefaultDirectoryManager(_blahIndexDir), new BlahDataIndexableInterpreter(), decorator, config);
            _blahIndexingSystem.start(); // ready to accept indexing events
        }
    }

    // TODO this can be configured via Spring, but will be factored out to its own service so don't bother yet
    private ZoieConfig makeIndexConfiguration() {
        final ZoieConfig config = new ZoieConfig();
        config.setAnalyzer(new StandardAnalyzer(Version.LUCENE_35));
        config.setSimilarity(new DefaultSimilarity());
        config.setBatchSize(_batchSize);
        config.setBatchDelay(_batchDelay);
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
     * @throws com.eweware.service.base.error.SystemErrorException
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
                _blahIndexingSystem.consume(events);
            } catch (ZoieException e) {
                throw new SystemErrorException("Indexing error", e, ErrorCodes.SERVER_INDEXING_ERROR);
            }
        }
    }

    /**
     * Deletes the blah from the index.
     *
     * @param blahId The blah's id.
     * @throws com.eweware.service.base.error.SystemErrorException
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
     * @throws com.eweware.service.base.error.SystemErrorException
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
                _commentIndexingSystem.consume(events);
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
//     * @throws com.eweware.service.base.error.SystemErrorException
//     *
//     */
//    private void deleteCommentFromIndex(String commentId) throws SystemErrorException {
//        if (CommonUtilities.isEmptyString(commentId)) {
//            throw new SystemErrorException("missing comment id in " + this, ErrorCodes.SERVER_INDEXING_ERROR);
//        }
//        final CommentDAO comment = _storeManager.createComment(commentId);
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
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     */
    public List<BasePayload> getFromIndex(Integer maxResults, ZoieSystem<BlahguaFilterIndexReader, ?> indexingSystem, boolean searchBlahs) throws SystemErrorException {
        ensureReady();
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
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     */
    public List<BasePayload> search(LocaleId localeId, String fieldName, String query, String subset, Integer maxResults) throws SystemErrorException {
        ensureReady();
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
            indexingSystem = searchBlahs ? _blahIndexingSystem : _commentIndexingSystem;

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
            system = isBlahIndex ? _blahIndexingSystem : _commentIndexingSystem;

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


//    public List<InboxBlahPayload> getInbox(LocaleId localeId, String groupId, HttpServletRequest request, Integer inboxNumber,
//                                           String blahTypeId, Integer start, Integer count, String sortFieldName, Integer sortDirection)
//            throws SystemErrorException, InvalidAuthorizedStateException, InvalidRequestException, ResourceNotFoundException, StateConflictException {
//        ensureReady();
//        if (groupId == null) {
//            throw new InvalidRequestException("Missing group id", ErrorCodes.MISSING_GROUP_ID);
//        }
//
//        count = ensureCount(count);
//        if (sortDirection == null || (sortDirection != 1 && sortDirection != -1)) {
//            sortDirection = -1;
//        }
//
//        checkGroupAccess(request, groupId);
//
//        // Cycle through inboxes
//        final Integer maxInbox = _inboxHandler.getMaxInbox(groupId);
//        final Integer unknown = -1;
//        if (maxInbox == unknown) {
//            // we don't know the max: attempt to get the first inbox (getting an inbox from the inbox cache retrieves the max, if any)
//            inboxNumber = 0;
//        } else {
//            if (inboxNumber == null) { // if no inbox number is requested, find last
//                Integer lastInbox = BlahguaSession.getLastInboxNumber(request, groupId);
//                inboxNumber = (lastInbox == null) ? 0 : (++lastInbox);  // if we have last, increment it; else start at first inbox
//                if (inboxNumber >= maxInbox) { // rewind if past the maximum number of inboxes; else go to next
//                    inboxNumber = 0;
//                }
//            }
//        }
//        final Inbox inbox = _inboxHandler.getInboxFromCache(groupId, inboxNumber, blahTypeId, start, count, sortFieldName, sortDirection);
//
//        BlahguaSession.setLastInboxNumber(request, groupId, inboxNumber);
//
//        if (inbox == null) {
//            logger.warning("Got no mailbox for groupId '" + groupId + "' inbox #" + inboxNumber + " when maxInbox=" + maxInbox);
//            return new ArrayList<InboxBlahPayload>(0);
//        }
//
//
//        return inbox.getItems();
//    }
