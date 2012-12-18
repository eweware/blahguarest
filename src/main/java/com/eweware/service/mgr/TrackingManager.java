package main.java.com.eweware.service.mgr;


import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import main.java.com.eweware.service.base.date.DateUtils;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.ResourceNotFoundException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.mgr.ManagerInterface;
import main.java.com.eweware.service.base.mgr.ManagerState;
import main.java.com.eweware.service.base.store.dao.*;
import main.java.com.eweware.service.base.store.dao.schema.SchemaSpec;
import main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema;
import main.java.com.eweware.service.base.store.dao.tracker.TrackerOperation;
import main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;
import main.java.com.eweware.service.base.type.TrackerType;

import javax.xml.ws.WebServiceException;
import java.util.*;

// TODO remove this dependency

/**
 * User: rk@post.harvard.edu
 * Date: 6/11/12 Time: 6:14 PM
 * <p/>
 * The tracking manager tracks updates to blahs,
 * comments, and users.
 * <p/>
 * This implementation is very MongoDB-dependent as it
 * uses much of its document updating capabilities to advantage.
 * Even if other parts of the system use a different DB (e.g., Cassandra),
 * mongodb appears very valuable for this part.
 * <p/>
 * Instead of relying too much on Hadoop-like aggregation,
 * we keep just one impl doc with all tracking statistics
 * for an object for a large time period (e.g., one year).
 * We use atomic operations to update this doc (e.g.,
 * add votes). Statistics on this data can be computed
 * overnight on Hadoop and/or, in cases where this hasn't
 * been done, it can be done on the fly by the client
 * that reads a single doc (especially when the client is
 * only interested in a reasonably small subset of the doc's data).
 * <p/>
 * <p/>
 * TODO eventually, it will talk to a tracking service
 * TODO add monitors for mutating variables or lists with mutating elements
 * TODO add transaction-level management (rollbacks)
 * <p/>
 * via a persistent messaging queue (e.g., Kestrel) instead of doing the updates
 * in the same server as the web service.
 */
public final class TrackingManager implements ManagerInterface, UserTrackerDAOConstants, BlahTrackerDAOConstants, CommentTrackerDAOConstants {


    private MongoStoreManager storeManager;

    /**
     * Eventually, this object will be queued to the tracking service.
     */
    private class TrackerQueueItem {
        private final TrackerOperation operation;
        private final String authorId;
        private final boolean isBlah;
        private final boolean isNewObject;
        private final String objectId;
        private final String subObjectId;
        private final String userId;
        private final boolean voteUp;
        private final boolean voteDown;
        private final Integer viewCount;
        private final Integer openCount;

        /**
         * Constructor for a tracker queue item.
         *
         * @param operation   The operation
         * @param userId      The user id for this operation
         * @param authorId    The id of the author of the blah or comment, depending on the operation
         * @param isBlah      True if the object is a blah, else it's a comment
         * @param isNewObject True if this is a new blah or comment (i.e., was created by userId)
         * @param objectId    The blah or comment id
         * @param subObjectId If this is a comment (i.e., !isBlah), then this is the id of the blah
*                    upon which it is commenting.
         * @param voteUp   True if the vote is up for this operation
         * @param voteDown True if the vote is down for this operation
         * @param viewCount   Number of views
         * @param openCount   Number of opens
         */
        TrackerQueueItem(TrackerOperation operation, String userId, String authorId, boolean isBlah, boolean isNewObject, String objectId, String subObjectId, boolean voteUp, boolean voteDown, Integer viewCount, Integer openCount) {
            this.operation = operation;
            this.authorId = authorId;
            this.isBlah = isBlah;
            this.isNewObject = isNewObject;
            this.objectId = objectId;
            this.subObjectId = subObjectId;
            this.userId = userId;
            this.voteUp = voteUp;
            this.voteDown = voteDown;
            this.viewCount = viewCount;
            this.openCount = openCount;
        }

        public String toString() {
            final StringBuilder b = new StringBuilder();
            b.append("[op=");
            b.append(operation);
            b.append("userId=");
            b.append(userId);
            b.append("authorId=");
            b.append(authorId);
            b.append("isBlah=");
            b.append(isBlah);
            b.append("isNewObject=");
            b.append(isNewObject);
            b.append("objectId=");
            b.append(objectId);
            b.append("subObjectId=");
            b.append(subObjectId);
            b.append("voteUp=");
            b.append(voteUp);
            b.append("voteDown=");
            b.append(voteDown);
            b.append("viewCount=");
            b.append(viewCount);
            b.append("openCount=");
            b.append(openCount);
            b.append("]");
            return b.toString();
        }
    }

    private static TrackingManager singleton;

    private ManagerState state = ManagerState.UNINITIALIZED;

    // Tracks both comments and blahs:
    private DBCollection blahTrackerCollection;

    // Tracks comments
    private DBCollection commentTrackerCollection;

    // Tracks users for both comments and blahs:
    private DBCollection userTrackerCollection;

    private DBCollection trackerCollection;

    private DBCollection userCollection;

    public TrackingManager() {
        TrackingManager.singleton = this;
        state = ManagerState.INITIALIZED;
        System.out.println("*** TrackingManager initialized ***");
    }

    public ManagerState getState() {
        return state;
    }

    public static TrackingManager getInstance() throws SystemErrorException {
        if (TrackingManager.singleton == null) {
            throw new SystemErrorException("TrackingManager not initialized", ErrorCodes.SERVER_NOT_INITIALIZED);
        }
        return TrackingManager.singleton;
    }

    public void track(LocaleId localeId, TrackerDAO tracker) throws SystemErrorException {
        final String userId = tracker.getUserId();
        if (userId != null) {
            setWithUserProfileInfo(userId, tracker, localeId);
        }
        tracker._insert();
    }

    public final void setWithUserProfileInfo(String userId, TrackerDAO tracker, LocaleId localeId) throws SystemErrorException {
        try {
            final UserProfileDAO profile = (UserProfileDAO) storeManager.createUserProfile(userId)._findByPrimaryId();
            final boolean hasProfile = profile != null;
            final UserProfileSchema schema = hasProfile ? null : UserProfileSchema.getSchema(localeId);
            final Map<String, SchemaSpec> fieldNameToSpecMap = hasProfile ? null : schema.getFieldNameToSpecMap();
            final String gender = hasProfile ? profile.getGender() : (String) fieldNameToSpecMap.get(UserProfileDAO.USER_PROFILE_GENDER).getDefaultValue();
            final String income = hasProfile ? profile.getIncomeRange() : (String) schema.getFieldNameToSpecMap().get(UserProfileDAO.USER_PROFILE_INCOME_RANGE).getDefaultValue();
            final String race = hasProfile ? profile.getRace() : (String) schema.getFieldNameToSpecMap().get(UserProfileDAO.USER_PROFILE_RACE).getDefaultValue();
            final Date dob = hasProfile ? profile.getDateOfBirth() : null;
            if (gender != null) tracker.setUserGender(gender);
            if (income != null) tracker.setUserIncomeRange(income);
            if (race != null) tracker.setUserRace(race);
            if (dob != null) tracker.setUserDateOfBirth(dob);
        } catch (SystemErrorException e) {
            throw e;
        } catch (Exception e) {
            throw new SystemErrorException("failed to set user profile for userId=" + userId + "; tracker=" + tracker);
        }
    }

    /**
     * Updates the daily stats for the blah and the user.
     * This method is the interface between the REST API service
     * and the Tracking service. TODO Eventually, this method will queue up the tracker data for the tracking service.
     *
     *
     *
     * @param operation   The operation
     * @param userId      The id of the user for this operation
     * @param authorId    The id of the author of a blah or comment, depending on the operation
     * @param isBlah      True if the object is a blah; else it's a comment
     * @param isNewObject True if the blah or comment is new. Implies that
 *                    the userId is the author's id.
     * @param objectId    The blah or comment id
     * @param subObjectId If the object is a comment, this is optionally it's blah's id
     * @param voteUp      Vote up for a blah or a comment
     * @param voteDown    Vote up for a blah or a comment
     * @param viewCount   Number of views
     * @param openCount   Number of opens         @throws main.java.com.eweware.service.base.error.SystemErrorException
     */
    public void trackObject(TrackerOperation operation, String userId, String authorId, boolean isBlah, boolean isNewObject, String objectId, String subObjectId, boolean voteUp, boolean voteDown, Integer viewCount, Integer openCount) throws SystemErrorException, ResourceNotFoundException, InvalidRequestException {
        if (!isBlah && subObjectId == null) {
            throw new SystemErrorException("missing subObjectId", ErrorCodes.SERVER_RECOVERABLE_ERROR);
        }
        final TrackerQueueItem trackerData = new TrackerQueueItem(operation, userId, authorId, isBlah, isNewObject, objectId, subObjectId, voteUp, voteDown, viewCount, openCount);
        trackUserStats(trackerData);
        trackBlahOrCommentStats(trackerData);
    }

    /**
     * Tracks the user stats for authors and consumers of blahs.
     *
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     */
    private void trackUserStats(TrackerQueueItem trackerData) throws SystemErrorException, ResourceNotFoundException, InvalidRequestException {

        if (!trackerData.isBlah && (trackerData.subObjectId == null)) { // sanity check
            throw new SystemErrorException("TrackingManager: missing subObjectId for comment", ErrorCodes.SERVER_RECOVERABLE_ERROR);
        }
        switch (trackerData.operation) {
            case CREATE_BLAH:
                trackCreateBlahOperation(trackerData);
                break;
            case UPDATE_BLAH:
                trackUpdateBlahOperation(trackerData);
                break;
            case CREATE_COMMENT:
                trackCreateCommentOperation(trackerData);
                break;
            case UPDATE_COMMENT:
                trackUpdateCommentOperation(trackerData);
                break;
            default:
                throw new SystemErrorException("invalid tracking operation=" + trackerData.operation);
        }

    }

    private void trackUpdateCommentOperation(TrackerQueueItem trackerData) throws SystemErrorException, ResourceNotFoundException {
        boolean trackerIsForUser;
        final String commentAuthorId = BlahManager.getInstance().getAuthorIdForComment(trackerData.objectId);
        final boolean userIsCommentCreator = commentAuthorId.equals(trackerData.userId);
        final String blahAuthorId = BlahManager.getInstance().getAuthorIdForBlah(trackerData.subObjectId);
        final boolean userIsBlahCreator = blahAuthorId.equals(trackerData.userId);

        String trackerId = maybeAllocateUserTracker(trackerData.userId);

        if (!userIsCommentCreator && !userIsBlahCreator) {
            // voting/viewing/opening comment to someone else's blah

            // update vote/view/open for OTHER comments
            trackerIsForUser = true;
            updateUserStats(trackerData, trackerIsForUser, trackerId, userIsBlahCreator);

            // update vote/view/open for OWNED comments of owner of comment
            trackerId = maybeAllocateUserTracker(commentAuthorId);
            trackerIsForUser = false;
            final boolean isBlahCreator = true;
            updateUserStats(trackerData, trackerIsForUser, trackerId, isBlahCreator);

        } else if (userIsCommentCreator && !userIsBlahCreator) {
            // voting/viewing/opening on own comment to someone else's blah
            // not supported: comment creator is motivated to look at own comments, so ignore
        } else if (!userIsCommentCreator && userIsBlahCreator) {
            // voting/viewing/opening comment to own blah
            // not supported: blah creator is motivated to look at comments to own blah, so ignore
        } else { /* userIsCommentCreator && userIsBlahCreator */
            // voting/viewing/opening on own comment to own blah
            // not supported:  blah creator is motivated to look at comments (especially own comments) to own blah, so ignore
        }
    }

    private void trackCreateCommentOperation(TrackerQueueItem trackerData) throws SystemErrorException, ResourceNotFoundException {

        String trackerId = maybeAllocateUserTracker(trackerData.userId);
        final String blahAuthorId = BlahManager.getInstance().getAuthorIdForBlah(trackerData.subObjectId);
        boolean trackerIsForUser = true;
        final boolean   userIsBlahCreator = blahAuthorId.equals(trackerData.userId);

        updateUserStats(trackerData, trackerIsForUser, trackerId, userIsBlahCreator);

        if (!userIsBlahCreator) {
            // comment by someone other than blah creator: update creator's stats
            trackerId = maybeAllocateUserTracker(blahAuthorId);
            trackerIsForUser = false;
            final boolean ignored = false;
            updateUserStats(trackerData, trackerIsForUser, trackerId, ignored);
        }
    }

    private void trackUpdateBlahOperation(TrackerQueueItem trackerData) throws SystemErrorException, ResourceNotFoundException {
        final String trackerId = maybeAllocateUserTracker(trackerData.userId);
        final String blahAuthorId = BlahManager.getInstance().getAuthorIdForBlah(trackerData.objectId);
        boolean userIsBlahCreator = blahAuthorId.equals(trackerData.userId);

        if (userIsBlahCreator) {
            final boolean trackerIsForUser = false;
            updateUserStats(trackerData, trackerIsForUser, trackerId, userIsBlahCreator);
        } else {
            boolean trackerIsForUser = true;
            updateUserStats(trackerData, trackerIsForUser, trackerId, userIsBlahCreator);

            final String blahAuthorTrackerId = maybeAllocateUserTracker(blahAuthorId);
            trackerIsForUser = false;
            userIsBlahCreator = true;
            updateUserStats(trackerData, trackerIsForUser, blahAuthorTrackerId, userIsBlahCreator);
        }
    }

    private void trackCreateBlahOperation(TrackerQueueItem trackerData) throws SystemErrorException {
        final String trackerId = maybeAllocateUserTracker(trackerData.userId);
        final boolean userIsBlahCreator = true;
        final boolean trackerIsForUser = true;
        updateUserStats(trackerData, trackerIsForUser, trackerId, userIsBlahCreator);
        return;
    }

    /**
     * Preallocates a user tracker object in the DB if it doesn't already exist.
     *
     * @param userId The user id
     * @return String   The tracker id for the tracker
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     */
    private String maybeAllocateUserTracker(String userId) throws SystemErrorException {
        final TrackerType trackerType = TrackerType.USER;
        final String trackerId = makeTrackerId(trackerType, userId);
        if (getUserTrackerCollection().count(new BasicDBObject(BaseDAO.ID, trackerId)) == 0) { // preallocate space for document
            createUserTracker(trackerId, userId);
        }
        return trackerId;
    }

    /**
     * See UserTrackerDAOConstants for field names and structure.
     *
     * @param trackerData
     * @param trackerIsForUser
     * @param userIsBlahCreator
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     */
    private void updateUserStats(TrackerQueueItem trackerData, boolean trackerIsForUser, String trackerId, boolean userIsBlahCreator) throws SystemErrorException {

        final DBObject updates = new BasicDBObject();
        final Map<String, Object> inc = new HashMap<String, Object>();
        updates.put("$inc", inc);

        final boolean hasVoted = trackerData.voteUp || trackerData.voteDown;
        final boolean hasViewed = trackerData.viewCount != null && trackerData.viewCount > 0;
        final boolean hasOpened = trackerData.openCount != null && trackerData.openCount > 0;

        // Monthly updates
        Map<String, Object> monthlyCumFieldMap = updateUserMonthlies(trackerData, trackerIsForUser, userIsBlahCreator, hasVoted, hasViewed, hasOpened);
        inc.putAll(monthlyCumFieldMap);

        // Daily updates
        final Map<String, Object> dailyFieldMap = updateUserDailies(trackerData, trackerIsForUser, userIsBlahCreator, hasVoted, hasViewed, hasOpened);
        inc.putAll(dailyFieldMap);

        final WriteResult result = getUserTrackerCollection().update(new BasicDBObject(BaseDAO.ID, trackerId), updates);
        if (result.getError() != null) {
            throw new SystemErrorException("failed to insert tracker " + updates + "; error=" + result.getError(), ErrorCodes.FAILED_TRACKER_INSERT);
        }
    }

    private Map<String, Object> updateUserMonthlies(TrackerQueueItem trackerData, boolean trackerIsForUser, boolean userIsBlahCreator, boolean hasVoted, boolean hasViewed, boolean hasOpened) {

        Map<String, Object> monthlyCumFieldMap = new HashMap<String, Object>();

        final boolean daily = false;
        final String dailyIndex = "";
        if (trackerData.isBlah) {
            updateUserBlahEntry(daily, dailyIndex, trackerData, trackerIsForUser, userIsBlahCreator, hasVoted, hasViewed, hasOpened, monthlyCumFieldMap);
        } else {
            updateUserCommentEntry(daily, dailyIndex, trackerData, trackerIsForUser, userIsBlahCreator, hasVoted, hasViewed, hasOpened, monthlyCumFieldMap);
        }
        return monthlyCumFieldMap;
    }

    private Map<String, Object> updateUserDailies(TrackerQueueItem trackerData, boolean trackerIsForUsers, boolean userIsBlahCreator, boolean hasVoted, boolean hasViewed, boolean hasOpened) {

        final Map<String, Object> todayFieldMap = new HashMap<String, Object>();

        final int dayOfMonth = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC")).get(Calendar.DAY_OF_MONTH) - 1; // decrement for zero-origin array

        final StringBuilder b = new StringBuilder(UT_DAILY_STATS_ARRAY);
        b.append('.');
        b.append(dayOfMonth);
        b.append('.');
        final String index = b.toString();

        final boolean daily = true;
        if (trackerData.isBlah) {
            updateUserBlahEntry(daily, index, trackerData, trackerIsForUsers, userIsBlahCreator, hasVoted, hasViewed, hasOpened, todayFieldMap);
        } else {
            updateUserCommentEntry(daily, index, trackerData, trackerIsForUsers, userIsBlahCreator, hasVoted, hasViewed, hasOpened, todayFieldMap);
        }

        return todayFieldMap;
    }

    private void updateUserBlahEntry(boolean day, String dayMarker, TrackerQueueItem trackerData, boolean trackerIsForUser, boolean userIsBlahCreator, boolean hasVoted, boolean hasViewed, boolean hasOpened, Map<String, Object> map) {

        final boolean creatingObject = trackerData.isNewObject;

        if (creatingObject) {  // blah created
            if (trackerIsForUser && userIsBlahCreator) {  // creatingObject = true && trackerIsForUser = true && obviously userIsBlahCreator = true
                map.put(day ? dayMarker + UT_BLAHS_CREATED_IN_DAY : UT_BLAHS_CREATED_IN_MONTH, 1);
            } else {  // creatingObject = true && trackerIsForUser = false:
                // never happens
            }
        } else {  // blah updated
            if (trackerIsForUser) { // creatingObject = false && trackerIsForUser = true
                if (userIsBlahCreator) {
                    if (hasViewed) {
                        map.put(day ? dayMarker + UT_VIEWS_OF_OWNED_BLAHS_BY_SELF_IN_DAY : UT_VIEWS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH, 1);
                    }
                    if (hasOpened) {
                        map.put(day ? dayMarker + UT_OPENS_OF_OWNED_BLAHS_BY_SELF_IN_DAY : UT_OPENS_OF_OWNED_BLAHS_BY_SELF_IN_MONTH, 1);
                    }
                } else {
                    if (hasVoted) {
                        map.put(day ? dayMarker + UT_VOTES_FOR_OTHERS_BLAHS_IN_DAY : UT_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, 1);
                        if (trackerData.voteUp) {
                            map.put(day ? dayMarker + UT_UP_VOTES_FOR_OTHERS_BLAHS_IN_DAY : UT_UP_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, 1);
                        }
                        if (trackerData.voteDown) {
                            map.put(day ? dayMarker + UT_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_DAY : UT_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, 1);
                        }
                    }
                    if (hasViewed) {
                        map.put(day ? dayMarker + UT_VIEWS_OF_OTHERS_BLAHS_IN_DAY : UT_VIEWS_OF_OTHERS_BLAHS_IN_MONTH, trackerData.viewCount);
                    }
                    if (hasOpened) {
                        map.put(day ? dayMarker + UT_OPENS_OF_OTHERS_BLAHS_IN_DAY : UT_OPENS_OF_OTHERS_BLAHS_IN_MONTH, trackerData.openCount);
                    }
                }
            } else { // creatingObject = false && trackerIsForUser = false
                if (userIsBlahCreator) {
                    if (hasVoted) {
                        map.put(day ? dayMarker + UT_VOTES_FOR_OWNED_BLAHS_IN_DAY : UT_VOTES_FOR_OWNED_BLAHS_IN_MONTH, 1);
                        if (trackerData.voteUp) {
                            map.put(day ? dayMarker + UT_UP_VOTES_FOR_OWNED_BLAHS_IN_DAY : UT_UP_VOTES_FOR_OWNED_BLAHS_IN_MONTH, 1);
                        }
                        if (trackerData.voteDown) {
                            map.put(day ? dayMarker + UT_DOWN_VOTES_FOR_OWNED_BLAHS_IN_DAY : UT_DOWN_VOTES_FOR_OWNED_BLAHS_IN_MONTH, 1);
                        }
                    }
                    if (hasViewed) {
                        map.put(day ? dayMarker + UT_VIEWS_OF_OWNED_BLAHS_IN_DAY : UT_VIEWS_OF_OWNED_BLAHS_IN_MONTH, trackerData.viewCount);
                    }
                    if (hasOpened) {
                        map.put(day ? dayMarker + UT_OPENS_OF_OWNED_BLAHS_IN_DAY : UT_OPENS_OF_OWNED_BLAHS_IN_MONTH, trackerData.openCount);
                    }
                } else {
                    // no action
                }
            }
        }
    }

    private void updateUserCommentEntry(boolean day, String dayMarker, TrackerQueueItem trackerData, boolean trackerIsForUser, boolean userIsBlahCreator, boolean hasVoted, boolean hasViewed, boolean hasOpened, Map<String, Object> map) {

        final boolean creatingObject = trackerData.isNewObject;

        if (creatingObject) { // a comment is created

            if (trackerIsForUser) { // creatingObject = true && trackerIsForUser = true

                map.put(day ? dayMarker + UT_COMMENTS_CREATED_IN_DAY : UT_COMMENTS_CREATED_IN_MONTH, 1);

                if (userIsBlahCreator) {
                    map.put(day ? dayMarker + UT_COMMENTS_CREATED_ON_OWNED_BLAHS_BY_SELF_IN_DAY : UT_COMMENTS_CREATED_ON_OWNED_BLAHS_BY_SELF_IN_MONTH, 1);
                } else {
                    map.put(day ? dayMarker + UT_COMMENTS_ON_OTHERS_BLAHS_IN_DAY : UT_COMMENTS_ON_OTHERS_BLAHS_IN_MONTH, 1);
                    if (hasVoted) {
                        map.put(day ? dayMarker + UT_VOTES_FOR_OTHERS_BLAHS_IN_DAY : UT_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, 1);
                        if (trackerData.voteUp) {
                            map.put(day ? dayMarker + UT_UP_VOTES_FOR_OTHERS_BLAHS_IN_DAY : UT_UP_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, 1);
                        }
                        if (trackerData.voteDown) {
                            map.put(day ? dayMarker + UT_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_DAY : UT_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, 1);
                        }
                    }
                }

            } else { // creatingObject = true && trackerIsForUser = false
                map.put(day ? dayMarker + UT_COMMENTS_ON_OWNED_BLAHS_IN_DAY : UT_COMMENTS_ON_OWNED_BLAHS_IN_MONTH, 1);
                if (hasVoted) {
                    map.put(day ? dayMarker + UT_VOTES_FOR_OWNED_BLAHS_IN_DAY : UT_VOTES_FOR_OWNED_BLAHS_IN_MONTH, 1);
                    if (trackerData.voteUp) {
                        map.put(day ? dayMarker + UT_UP_VOTES_FOR_OWNED_BLAHS_IN_DAY : UT_UP_VOTES_FOR_OWNED_BLAHS_IN_MONTH, 1);
                    }
                    if (trackerData.voteDown) {
                        map.put(day ? dayMarker + UT_DOWN_VOTES_FOR_OWNED_BLAHS_IN_DAY : UT_DOWN_VOTES_FOR_OWNED_BLAHS_IN_MONTH, 1);
                    }
                }
            }
        } else { // a comment is updated
            if (trackerIsForUser) {  // creatingObject = false && trackerIsForUser = true
                if (userIsBlahCreator) {
                    // not supported
                } else {
                    if (hasVoted) {
                        map.put(day ? dayMarker + UT_VOTES_FOR_OTHERS_COMMENTS_IN_DAY : UT_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH, 1);
                        if (trackerData.voteUp) {
                            map.put(day ? dayMarker + UT_UP_VOTES_FOR_OTHERS_COMMENTS_IN_DAY : UT_UP_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH, 1);
                        }
                        if (trackerData.voteDown) {
                            map.put(day ? dayMarker + UT_DOWN_VOTES_FOR_OTHERS_COMMENTS_IN_DAY : UT_DOWN_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH, 1);
                        }
                    }
                    if (hasViewed) {
                        map.put(day ? dayMarker + UT_VIEWS_OF_OTHERS_COMMENTS_IN_DAY : UT_VIEWS_OF_OTHERS_COMMENTS_IN_MONTH, trackerData.viewCount);
                    }
                    if (hasOpened) {
                        map.put(day ? dayMarker + UT_OPENS_OF_OTHERS_COMMENTS_IN_DAY : UT_OPENS_OF_OTHERS_COMMENTS_IN_MONTH, trackerData.openCount);
                    }
                }
            } else {  // creatingObject = false && trackerIsForUser = false:
                if (userIsBlahCreator) {
                    if (hasVoted) {
                        map.put(day ? dayMarker + UT_VOTES_FOR_OWNED_COMMENTS_IN_DAY : UT_VOTES_FOR_OWNED_COMMENTS_IN_MONTH, 1);
                        if (trackerData.voteUp) {
                            map.put(day ? dayMarker + UT_UP_VOTES_FOR_OWNED_COMMENTS_IN_DAY : UT_UP_VOTES_FOR_OWNED_COMMENTS_IN_MONTH, 1);
                        }
                        if (trackerData.voteDown) {
                            map.put(day ? dayMarker + UT_DOWN_VOTES_FOR_OWNED_COMMENTS_IN_DAY : UT_DOWN_VOTES_FOR_OWNED_COMMENTS_IN_MONTH, 1);
                        }
                    }
                    if (hasViewed) {
                        map.put(day ? dayMarker + UT_VIEWS_OF_OWNED_COMMENTS_IN_DAY : UT_VIEWS_OF_OWNED_COMMENTS_IN_MONTH, trackerData.viewCount);
                    }
                    if (hasOpened) {
                        map.put(day ? dayMarker + UT_OPENS_OF_OWNED_COMMENTS_IN_DAY : UT_OPENS_OF_OWNED_COMMENTS_IN_MONTH, trackerData.openCount);
                    }
                } else {
                    // not supported
                }
            }
        }
    }

    /**
     * See BlahTrackerDAOConstants for field names and structure.
     *
     * @param trackerData A tracker data instance.
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     */
    private void trackBlahOrCommentStats(TrackerQueueItem trackerData) throws SystemErrorException {

        if (trackerData.isBlah) {
            trackBlah(trackerData);
        } else {
            trackComment(trackerData);
        }
    }

    private void trackComment(TrackerQueueItem trackerData) throws SystemErrorException {

        // First update comment itself
        final DBCollection commentCollection = getCommentTrackerCollection();

        final String commentTrackerId = maybeAllocateTracker(commentCollection, TrackerType.COMMENT, trackerData, trackerData.objectId);

        final Map<String, Object> commentTrackerUpdates = new HashMap<String, Object>();

        if (trackerData.voteUp) {
            commentTrackerUpdates.put(trackerData.isNewObject ? CT_UP_VOTE_FOR_BLAH : CT_UP_VOTES_FOR_COMMENT, 1);
        } else if (trackerData.voteDown) {
            commentTrackerUpdates.put(trackerData.isNewObject ? CT_DOWN_VOTE_FOR_BLAH : CT_DOWN_VOTES_FOR_COMMENT, 1);
        }
        if (trackerData.viewCount != null && trackerData.viewCount > 0) {
            commentTrackerUpdates.put(CT_VIEWS, trackerData.viewCount);
        }
        if (trackerData.openCount != null && trackerData.openCount > 0) {
            commentTrackerUpdates.put(CT_OPENS, trackerData.openCount);
        }

        if (!commentTrackerUpdates.isEmpty()) {

            DBObject fields = new BasicDBObject();
            Map<String, Object> incrementorMap = new HashMap<String, Object>();
            fields.put("$inc", incrementorMap);
            incrementorMap.putAll(commentTrackerUpdates);

            final WriteResult result = commentCollection.update(new BasicDBObject(BaseDAO.ID, commentTrackerId), fields);
            if (result.getError() != null) {
                throw new SystemErrorException("failed to insert comment tracker " + fields + "; error=" + result.getError(), ErrorCodes.FAILED_TRACKER_INSERT);
            }
        } else {
            // nothing to update, but comment tracker has been created
        }


        if (trackerData.isNewObject) {

            final DBCollection blahCollection = getBlahTrackerCollection();

            final BasicDBObject fields = new BasicDBObject();
            final Map<String, Object> incrementorMap = new HashMap<String, Object>();
            fields.put("$inc", incrementorMap);

            final String blahTrackerId = maybeAllocateTracker(blahCollection, TrackerType.BLAH, trackerData, trackerData.subObjectId);

            final Map<String, Object> blahTrackerUpdates = new HashMap<String, Object>();

            blahTrackerUpdates.put(BT_COMMENTS, 1);
            // Interpret votes as being for blah
            if (trackerData.voteUp) {
                blahTrackerUpdates.put(BT_UP_VOTES, 1);
            } else if (trackerData.voteDown) {
                blahTrackerUpdates.put(BT_DOWN_VOTES, 1);
            }
            incrementorMap.putAll(blahTrackerUpdates);

            final WriteResult r = blahCollection.update(new BasicDBObject(BaseDAO.ID, blahTrackerId), fields);
            if (r.getError() != null) {
                throw new SystemErrorException("failed to insert blah tracker " + fields + "; error=" + r.getError(), ErrorCodes.FAILED_TRACKER_INSERT);
            }
        }

    }

    private void trackBlah(TrackerQueueItem trackerData) throws SystemErrorException {

        final DBObject updates = new BasicDBObject();
        final Map<String, Object> inc = new HashMap<String, Object>();
        updates.put("$inc", inc);

        final DBCollection collection = getBlahTrackerCollection();

        final String trackerId = maybeAllocateTracker(collection, TrackerType.BLAH, trackerData, trackerData.objectId);

        final Map<String, Object> map = new HashMap<String, Object>();

        if (trackerData.voteUp) {
            map.put(BT_UP_VOTES, 1);
        } else if (trackerData.voteDown) {
            map.put(BT_DOWN_VOTES, 1);
        }
        if (trackerData.viewCount != null && trackerData.viewCount > 0) {
            map.put(BT_VIEWS, trackerData.viewCount);
        }
        if (trackerData.openCount != null && trackerData.openCount > 0) {
            map.put(BT_OPENS, trackerData.openCount);
        }
        inc.putAll(map);

        final WriteResult result = collection.update(new BasicDBObject(BaseDAO.ID, trackerId), updates);
        if (result.getError() != null) {
            throw new SystemErrorException("failed to insert tracker " + updates + "; error=" + result.getError(), ErrorCodes.FAILED_TRACKER_INSERT);
        }
    }

    private String maybeAllocateTracker(DBCollection collection, TrackerType trackerType, TrackerQueueItem trackerData, String id) throws SystemErrorException {
        final String trackerId = makeTrackerId(trackerType, id);
        if (collection.count(new BasicDBObject(BaseDAO.ID, trackerId)) == 0) { // preallocate space for document
            createTracker(collection, trackerType, trackerId, trackerData);
        }
        return trackerId;
    }

    /**
     * For blah and comment trackers
     */
    private DBObject createTracker(DBCollection collection, TrackerType trackerType, String trackerId, TrackerQueueItem trackerData) throws SystemErrorException {

        final DBObject tracker = new BasicDBObject();

        final boolean isBlahTracker = trackerType == TrackerType.BLAH;
        tracker.put(BaseDAO.ID, trackerId);
        tracker.put(isBlahTracker ? BT_OBJECT_ID : CT_OBJECT_ID, trackerData.objectId);
        tracker.put(isBlahTracker ? BT_AUTHOR_ID : CT_AUTHOR_ID, trackerData.authorId);

        // Monthly stats:
        for (String fieldName : isBlahTracker ? BT_FIELD_NAMES : CT_FIELD_NAMES) {
            tracker.put(fieldName, 0);
        }
        final WriteResult insert = collection.insert(tracker);
        if (insert.getError() != null) {
            throw new SystemErrorException("failed to insert tracker; error=" + insert.getError() + "\ntracker value=" + tracker, ErrorCodes.FAILED_TRACKER_INSERT);
        }

        return tracker;
    }

    private DBObject createUserTracker(String trackerId, String userId) throws SystemErrorException {

        final DBObject tracker = new BasicDBObject();

        final Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.DAY_OF_MONTH, 1); // start from first day of month

        tracker.put(UserTrackerDAO.ID, trackerId);
        tracker.put(UT_USER_ID, userId);
        final int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        tracker.put(UT_NUMBER_OF_DAYS_IN_MONTH, daysInMonth);

        // Monthly stats:
        for (String fieldName : UT_MONTHLY_FIELD_NAMES) {
            tracker.put(fieldName, 0);
        }

        // Dailies stats:
        final List<Map<String, Object>> dailyArray = new ArrayList<Map<String, Object>>();
        tracker.put(UT_DAILY_STATS_ARRAY, dailyArray);
        for (int day = 1; day <= daysInMonth; day++) {
            final Map<String, Object> dayMap = new HashMap<String, Object>();
            dailyArray.add(dayMap);
            for (String fieldName : UT_DAILY_FIELD_NAMES) {
                dayMap.put(fieldName, 0);
            }
        }
        final WriteResult insert = getUserTrackerCollection().insert(tracker);
        if (insert.getError() != null) {
            throw new SystemErrorException("failed to insert tracker; error=" + insert.getError() + "\ntracker value=" + tracker, ErrorCodes.FAILED_TRACKER_INSERT);
        }

        return tracker;
    }

    private void trackUserGraph() {
        // TODO builds the user graph based on infection
    }


    /**
     * Creates a tracker id for the specified parameters
     * and for the current month and year.
     * TODO this method should be in a base class so that the future tracking service can share it with the API
     *
     * @param trackerType Whether tracker is for user, blah or comment
     * @param objectId    object id: a userId (to track users), or a blah Id (to track blahs),
     *                    or a comment Id (to track comments)
     * @return String  The tracker id
     */
    public static final String makeTrackerId(TrackerType trackerType, String objectId) throws SystemErrorException {
        final Calendar cal = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        return makeTrackerIdExternal(trackerType, objectId, cal);
    }

    public static String makeTrackerIdExternal(TrackerType trackerType, String objectId, Calendar cal) throws SystemErrorException {
        if (trackerType == TrackerType.USER) {
            cal.set(Calendar.DAY_OF_MONTH, 1); // start first day of month
        }
        return makeTrackerIdInternal(trackerType, objectId, cal);
    }

    /**
     * Returns a tracker id for the specified parameters.
     * v
     *
     * @param trackerType Is this tracking a user, a blah or a comment?
     * @param objectId    A userId, blahId, or commentId, depending on the tracker type
     * @return String  The tracker id
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     */
    public static final String makeUserTrackerIdExternal(TrackerType trackerType, Calendar cal, String objectId) throws SystemErrorException {
        final int month = cal.get(Calendar.MONTH);
        final int year = cal.get(Calendar.YEAR);
        return makeTrackerIdInternal(trackerType, objectId, cal);
    }

    /**
     * TODO this method should be in a base class so that the future tracking service can share it with the API
     * <p/>
     * The tracker id allows us to easily search for a specific month
     * and to sort by date ranges. It is composed of:
     * <p/>
     * 1. The id of the object (userId, blahId or commentId) itself.
     * 2. Type identifier character IFF tracker is not a user tracker:
     * "B" if this is a blah tracker
     * "C" if this is a comment tracker
     * 3. The date of the first month in YYMM format (e.g., August 2012 is 1208)
     * <p/>
     *
     * @param trackerType Is the tracker for a blah or a comment?
     * @param objectId    Either a userId, blahId, or objectId
     * @param cal         A calendar object holding the month/year for which the id should be constructed.
     * @return String   A tracker id
     */
    private static String makeTrackerIdInternal(TrackerType trackerType, String objectId, Calendar cal) throws SystemErrorException {
        if (objectId == null || cal == null || trackerType == null) {
            throw new SystemErrorException("Missing objectId, calendar, or tracker type", ErrorCodes.SERVER_RECOVERABLE_ERROR);
        }
        final StringBuilder b = new StringBuilder(objectId);
        final String date = (trackerType == TrackerType.USER) ? DateUtils.formatYearMonthDate(cal) : DateUtils.formatYearMonthDateDate(cal);
        b.append(date);
        return b.toString();
    }

    private static final String getDateFromTrackerId(String trackerId) {
        return trackerId.substring(0, 10);
    }

    private DBCollection getBlahTrackerCollection() {
        return blahTrackerCollection;
    }

    private DBCollection getCommentTrackerCollection() {
        return commentTrackerCollection;
    }

    private DBCollection getUserTrackerCollection() {
        return userTrackerCollection;
    }

    private DBCollection getTrackerCollection() {
        return trackerCollection;
    }

    private DBCollection getUserCollection() {
        return userCollection;
    }

    public void start() {
        try {
            // TODO remove dependency on MongoStoreManager
            this.storeManager = ((MongoStoreManager) MongoStoreManager.getInstance());
        } catch (SystemErrorException e) {
            throw new WebServiceException(e);
        }
        userTrackerCollection = storeManager.getCollection(storeManager.getTrackUserCollectionName());
        blahTrackerCollection = storeManager.getCollection(storeManager.getTrackBlahCollectionName());
        commentTrackerCollection = storeManager.getCollection(storeManager.getTrackCommentCollectionName());
        trackerCollection = storeManager.getCollection(storeManager.getTrackerCollectionName());
        userCollection = storeManager.getCollection(storeManager.getUserCollectionName());
        state = ManagerState.STARTED;
        System.out.println("*** TrackingManager started ***");
    }

    public void shutdown() {
        state = ManagerState.SHUTDOWN;
        System.out.println("*** TrackingManager shutdown ***");
    }
}

