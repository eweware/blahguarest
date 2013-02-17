package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.*;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.mgr.ManagerState;
import main.java.com.eweware.service.base.store.StoreManager;
import main.java.com.eweware.service.base.store.dao.*;
import main.java.com.eweware.service.base.store.dao.tracker.TrackerOperation;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         <p/>
 *         Store manager controls MongoDB access.
 *         Initialized as a Spring bean.
 *         <p/>
 */
public final class MongoStoreManager implements StoreManager {

    // Keep it simple for now: only one type and one instance allowed
    protected static MongoStoreManager singleton;

    public static MongoStoreManager getInstance() throws SystemErrorException {
        if (MongoStoreManager.singleton == null) {
            throw new SystemErrorException("mongo store manager not initialized");
        }
        return MongoStoreManager.singleton;
    }

    private ManagerState status = ManagerState.UNINITIALIZED;
    private int port;
    private String hostname;
    private Integer connectionsPerHost = 10; // default
    private Mongo mongo;
    private Map<String, DB> dbNameToDbMap;
    private String sysDbName;
    private String userDbName;
    private String blahDbName;
//    private String mediaDbName;
    private String trackerDbName;

    private String inboxStateCollectionName;
    private String mediaCollectionName;
    private String blahCollectionName;
    private String blahTypeCollectionName;
    private String groupCollectionName;
    private String groupTypeCollectionName;
    private String userCollectionName;
    private String userProfileCollectionName;
    private String userGroupCollectionName;
    private String commentCollectionName;
    private String userBlahInfoCollectionName;
    private String userCommentInfoCollectionName;
    private String trackBlahCollectionName;
    private String trackCommentCollectionName;
    private String trackerCollectionName;
    private String trackUserCollectionName;
    private String blahInboxCollectionName;
    private String demographicsCollectionName;

    Map<String, DBCollection> collectionNameToCollectionMap = new HashMap<String, DBCollection>();


    public String getInboxStateCollectionName() {
        return inboxStateCollectionName;
    }

    public String getMediaCollectionName() {
        return mediaCollectionName;
    }
    public String getBlahCollectionName() {
        return blahCollectionName;
    }

    public String getBlahTypeCollectionName() {
        return blahTypeCollectionName;
    }

    public String getGroupCollectionName() {
        return groupCollectionName;
    }

    public String getGroupTypeCollectionName() {
        return groupTypeCollectionName;
    }

    public String getUserCollectionName() {
        return userCollectionName;
    }

    public String getUserProfileCollectionName() {
        return userProfileCollectionName;
    }

    public String getUserGroupCollectionName() {
        return userGroupCollectionName;
    }

    public String getCommentCollectionName() {
        return commentCollectionName;
    }

    public String getUserBlahInfoCollectionName() {
        return userBlahInfoCollectionName;
    }

    public String getUserCommentInfoCollectionName() {
        return userCommentInfoCollectionName;
    }

    public String getTrackBlahCollectionName() {
        return trackBlahCollectionName;
    }

    public String getTrackCommentCollectionName() {
        return trackCommentCollectionName;
    }

    public String getTrackerCollectionName() {
        return trackerCollectionName;
    }

    public String getTrackUserCollectionName() {
        return trackUserCollectionName;
    }

    public String getBlahInboxCollectionName() {
        return blahInboxCollectionName;
    }

    public String getDemographicsCollectionName() {
        return demographicsCollectionName;
    }


    /**
     * tracks stats TODO will eventually be a separate tracking service *
     */

    /**
     * Constructor for testing, etc.
     */
    public MongoStoreManager() {
        MongoStoreManager.singleton = this;
    }

    /**
     * Production-time constructor called by the Spring framework.
     */
    public MongoStoreManager(

            String hostname,
            String port,

            String userDbName,
            String blahDbName,
            String trackerDbName,

            String trackUserCollectionName,
            String trackBlahCollectionName,
            String trackCommentCollectionName,
            String trackerCollectionName,

            String userCollectionName,
            String userProfileCollectionName,

            String groupCollectionName,
            String groupTypeCollectionName,

            String userGroupCollectionName,

            String mediaCollectionName,
            String blahCollectionName,
            String blahTypeCollectionName,
            String commentCollectionName,
            String userBlahInfoCollectionName,
            String userCommentInfoCollectionName,
            String inboxStateCollectionName,
            String blahInboxCollectionName,
            String demographicsCollectionName,

            Integer connectionsPerHost
            ) {
        this.hostname = hostname;
        this.port = Integer.parseInt(port);
        this.connectionsPerHost = connectionsPerHost;

        this.dbNameToDbMap = new HashMap<String, DB>(3);
        this.sysDbName = "sysdb";
//        this.mediaDbName = mediaDbName;
        this.userDbName = userDbName;
        this.blahDbName = blahDbName;
        this.trackerDbName = trackerDbName;

        dbNameToDbMap.put(sysDbName, null);
        dbNameToDbMap.put(userDbName, null);
//        dbNameToDbMap.put(mediaDbName, null);
        dbNameToDbMap.put(blahDbName, null);
        dbNameToDbMap.put(trackerDbName, null);

        this.trackUserCollectionName = trackUserCollectionName;

        this.trackBlahCollectionName = trackBlahCollectionName;
        this.trackCommentCollectionName = trackCommentCollectionName;
        this.trackerCollectionName = trackerCollectionName;
        this.userCollectionName = userCollectionName;
        this.userProfileCollectionName = userProfileCollectionName;
        this.groupCollectionName = groupCollectionName;
        this.groupTypeCollectionName = groupTypeCollectionName;
        this.userGroupCollectionName = userGroupCollectionName;
        this.inboxStateCollectionName = inboxStateCollectionName;
        this.mediaCollectionName = mediaCollectionName;
        this.blahCollectionName = blahCollectionName;
        this.blahTypeCollectionName = blahTypeCollectionName;
        this.commentCollectionName = commentCollectionName;
        this.userBlahInfoCollectionName = userBlahInfoCollectionName;
        this.userCommentInfoCollectionName = userCommentInfoCollectionName;
        this.blahInboxCollectionName = blahInboxCollectionName;
        this.demographicsCollectionName = demographicsCollectionName;

        System.out.println("*STORE MGR: known database names: " + dbNameToDbMap.keySet());
        MongoStoreManager.singleton = this;
        this.status = ManagerState.INITIALIZED;
    }

    public static final ObjectId makeObjectId(String id) throws SystemErrorException {
        try {
            return new ObjectId(id);
        } catch (IllegalArgumentException e) {
            throw new SystemErrorException("the id is not valid (it must be an UUID string); id=" + id, e);
        }
    }

    public void start() {
        try {
            final MongoOptions mongoOptions = new MongoOptions();
            mongoOptions.connectionsPerHost = connectionsPerHost; // Use >db.serverStatus() to check number of connections in mongo server
            final ServerAddress serverAddress = new ServerAddress(hostname, port);

            this.mongo = new Mongo(serverAddress, mongoOptions);

            for (String dbname : dbNameToDbMap.keySet()) {
                dbNameToDbMap.put(dbname, mongo.getDB(dbname));
            }
            collectionNameToCollectionMap.put(groupCollectionName, getUserDb().getCollection(groupCollectionName));

            collectionNameToCollectionMap.put(groupTypeCollectionName, getUserDb().getCollection(groupTypeCollectionName));

            collectionNameToCollectionMap.put(userCollectionName, getUserDb().getCollection(userCollectionName));

            collectionNameToCollectionMap.put(userProfileCollectionName, getUserDb().getCollection(userProfileCollectionName));

            collectionNameToCollectionMap.put(userGroupCollectionName, getUserDb().getCollection(userGroupCollectionName));

            collectionNameToCollectionMap.put(blahCollectionName, getBlahDb().getCollection(blahCollectionName));

            collectionNameToCollectionMap.put(mediaCollectionName, getBlahDb().getCollection(mediaCollectionName));

            collectionNameToCollectionMap.put(inboxStateCollectionName, getBlahDb().getCollection(inboxStateCollectionName));

            collectionNameToCollectionMap.put(blahTypeCollectionName, getBlahDb().getCollection(blahTypeCollectionName));

            collectionNameToCollectionMap.put(commentCollectionName, getBlahDb().getCollection(commentCollectionName));

            collectionNameToCollectionMap.put(userBlahInfoCollectionName, getUserDb().getCollection(userBlahInfoCollectionName));

            collectionNameToCollectionMap.put(blahInboxCollectionName, getBlahDb().getCollection(blahInboxCollectionName));

            collectionNameToCollectionMap.put(demographicsCollectionName, getTrackerDb().getCollection(demographicsCollectionName));

            collectionNameToCollectionMap.put(userCommentInfoCollectionName, getUserDb().getCollection(userCommentInfoCollectionName));

            // TODO this collection could be a capped collection for production, but there are many restrictions (e.g., not shardable):
            collectionNameToCollectionMap.put(trackBlahCollectionName, getTrackerDb().getCollection(trackBlahCollectionName));

            // TODO this collection could be a capped collection for production, but there are many restrictions (e.g., not shardable):
            collectionNameToCollectionMap.put(trackCommentCollectionName, getTrackerDb().getCollection(trackCommentCollectionName));

            // TODO this collection could be a capped collection for production, but there are many restrictions (e.g., not shardable):
            collectionNameToCollectionMap.put(trackUserCollectionName, getTrackerDb().getCollection(trackUserCollectionName));

            collectionNameToCollectionMap.put(trackerCollectionName, getTrackerDb().getCollection(trackerCollectionName));

            this.status = ManagerState.STARTED;

            System.out.println("*** MongoStoreManager started *** (connected to MongoDB at " + hostname + ":" + port +
                    " for dbs: " + dbNameToDbMap + ") # pooled connections=" + connectionsPerHost);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        mongo.close();
        this.status = ManagerState.SHUTDOWN;
        System.out.println("*** MongoStoreManager shut down ***");
    }

    public ManagerState getState() {
        return this.status;
    }

    public DB getSysDb() {
        return dbNameToDbMap.get(sysDbName);
    }

    public DB getUserDb() {
        return dbNameToDbMap.get(userDbName);
    }

    public DB getBlahDb() {
        return dbNameToDbMap.get(blahDbName);
    }

//    public DB getMediaDb() {
//        return dbNameToDbMap.get(mediaDbName);
//    }

    public DB getTrackerDb() {
        return dbNameToDbMap.get(trackerDbName);
    }

    public DBCollection getCollection(String name) {
        return collectionNameToCollectionMap.get(name);
    }

    // FACTORY METHODS -------------------------------------------------------------------------------------

    @Override
    public GroupTypeDAO createGroupType() throws SystemErrorException {
        return new GroupTypeDAOImpl();
    }

    @Override
    public GroupTypeDAO createGroupType(String groupTypeId) throws SystemErrorException {
        return new GroupTypeDAOImpl(groupTypeId);
    }

    @Override
    public GroupTypeDAO createGroupType(Map<String, Object> map) throws SystemErrorException {
        return new GroupTypeDAOImpl(map, true);
    }

    @Override
    public GroupDAO createGroup() throws SystemErrorException {
        return new GroupDAOImpl();
    }

    @Override
    public GroupDAO createGroup(String groupId) throws SystemErrorException {
        return new GroupDAOImpl(groupId);
    }

    @Override
    public GroupDAO createGroup(Map<String, Object> map) throws SystemErrorException {
        return new GroupDAOImpl(map, true);
    }

    @Override
    public UserDAO createUser(String userId) throws SystemErrorException {
        return new UserDAOImpl(userId);
    }

    @Override
    public UserProfileDAO createUserProfile() {
        return new UserProfileDAOImpl();
    }

    @Override
    public UserProfileDAO createUserProfile(Map<String, Object> map) throws SystemErrorException {
        return new UserProfileDAOImpl(map, true);
    }

    @Override
    public UserProfileDAO createUserProfile(String profileId) throws SystemErrorException {
        return new UserProfileDAOImpl(profileId);
    }

    @Override
    public UserDAO createUser(Map<String, Object> map) throws SystemErrorException {
        return new UserDAOImpl(map, true);
    }

    @Override
    public UserDAO createUser() {
        return new UserDAOImpl();
    }

    @Override
    public UserGroupDAO createUserGroup() {
        return new UserGroupDAOImpl();
    }

    @Override
    public UserGroupDAO createUserGroup(String userGroupId) throws SystemErrorException {
        return new UserGroupDAOImpl(userGroupId);
    }

    @Override
    public UserGroupDAO createUserGroup(String userId, String groupId) throws SystemErrorException {
        UserGroupDAOImpl dao = new UserGroupDAOImpl();
        dao.setUserId(userId);
        dao.setGroupId(groupId);
        return dao;
    }

    @Override
    public UserGroupDAO createUserGroup(Map<String, Object> map) throws SystemErrorException {
        return new UserGroupDAOImpl(map, true);
    }

    @Override
    public MediaDAO createMedia() {
        return new MediaDAOImpl();
    }

    @Override
    public BlahDAO createBlah() {
        return new BlahDAOImpl();
    }

    @Override
    public BlahDAO createBlah(String blahId) throws SystemErrorException {
        return new BlahDAOImpl(blahId);
    }

    @Override
    public BlahDAO createBlah(Map<String, Object> map) throws SystemErrorException {
        return new BlahDAOImpl(map, true);
    }

    @Override
    public PollOptionTextDAO createPollOption() {
        return new PollOptionTextImpl();
    }

    @Override
    public PollOptionTextDAO createPollOption(String tagLine, String text) {
        return new PollOptionTextImpl(tagLine, text);
    }

    @Override
    public UserBlahInfoDAO createUserBlahInfo() {
        return new UserBlahInfoDAOImpl();
    }

    @Override
    public UserBlahInfoDAO createUserBlahInfo(String userId, String blahId) {
        UserBlahInfoDAOImpl dao = new UserBlahInfoDAOImpl();
        dao.setUserId(userId);
        dao.setBlahId(blahId);
        return dao;
    }

    @Override
    public UserBlahInfoDAO createUserBlahInfo(Map<String, Object> map) throws SystemErrorException {
        return new UserBlahInfoDAOImpl(map, true);
    }

    @Override
    public UserCommentInfoDAO createUserCommentInfo(String userId, String commentId) {
        UserCommentInfoDAOImpl dao = new UserCommentInfoDAOImpl();
        dao.setUserId(userId);
        dao.setCommentId(commentId);
        return dao;
    }

    @Override
    public UserCommentInfoDAO createUserCommentInfo(Map<String, Object> map) throws SystemErrorException {
        return new UserCommentInfoDAOImpl(map, true);
    }

    @Override
    public CommentDAO createComment() {
        return new CommentDAOImpl();
    }

    @Override
    public CommentDAO createComment(String commentId) throws SystemErrorException {
        return new CommentDAOImpl(commentId);
    }

    @Override
    public CommentDAO createComment(Map<String, Object> map) throws SystemErrorException {
        return new CommentDAOImpl(map, true);
    }

    @Override
    public UserTrackerDAO createUserTracker() throws SystemErrorException {
        return new UserTrackerDAOImpl();
    }

    @Override
    public UserTrackerDAO createUserTracker(String trackerId) throws SystemErrorException {
        return new UserTrackerDAOImpl(trackerId);
    }

    @Override
    public UserTrackerDAO createUserTracker(Map<String, Object> map) throws SystemErrorException {
        return new UserTrackerDAOImpl(map, true);
    }

    @Override
    public InboxBlahDAO createInboxBlah() {
        return new InboxBlahDAOImpl();
    }

    @Override
    public BlahTypeDAO createBlahType() {
        return new BlahTypeDAOImpl();
    }

    @Override
    public CommentTrackerDAO createCommentTracker() {
        return new CommentTrackerDAOImpl();
    }

    @Override
    public CommentTrackerDAO createCommentTracker(String trackerId) throws SystemErrorException {
        return new CommentTrackerDAOImpl(trackerId);
    }

    @Override
    public BlahTrackerDAO createBlahTracker() {
        return new BlahTrackerDAOImpl();
    }

    @Override
    public BlahTrackerDAO createBlahTracker(String trackerId) throws SystemErrorException {
        return new BlahTrackerDAOImpl(trackerId);
    }

    @Override
    public InboxStateDAO createInboxState() {
        return new InboxStateDAOImpl();
    }

    @Override
    public TrackerDAO createTracker(TrackerOperation operation) throws SystemErrorException {
        return new TrackerDAOImpl(operation);
    }

    @Override
    public TrackerDAO createTracker(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        return new TrackerDAOImpl(map, validateAndConvert);
    }

}
