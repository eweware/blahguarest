package main.java.com.eweware.service.mgr;

import main.java.com.eweware.service.GeneralUtilities;
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
import main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import main.java.com.eweware.service.base.type.TrackerType;
import main.java.com.eweware.service.mgr.aux.InboxHandler;
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

import javax.xml.ws.WebServiceException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author rk@post.harvard.edu
 *         <p/>
 *         TODO add monitors for mutating variables or lists with mutating elements
 *         TODO add transaction-level management (rollbacks)
 */
public final class BlahManager implements ManagerInterface {

    private boolean debug;
    private final File blahIndexDir;
    private final File commentIndexDir;
    private final int batchSize;
    private final long batchDelay;
    private final int maxOpensOrViewsPerUpdate;
    private final InboxHandler inboxHandler;
    private Map<String, Boolean> blahTypeIdCheckCache;
    private List<BlahTypePayload> blahTypesCache;
    private long lastTimeBlahTypesCached;
    private ZoieSystem<BlahguaFilterIndexReader, BlahDAO> blahIndexingSystem;
    private ZoieSystem<BlahguaFilterIndexReader, CommentDAO> commentIndexingSystem;

    private ManagerState status;
    public static BlahManager singleton;
    private StoreManager storeManager;
    private final Integer returnedObjectLimit;

    public static BlahManager getInstance() throws SystemErrorException {
        if (BlahManager.singleton == null) {
            throw new SystemErrorException("blah manager not initialized", ErrorCodes.SERVER_NOT_INITIALIZED);
        }
        return BlahManager.singleton;
    }

    public BlahManager(Boolean debug, String blahIndexDir, String commentIndexDir,
                       String batchSize, String batchDelay, int maxOpensOrViewsPerUpdate,
                       Integer returnedObjectLimit) {
        this.debug = (debug == Boolean.TRUE);
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

        System.out.println("*** BlahManager initialized ***");
    }

    public void start() {

        try {
            storeManager = MongoStoreManager.getInstance(); // TODO abstract this

            initializeBlahIndex();

            this.status = ManagerState.STARTED;
            System.out.println("*** BlahManager started ***");

        } catch (Exception e) {
            throw new WebServiceException(e);
        }
    }

    public void shutdown() {
        blahIndexingSystem.shutdown();
        commentIndexingSystem.shutdown();
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
     * @param request
     * @return BlahPayload A blah payload including the new blah id
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws InvalidRequestException
     * @throws ResourceNotFoundException
     */
    public BlahPayload createBlah(LocaleId localeId, BlahPayload request) throws SystemErrorException, InvalidRequestException, ResourceNotFoundException, StateConflictException {
        final String authorId = request.getAuthorId();
        if (isEmptyString(authorId)) {
            throw new InvalidRequestException("missing field authorId=" + authorId, request, ErrorCodes.MISSING_USER_ID);
        }
        final String text = request.getText();
        if (isEmptyString(text)) {
            throw new InvalidRequestException("missing field text=" + text, request, ErrorCodes.MISSING_TEXT);
        }
        final String groupId = request.getGroupId();
        if (isEmptyString(groupId)) {
            throw new InvalidRequestException("missing field groupId=" + groupId, ErrorCodes.MISSING_GROUP_ID);
        }
        final String typeId = request.getTypeId();
        if (!isTypeIdValid(typeId)) {
            throw new InvalidRequestException("invalid field typeId=" + typeId, ErrorCodes.MISSING_BLAH_TYPE_ID);
        }

        // Ensure user is active in group
        UserGroupDAO userGroupDAO = storeManager.createUserGroup();
        userGroupDAO.setUserId(authorId);
        userGroupDAO.setGroupId(groupId);
        userGroupDAO = (UserGroupDAO) userGroupDAO._findByCompositeId(new String[]{UserGroupDAO.STATE}, UserGroupDAO.USER_ID, UserGroupDAO.GROUP_ID);
        if (userGroupDAO == null || !userGroupDAO.getState().equals(AuthorizedState.A.toString())) {
            throw new StateConflictException("userId=" + authorId + " is not active in groupId=" + groupId, ErrorCodes.USER_NOT_ACTIVE_IN_GROUP);
        }

        // Create fresh blah to prevent injection
        final BlahDAO blahDAO = storeManager.createBlah();
        blahDAO.initToDefaultValues(localeId);
        blahDAO.addFromMap(request, true);
        blahDAO._insert();

        // Update group count TODO maybe either rely on tracker and take the hit in the query OR insert a record into a stream and count offline on a processor?
        final GroupDAO groupDAO = storeManager.createGroup(groupId);
        groupDAO.setBlahCount(1);
        groupDAO._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);

        // Add to inboxes
        inboxHandler.spreadBlah(LocaleId.en_us, blahDAO, groupId);

        // Track it
        final boolean isBlah = true;
        final boolean isNewObject = true;
        final String objectId = blahDAO.getId();
        final String subObjectId = null;
        final boolean voteUp = false;
        final boolean voteDown = false;
        final Integer viewCount = null;
        final Integer openCount = null;
        TrackingManager.getInstance().trackObject(TrackerOperation.CREATE_BLAH, authorId, authorId, isBlah, isNewObject, objectId, subObjectId, voteUp, voteDown, viewCount, openCount);

//        final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.CREATE_BLAH);
//        tracker.setBlahId(blahDAO.getId());
//        tracker.setUserId(authorId);
//        tracker.setBlahAuthorId(authorId);
//        tracker.setGroupId(groupId);
//        TrackingManager.getInstance().track(LocaleId.en_us, tracker);

        indexBlah(blahDAO); // index new blah

        return new BlahPayload(blahDAO);
    }

    private boolean isTypeIdValid(String typeId) throws InvalidRequestException, SystemErrorException {
        if (isEmptyString(typeId)) {
            throw new InvalidRequestException("missing typeId");
        }
        ensureBlahTypesCached();
        return blahTypeIdCheckCache.get(typeId) != null;
    }

    /**
     * Allows a user to update a blah's vote, views, and opens, in any combination.
     * A blah's text may not be edited.
     * Ignored if there is no vote, views or opens in request.
     * <p/>
     * TODO check injection problems: e.g., authorId changed, etc...
     * <p/>
     * Transaction cost:
     * 1. check that user exists
     * 2. get blah to check existence, with blah's authorId to check whether user authored blah
     * 3. Check whether user already voted for blah
     * 4. If vote, update or insert vote
     * 5. If view, update or insert view
     * 6. If open, update or insert open
     * 7. If vote, view, or open, update blah
     *
     * @param localeId
     * @param request
     * @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException
     */
    public void updateBlah(LocaleId localeId, BlahPayload request) throws InvalidRequestException, StateConflictException, SystemErrorException, ResourceNotFoundException {
        if (!isEmptyString(request.getText()) || !isEmptyString(request.getBody())) {
            throw new InvalidRequestException("user may not edit blah text or body", request, ErrorCodes.CANNOT_EDIT_TEXT);
        }
        final String blahId = request.getId();
        if (isEmptyString(blahId)) {
            throw new InvalidRequestException("missing blah id", request, ErrorCodes.MISSING_BLAH_ID);
        }
        final String userId = request.getAuthorId();
        if (isEmptyString(userId)) {
            throw new InvalidRequestException("missing update user id", request, ErrorCodes.MISSING_AUTHOR_ID);
        }

        final Integer vote = GeneralUtilities.checkDiscreteValue(request.getVotes(), request);
        final int maxViewIncrements = maxOpensOrViewsPerUpdate;
        final Integer viewCount = GeneralUtilities.checkValueRange(request.getViews(), 0, maxViewIncrements, request);
        final Integer openCount = GeneralUtilities.checkValueRange(request.getOpens(), 0, maxViewIncrements, request);

        if (vote == 0 && viewCount == 0 && openCount == 0) {
            return; // don't complain
        }

        if (!storeManager.createUser(userId)._exists()) {
            throw new ResourceNotFoundException("user not found; userId=" + userId, ErrorCodes.NOT_FOUND_USER_ID);
        }

        final boolean createdComment = false;
        final BlahDAO updateBlahDAO = updateBlahInternal(LocaleId.en_us, blahId, userId, vote, viewCount, openCount, createdComment);

        // Track it
        final boolean isBlah = true;
        final boolean isNewObject = false;
        final String objectId = blahId;
        final String subObjectId = null;
        final boolean voteUp = (vote.intValue() > 0);
        final boolean voteDown = (vote.intValue() < 0);
        TrackingManager.getInstance().trackObject(TrackerOperation.UPDATE_BLAH, userId, updateBlahDAO.getAuthorId(), isBlah, isNewObject, objectId, subObjectId, voteUp, voteDown, viewCount, openCount);

        indexBlah(updateBlahDAO);

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
//        TrackingManager.getInstance().track(LocaleId.en_us, tracker);
    }

    /**
     * Called when updating a blah or creating a comment.
     * Updates userBlahInfoDAO and blahDAOs.
     *
     * @param localeId
     * @param blahId          The blah's id
     * @param userId          The user's id
     * @param vote            The vote (always 0|1|-2)
     * @param viewCount       The view count (always 0|1|-2)
     * @param openCount       The open count (always 0|1|-2)
     * @param creatingComment True if this is called when a comment is created
     * @return BlahDAO  The blah DAO including the updates plus the author id
     * @throws SystemErrorException
     * @throws ResourceNotFoundException
     * @throws StateConflictException
     * @throws InvalidRequestException
     */
    private BlahDAO updateBlahInternal(LocaleId localeId, String blahId, String userId, Integer vote, Integer viewCount, Integer openCount, boolean creatingComment) throws SystemErrorException, ResourceNotFoundException, StateConflictException, InvalidRequestException {
        BlahDAO blahDAO = storeManager.createBlah(blahId);
        blahDAO = (BlahDAO) blahDAO._findByPrimaryId(BlahDAO.AUTHOR_ID, BlahDAO.GROUP_ID, BlahDAO.TYPE_ID);
        if (blahDAO == null) {
            throw new ResourceNotFoundException("blah not found; blahId=" + blahId, ErrorCodes.NOT_FOUND_BLAH_ID);
        }
        if (isEmptyString(userId)) {
            throw new InvalidRequestException("userId is missing", ErrorCodes.MISSING_USER_ID);
        }
        final String authorId = blahDAO.getAuthorId();
        final boolean userIsBlahAuthor = userId.equals(authorId);
        if (userIsBlahAuthor && (vote != 0)) {
            throw new StateConflictException("userId=" + userId + " may not vote on own blahId=" + blahId, ErrorCodes.USER_CANNOT_UPDATE_ON_OWN_BLAH);
        }

        final String[] fieldsToReturnHint = new String[]{UserBlahInfoDAOConstants.VOTE};
        final UserBlahInfoDAO userBlahHistory = (UserBlahInfoDAO) storeManager.createUserBlahInfo(userId, blahId)._findByCompositeId(fieldsToReturnHint, UserBlahInfoDAOConstants.USER_ID, UserBlahInfoDAOConstants.BLAH_ID);
        final boolean insert = (userBlahHistory == null);

        final UserBlahInfoDAO userBlahInfoDAO = storeManager.createUserBlahInfo();
        if (insert) {
            userBlahInfoDAO.initToDefaultValues(localeId);
        } else {
            userBlahInfoDAO.setId(userBlahHistory.getId());
        }
        if (vote != 0) {
            if (!insert && userBlahHistory.getVote() != null && userBlahHistory.getVote() != 0) {
                throw new InvalidRequestException("userId=" + userId + " has already voted on blahId=" + blahId, ErrorCodes.USER_ALREADY_VOTED_ON_BLAH_ID);
            }
            userBlahInfoDAO.setVote(vote > 0 ? 1 : -1);
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

        final BlahDAO blah = storeManager.createBlah(blahId);
        if (vote != 0) {
            blah.setVotes(vote);
        } // incremental
        if (vote > 0) {
            blah.setUpVotes(1);
        } else if (vote < 0) {
            blah.setDownVotes(1);
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
        if (isEmptyString(blahId)) {
            throw new InvalidRequestException("missing blah id", ErrorCodes.MISSING_BLAH_ID);
        }

        // Fetch comment ids
        final CommentDAO searchCommentDAO = storeManager.createComment();
        searchCommentDAO.setBlahId(blahId);

        // Fetch comments to delete them from index
        final List<CommentDAO> commentDAOs = (List<CommentDAO>) searchCommentDAO._findManyByCompositeId(null, null, null, new String[]{CommentDAO.ID}, CommentDAO.BLAH_ID);

        // Delete comments and blahs
        searchCommentDAO._deleteByCompositeId(CommentDAO.BLAH_ID); // multiple deletes
        final BlahDAO blah = storeManager.createBlah(blahId);
        decrementGroupBlahCount(blah);
        blah._deleteByPrimaryId();


        // Delete comments and blahs from index
        for (CommentDAO commentDAO : commentDAOs) {
            deleteCommentFromIndex(commentDAO);
        }
        deleteBlahFromIndex(blahId); // TODO when queued, this will automatically delete dependent comments
    }

    private void decrementGroupBlahCount(BlahDAO blah) throws SystemErrorException {
        final BlahDAO blahDAO = (BlahDAO) blah._findByPrimaryId(BlahDAO.GROUP_ID);
        final String groupId = blahDAO.getGroupId();
        final GroupDAO group = storeManager.createGroup(groupId);
        group.setBlahCount(-1);
        group._updateByPrimaryId(DAOUpdateType.INCREMENTAL_DAO_UPDATE);
    }

    public List<BlahPayload> getBlahs(LocaleId localeId, String userId, String authorId, String typeId, Integer start, Integer count, String sortFieldName) throws SystemErrorException, InvalidRequestException {
        count = ensureCount(count);
        final BlahDAO blahDAO = storeManager.createBlah();
        final boolean hasAuthor = !isEmptyString(authorId);
        if (hasAuthor) {
            blahDAO.setAuthorId(authorId);
        }
        final boolean hasType = !isEmptyString(typeId);
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
        final boolean hasUserId = !isEmptyString(userId);
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
        final List<? extends BaseDAO> daos = storeManager.createBlahType()._findMany(null, null, null);
        blahTypesCache = new ArrayList<BlahTypePayload>(daos.size());
        for (BaseDAO dao : daos) {
            blahTypesCache.add(new BlahTypePayload(dao.toMap()));
        }
        return blahTypesCache;
    }

    private void ensureBlahTypesCached() throws SystemErrorException {
        long now = System.currentTimeMillis();
        final int TEN_MINUTES = 1000 * 60 * 10;
        if (blahTypesCache == null || ((lastTimeBlahTypesCached - now) > TEN_MINUTES)) {
            refreshBlahTypesCache(now);
        }
    }

    private void refreshBlahTypesCache() throws SystemErrorException {
        refreshBlahTypesCache(System.currentTimeMillis());
    }

    private void refreshBlahTypesCache(long now) throws SystemErrorException {
        final List<BlahTypeDAO> blahTypeDAOs = (List<BlahTypeDAO>) storeManager.createBlahType()._findMany(null, null, null);
        blahTypesCache = new ArrayList<BlahTypePayload>(blahTypeDAOs.size());
        blahTypeIdCheckCache = new HashMap<String, Boolean>(blahTypeDAOs.size());
        for (BlahTypeDAO dao : blahTypeDAOs) {
            blahTypesCache.add(new BlahTypePayload(dao.toMap()));
            blahTypeIdCheckCache.put(dao.getId(), Boolean.TRUE);
        }
        lastTimeBlahTypesCached = now;
        System.out.println(new Date() + ": Blah type cache refreshed");
    }

    public List<BlahTypePayload> getCachedBlahTypes() {
        return blahTypesCache;
    }

    /**
     * Used for testing purposes: refresh all caches.
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
    public BlahPayload getBlahById(LocaleId localeId, String blahId, String userId, boolean stats, String statsStartDate, String statsEndDate) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException {
        if (isEmptyString(blahId)) {
            throw new InvalidRequestException("missing blah id", ErrorCodes.MISSING_BLAH_ID);
        }
        final boolean includeUserInfo = !isEmptyString(userId);

        if (includeUserInfo && !storeManager.createUser(userId)._exists()) {
            throw new InvalidRequestException("userId=" + userId + " not found", ErrorCodes.NOT_FOUND_USER_ID);
        }
        final BlahDAO blahDAO = storeManager.createBlah();
        blahDAO.setId(blahId);
        final BaseDAO found = blahDAO._findByPrimaryId();
        if (found == null) {
            throw new ResourceNotFoundException("blah not found", "blahId=" + blahId, ErrorCodes.NOT_FOUND_BLAH_ID);
        }
        // TODO both fetches below are unacceptably inefficient
        final BlahPayload blahPayload = new BlahPayload(found);
        if (includeUserInfo) {
            addUserBlahInfoToPayload(userId, blahId, blahPayload);
        }
        if (stats) {
            fetchAndAddBlahTrackers(blahId, statsStartDate, statsEndDate, blahPayload);
        }
        return blahPayload;
    }

    private void fetchAndAddBlahTrackers(String blahId, String statsStartDate, String statsEndDate, BlahPayload blahPayload) throws InvalidRequestException, SystemErrorException {
        if (statsStartDate == null && statsEndDate != null) {
            throw new InvalidRequestException("stats start date (s) must be provided if an end date (e) is specified", ErrorCodes.INVALID_INPUT);
        }

        // blah tracker ids are: <blahId><2-digit year><2-digit month><2-digit day of month> (e.g., 5031b25d036408e9b4160b95120820)
        Calendar startDate = main.java.com.eweware.service.base.date.DateUtils.convertToCalendar(statsStartDate);
        Calendar endDate = main.java.com.eweware.service.base.date.DateUtils.convertToCalendar(statsEndDate);
        // We've made sure that the dates can be parsed as expected and are available as calendar instances for comparison
        List<BlahTrackerPayload> trackers = null;
        if (DateUtils.isSameDay(startDate, endDate)) { // fetch single
            final String trackerId = TrackingManager.makeTrackerIdExternal(TrackerType.BLAH, blahId, startDate);
            final BlahTrackerDAO blahTrackerDAO = (BlahTrackerDAO) storeManager.createBlahTracker(trackerId)._findByPrimaryId();
            if (blahTrackerDAO != null) {
                trackers = new ArrayList<BlahTrackerPayload>(1);
                trackers.add(new BlahTrackerPayload(blahTrackerDAO.toMap()));
            }
        } else { // range search
            final BlahTrackerDAO blahTrackerDAO = (BlahTrackerDAO) storeManager.createBlahTracker();
            final String from = extractYearMonthFromTrackerDate(statsStartDate);
            final String to = extractYearMonthFromTrackerDate(statsEndDate);
            final boolean sorted = true;
            // TODO is this correct? WRS-55 ... double-check date format of ID
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
        final UserBlahInfoDAO userBlahDAO = (UserBlahInfoDAO) storeManager.createUserBlahInfo(userId, blahId)._findByCompositeId(
                new String[]{UserBlahInfoDAO.VOTE, UserBlahInfoDAO.VIEWS, UserBlahInfoDAO.OPENS},
                UserBlahInfoDAO.USER_ID, UserBlahInfoDAO.BLAH_ID);
        if (userBlahDAO != null) {
            blahPayload.setUserVote(userBlahDAO.getVote());
            blahPayload.setUserViews(userBlahDAO.getViews());
            blahPayload.setUserOpens(userBlahDAO.getOpens());
        }
    }

    public String getAuthorIdForBlah(String blahId) throws SystemErrorException, ResourceNotFoundException {
        if (isEmptyString(blahId)) {
            throw new SystemErrorException("missing blah id", ErrorCodes.MISSING_BLAH_ID);
        }
        BlahDAO blahDAO = storeManager.createBlah(blahId);
        blahDAO = (BlahDAO) blahDAO._findByPrimaryId(BlahDAO.AUTHOR_ID);
        if (blahDAO == null) {
            throw new ResourceNotFoundException("did not find blahId=" + blahId, ErrorCodes.NOT_FOUND_BLAH_ID);
        }
        return blahDAO.getAuthorId();
    }


    public String getAuthorIdForComment(String commentId) throws SystemErrorException, ResourceNotFoundException {
        if (isEmptyString(commentId)) {
            throw new SystemErrorException("missing comment id", ErrorCodes.MISSING_COMMENT_ID);
        }
        CommentDAO commentDAO = storeManager.createComment(commentId);
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
        if (isEmptyString(blahId)) {
            throw new SystemErrorException("missing blah id", entity, ErrorCodes.MISSING_BLAH_ID);
        }
        if (!storeManager.createBlah(blahId)._exists()) {
            throw new ResourceNotFoundException("not found blahId=" + blahId, entity, ErrorCodes.NOT_FOUND_BLAH_ID);
        }
    }

    /**
     * When a comment is created, it may optionally include a vote for the blah upon which
     * it comments. The author of the blah is permitted to comment on it, but his vote is ignored.
     *
     * @param localeId
     * @param request
     * @return
     * @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException
     * @throws StateConflictException
     */
    public CommentPayload createComment(LocaleId localeId, CommentPayload request) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException, StateConflictException {
        if (request.getCommentVotes() != null) {
            throw new InvalidRequestException("cannot vote for comment when creating it", request, ErrorCodes.CANNOT_VOTE_ON_COMMENT_WHEN_CREATING_IT);
        }
        final String blahId = request.getBlahId();
        if (isEmptyString(blahId)) {
            throw new InvalidRequestException("missing blah id", request, ErrorCodes.MISSING_BLAH_ID);
        }
        final String commentAuthorId = request.getAuthorId();
        if (isEmptyString(commentAuthorId)) {
            throw new InvalidRequestException("missing authorId", request, ErrorCodes.MISSING_AUTHOR_ID);
        }
        final String text = request.getText();
        if (isEmptyString(text)) {
            throw new InvalidRequestException("missing text", request, ErrorCodes.MISSING_TEXT);
        }
        final Integer blahVote = GeneralUtilities.checkDiscreteValue(request.getBlahVote(), request);
        boolean votedForBlah = (blahVote != 0);
        // Check existence of user and blah
        UserManager.getInstance().checkUserById(commentAuthorId, request);
        final BlahDAO blahDAO = storeManager.createBlah(blahId);
        final BlahDAO blah = (BlahDAO) blahDAO._findByPrimaryId(BlahDAO.AUTHOR_ID);
        if (blah == null) {
            throw new InvalidRequestException("no blahId=" + blahId + " exists", request, ErrorCodes.INVALID_INPUT);
        }
        if (votedForBlah && blah.getAuthorId().equals(commentAuthorId)) { // Check if comment author is also blah author: voting not allowed
            throw new InvalidRequestException("authorId=" + commentAuthorId + " (author of the blahId=" + blahId + ") cannot vote on own blah", request, ErrorCodes.USER_CANNOT_UPDATE_ON_OWN_BLAH);
        }

        // Create comment
        CommentDAO commentDAO = storeManager.createComment();
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
        TrackingManager.getInstance().trackObject(TrackerOperation.CREATE_COMMENT, commentAuthorId, commentAuthorId, isBlah, isNewObject, objectId, subObjectId, voteUp, voteDown, request.getViews(), request.getOpens());

//        final TrackerDAO tracker = storeManager.createTracker(TrackerOperation.CREATE_COMMENT);
//        tracker.setBlahAuthorId(blahAuthorId);
//        tracker.setCommentAuthorId(commentAuthorId);
//        tracker.setUserId(commentAuthorId);
//        tracker.setBlahId(blahId);
//        if (blahVote != 0) {
//            tracker.setVote(blahVote);
//        }
//        TrackingManager.getInstance().track(LocaleId.en_us, tracker);

        indexComment(commentDAO); // index new comment

        return new CommentPayload(commentDAO);
    }

    /**
     * The following fields are tracked: votes for comment (blah vote can't be changed after a comment is created),
     * and number of times comment was viewed or opened.
     * <p/>
     * TODO check injection problems: e.g., blahId or authorId changed, etc...
     *
     * @param localeId
     * @param request  The client request
     * @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException
     * @throws StateConflictException
     */
    public void updateComment(LocaleId localeId, CommentPayload request) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException, StateConflictException {

        if (!isEmptyString(request.getText())) {
            throw new InvalidRequestException("user may not edit comment text", request, ErrorCodes.CANNOT_EDIT_TEXT);
        }
        final String commentId = request.getId();
        if (isEmptyString(commentId)) {
            throw new InvalidRequestException("missing comment id", request, ErrorCodes.MISSING_COMMENT_ID);
        }
        final String userId = request.getAuthorId();
        if (isEmptyString(userId)) {
            throw new InvalidRequestException("missing user id", request, ErrorCodes.MISSING_AUTHOR_ID);
        }
        if (request.getBlahVote() != null) {
            throw new InvalidRequestException("user cannot vote on blah when updating a comment; userId=" +
                    request.getAuthorId() + " commentId=" + request.getId() + "blahId=" + request.getBlahId(), ErrorCodes.CANNOT_VOTE_ON_BLAH_WHEN_UPDATING_COMMENT);
        }

        final Integer voteForComment = GeneralUtilities.checkDiscreteValue(request.getCommentVotes(), request);
        final boolean didVoteForComment = (voteForComment != 0);
        final Integer views = GeneralUtilities.checkValueRange(request.getViews(), 0, maxOpensOrViewsPerUpdate, request);
        final Integer opens = GeneralUtilities.checkValueRange(request.getOpens(), 0, maxOpensOrViewsPerUpdate, request);
        if (!didVoteForComment &&
                (views == 0) &&
                (opens == 0)) {
            return; // nothing to update
        }

        UserManager.getInstance().checkUserById(userId, request);

        final CommentDAO commentDAO = (CommentDAO) storeManager.createComment(commentId)._findByPrimaryId(CommentDAO.BLAH_ID, CommentDAO.AUTHOR_ID);
        if (commentDAO == null) {
            throw new ResourceNotFoundException("No commentId=" + commentId, request, ErrorCodes.NOT_FOUND_COMMENT_ID);
        }
        final String blahId = commentDAO.getBlahId();
        final String commentAuthorId = commentDAO.getAuthorId();

        BlahDAO blahDAO = storeManager.createBlah(blahId);
        blahDAO = (BlahDAO) blahDAO._findByPrimaryId(BlahDAO.AUTHOR_ID);
        if (blahDAO == null) {
            throw new ResourceNotFoundException("no blahId=" + blahId + " exists to comment upon; commentId=" + commentId, ErrorCodes.NOT_FOUND_BLAH_ID);
        }
        final String blahAuthorId = blahDAO.getAuthorId();
        if (didVoteForComment && blahAuthorId.equals(userId)) {
            throw new InvalidRequestException("authorId=" + userId + " (author of the blahId=" + blahId + ") cannot vote on comments to author's own blah", request, ErrorCodes.USER_CANNOT_VOTE_ON_COMMENTS_TO_ONES_OWN_BLAH);
        }
        final UserCommentInfoDAO userCommentInfoDAO = storeManager.createUserCommentInfo(userId, commentId);
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
        final CommentDAO commentUpdateDAO = storeManager.createComment(commentId);
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
        TrackingManager.getInstance().trackObject(TrackerOperation.UPDATE_COMMENT, userId, commentAuthorId, isBlah, isNewObject, objectId, subObjectId, voteUp, voteDown, views, opens);

        indexComment(commentUpdateDAO);

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
//        TrackingManager.getInstance().track(LocaleId.en_us, tracker);
    }

    // TODO draconic: should archive them if needed, though old comments should really just fade away
    public void deleteComment(LocaleId localeId, String commentId) throws InvalidRequestException, SystemErrorException {
        if (isEmptyString(commentId)) {
            throw new InvalidRequestException("missing comment id", ErrorCodes.MISSING_COMMENT_ID);
        }
        final CommentDAO commentDAO = storeManager.createComment(commentId);
        commentDAO._deleteByPrimaryId();

        deleteCommentFromIndex(commentDAO);
    }

    public CommentPayload getCommentById(LocaleId localeId, String commentId, String userId, boolean stats, String statsStartDate, String statsEndDate) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException {
        if (isEmptyString(commentId)) {
            throw new InvalidRequestException("missing comment id", ErrorCodes.MISSING_COMMENT_ID);
        }
        final CommentDAO commentDAO = (CommentDAO) storeManager.createComment(commentId)._findByPrimaryId();
        if (commentDAO == null) {
            throw new ResourceNotFoundException("blah comment not found", "commentId=" + commentId, ErrorCodes.NOT_FOUND_COMMENT_ID);
        }
        final CommentPayload comment = new CommentPayload(commentDAO);
        if (!isEmptyString(userId)) {
            addUserCommentInfoToPayload(comment, commentId, userId);
        }
        if (stats) {
            fetchAndAddCommentTrackers(statsStartDate, statsEndDate, comment);
        }
        return comment;
    }

    private void addUserCommentInfoToPayload(CommentPayload comment, String commentId, String userId) throws SystemErrorException {
        final UserCommentInfoDAO dao = (UserCommentInfoDAO) storeManager.createUserCommentInfo(userId, commentId)._findByCompositeId(
                new String[]{UserCommentInfoDAO.VOTE, UserCommentInfoDAO.VIEWS, UserCommentInfoDAO.OPENS},
                UserCommentInfoDAO.USER_ID, UserCommentInfoDAO.COMMENT_ID);
        if (dao != null) {
            comment.setUserVote(dao.getVote());
            comment.setUserViews(dao.getViews());
            comment.setUserOpens(dao.getOpens());
        }
    }

    private void fetchAndAddCommentTrackers(String statsStartDate, String statsEndDate, CommentPayload comment) throws InvalidRequestException, SystemErrorException {
        if (statsStartDate == null && statsEndDate != null) {
            throw new InvalidRequestException("stats start date (s) must be provided if an end date (e) is specified", ErrorCodes.INVALID_INPUT);
        }

        // comment tracker ids are: <commentId><2-digit year><2-digit month><2-digit day of month> (e.g., 5031b25d036408e9b4160b95120820)
        Calendar startDate = main.java.com.eweware.service.base.date.DateUtils.convertToCalendar(statsStartDate);
        Calendar endDate = main.java.com.eweware.service.base.date.DateUtils.convertToCalendar(statsEndDate);
        // We've made sure that the dates can be parsed as expected and are available as calendar instances for comparison
        List<CommentTrackerPayload> trackers = null;
        if (DateUtils.isSameDay(startDate, endDate)) { // fetch single
            final String trackerId = TrackingManager.makeTrackerIdExternal(TrackerType.COMMENT, comment.getId(), startDate);
            final CommentTrackerDAO commentTrackerDAO = (CommentTrackerDAO) storeManager.createCommentTracker(trackerId)._findByPrimaryId();
            if (commentTrackerDAO != null) {
                trackers = new ArrayList<CommentTrackerPayload>(1);
                trackers.add(new CommentTrackerPayload(commentTrackerDAO.toMap()));
            }
        } else { // range search
            final CommentTrackerDAO commentTrackerDAO = (CommentTrackerDAO) storeManager.createCommentTracker();
            final String from = extractYearMonthFromTrackerDate(statsStartDate);
            final String to = extractYearMonthFromTrackerDate(statsEndDate);
            final boolean sorted = true;
            // TODO WRS-55 check date field in this search:
            final List<? extends BaseDAO> trackerDAOs = commentTrackerDAO._findRangeSingleField(sorted, CommentTrackerDAO.ID, from, true, to, true);
            trackers = new ArrayList<CommentTrackerPayload>(trackerDAOs.size());
            for (BaseDAO dao : trackerDAOs) {
                trackers.add(new CommentTrackerPayload(dao.toMap()));
            }
        }
        comment.setStats(trackers == null ? new ArrayList<CommentTrackerPayload>(0) : trackers);

    }

    public List<CommentPayload> getComments(LocaleId localeId, String blahId, String userId, String authorId, Integer start, Integer count, String sortFieldName) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException {
        count = ensureCount(count);
        final boolean forBlah = !isEmptyString(blahId);
        if (forBlah) {
            checkBlahById(blahId, blahId);
        }
        final CommentDAO commentDAO = storeManager.createComment();
        if (forBlah) {
            commentDAO.setBlahId(blahId);
        }
        final boolean forAuthor = !isEmptyString(authorId);
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
            comments.add(new CommentPayload(dao));
        }
        if (!isEmptyString(userId)) {
            for (CommentPayload comment : comments) {
                addUserCommentInfoToPayload(comment, comment.getId(), userId);
            }
        }
        return comments;
    }

    /**
     * Returns a user inbox for this user. The blahs in this inbox
     * contain user-specific stats (opens/views/votes) for
     * the blah rather than total stats for the blah itself.
     *
     * @param localeId
     * @param userId        The user id
     * @param groupId       Optional group id for inbox. If not provided, picks
     *                      an arbitrary group in which user is active.
     * @param inboxNumber
     * @param type
     * @param start
     * @param count         @return
     * @param sortFieldName
     * @param sortDirection @throws InvalidRequestException
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     * @throws ResourceNotFoundException
     * @throws StateConflictException
     */
    public List<InboxBlahPayload> getUserInbox(LocaleId localeId, String userId, String groupId, Integer inboxNumber, String type, Integer start, Integer count, String sortFieldName, Integer sortDirection) throws InvalidRequestException, SystemErrorException, ResourceNotFoundException, StateConflictException {
        count = ensureCount(count);
        if (sortDirection == null) {
            sortDirection = -1;
        } else if (Math.abs(sortDirection) != 1) {
            sortDirection = (sortDirection < 0) ? -1 : 1;
        }
        if (isEmptyString(userId)) {
            throw new InvalidRequestException("missing user id", ErrorCodes.MISSING_USER_ID);
        }
        final UserDAO userDAO = (UserDAO) storeManager.createUser(userId)._findByPrimaryId(UserDAO.LAST_INBOX);
        if (userDAO == null) {
            throw new ResourceNotFoundException("userId=" + userId + " not found", ErrorCodes.NOT_FOUND_USER_ID);
        }

        final UserGroupDAO searchUserGroupDAO = storeManager.createUserGroup();
        searchUserGroupDAO.setUserId(userId);
        if (groupId != null) {
            searchUserGroupDAO.setGroupId(groupId);
        }
        searchUserGroupDAO.setState(AuthorizedState.A.toString()); // TODO really necessary to index on state?
        final UserGroupDAO found = (UserGroupDAO)
                ((groupId == null) ?
                        searchUserGroupDAO._findByCompositeId(new String[]{UserGroupDAO.GROUP_ID}, UserGroupDAO.USER_ID, UserGroupDAO.STATE) :
                        searchUserGroupDAO._findByCompositeId(new String[]{UserGroupDAO.GROUP_ID}, UserGroupDAO.USER_ID, UserGroupDAO.GROUP_ID, UserGroupDAO.STATE));
        if (found == null) {
            if (groupId == null) {   // we're not directly validating the userID
                throw new StateConflictException("userId=" + userId + " is not a member of any active groups", ErrorCodes.USER_NOT_ACTIVE_IN_GROUP);
            }
            throw new StateConflictException("userId=" + userId + " is not active in groupId=" + groupId, ErrorCodes.USER_NOT_ACTIVE_IN_GROUP);
        }
        if (groupId == null) {
            groupId = found.getGroupId();
        }

        // Cycle through inboxes
        Integer lastInbox = userDAO.getLastInbox();
        if (inboxNumber != null) {
            lastInbox = inboxNumber;
        } else {
            final Integer maxInbox = inboxHandler.getMaxInbox(groupId);
            if (lastInbox == null || ++lastInbox >= maxInbox) {
                lastInbox = 0; // initialize or wrap
            }
        }
        // Update last read inbox
        final UserDAO userUpdateDAO = storeManager.createUser(userId);
        userUpdateDAO.setLastInbox(lastInbox); // TODO this should be group-dependent! Store probably in UserGroupDAO. ok for alpha?
        userUpdateDAO._updateByPrimaryId(DAOUpdateType.ABSOLUTE_UPDATE);

        final Inbox inbox = inboxHandler.getInboxFromCache(found.getGroupId(), lastInbox, type, start, count, sortFieldName, sortDirection);
        if (inbox == null) {
            return new ArrayList<InboxBlahPayload>(0);
        }
        return inbox.getItems();
    }

    private boolean isEmptyString(String string) {
        return (string == null || string.length() == 0);
    }

    // Indexing -----------------

    private void initializeBlahIndex() {
        startBlahIndex();
        startCommentIndex();
    }

    private void startCommentIndex() {
        final IndexReaderDecorator<BlahguaFilterIndexReader> decorator = new BlahguaIndexReaderDecorator();
        final ZoieConfig config = makeIndexConfiguration();
        System.out.println("Creating Zoie index in directory " + commentIndexDir.getAbsolutePath());
        this.commentIndexingSystem = new ZoieSystem<BlahguaFilterIndexReader, CommentDAO>(new DefaultDirectoryManager(commentIndexDir), new BlahCommentDataIndexableInterpreter(), decorator, config);
        commentIndexingSystem.start(); // ready to accept indexing events
    }

    private void startBlahIndex() {
        final IndexReaderDecorator<BlahguaFilterIndexReader> decorator = new BlahguaIndexReaderDecorator();
        final ZoieConfig config = makeIndexConfiguration();
        System.out.println("Creating Zoie index in directory " + blahIndexDir.getAbsolutePath());
        this.blahIndexingSystem = new ZoieSystem<BlahguaFilterIndexReader, BlahDAO>(new DefaultDirectoryManager(blahIndexDir), new BlahDataIndexableInterpreter(), decorator, config);
        blahIndexingSystem.start(); // ready to accept indexing events
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
        doIndexBlah((BlahDAO) maybeUpdateForIndex(blahDAO, true));
    }

    private void doIndexBlah(BlahDAO blah) throws SystemErrorException {
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

    /**
     * Deletes the blah from the index.
     *
     * @param blahId The blah's id.
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *
     */
    private void deleteBlahFromIndex(String blahId) throws SystemErrorException {
        final BlahDAO blah = storeManager.createBlah(blahId);
        blah.setDeleted(Boolean.TRUE);
        indexBlah(blah);
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
        doIndexComment((CommentDAO) maybeUpdateForIndex(comment, false));
    }

    private void doIndexComment(CommentDAO comment) throws SystemErrorException {
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

    private void deleteCommentFromIndex(CommentDAO commentDAO) throws SystemErrorException {
        if (isEmptyString(commentDAO.getId())) {
            throw new SystemErrorException("missing comment id in " + this, ErrorCodes.SERVER_INDEXING_ERROR);
        }
        commentDAO.setDeleted(Boolean.TRUE);
        indexComment(commentDAO);
    }

//    /**
//     * Deletes the blah from the index.
//     *
//     * @param commentId The comment's id.
//     * @throws main.java.com.eweware.service.base.error.SystemErrorException
//     *
//     */
//    private void deleteCommentFromIndex(String commentId) throws SystemErrorException {
//        if (isEmptyString(commentId)) {
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
            throw new SystemErrorException("IO error while retrieving from blahs/comment index", e);
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
        if (isEmptyString(fieldName)) {
            fieldName = BlahDAO.TEXT;
        }

        final List<BasePayload> payload = new ArrayList<BasePayload>();
        List<ZoieIndexReader<BlahguaFilterIndexReader>> readerList = null;
        ZoieSystem<BlahguaFilterIndexReader, ?> indexingSystem = null;
        try {
            indexingSystem = searchBlahs ? blahIndexingSystem : commentIndexingSystem;

            if (isEmptyString(query)) {
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
        final Document doc = getDocument(dao.getId(), isBlah);
        if (doc == null) {
            return dao;
        } else { // it's an update
            // Create a copy to be used just for the index
            final BaseDAO updateDAO = isBlah ? storeManager.createBlah(dao) : storeManager.createComment(dao);
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
