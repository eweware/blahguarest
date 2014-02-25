package com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.*;
import com.eweware.service.base.error.ErrorCodes;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.mgr.ManagerState;
import com.eweware.service.base.mgr.SystemManager;
import com.eweware.service.base.store.StoreManager;
import com.eweware.service.base.store.dao.*;
import com.eweware.service.base.type.RunMode;
import org.bson.types.ObjectId;

import javax.net.SocketFactory;
import javax.xml.ws.WebServiceException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p/>
 * Store manager controls MongoDB access.
 * Initialized as a Spring bean.
 * <p/>
 *
 * @author rk@post.harvard.edu
 */
public final class MongoStoreManager implements StoreManager {

    private static final Logger logger = Logger.getLogger(MongoStoreManager.class.getName());

    // Keep it simple for now: only one type and one instance allowed
    protected static MongoStoreManager singleton;
    private String qaMongoDbHostname;
    private String devMongoDbHostname;
    private List<String> hostnames;

    public static MongoStoreManager getInstance() throws SystemErrorException {
        if (MongoStoreManager.singleton == null) {
            throw new SystemErrorException("mongo store manager not initialized");
        }
        return MongoStoreManager.singleton;
    }

    private ManagerState status = ManagerState.UNKNOWN;
    private int mongoDbPort;
    private Integer connectionsPerHost = 100; // default
    private MongoClient mongo;
    private Map<String, DB> dbNameToDbMap;

    private String sysDbName;
    private String userDbName;
    private String blahDbName;
    private String trackerDbName;
    private String inboxDbName;

    private String badgeAuthorityCollectionName;
    private String badgeTransactionCollectionName;
    private String badgeCollectionName;
    private String inboxStateCollectionName;
    private String mediaCollectionName;
    private String blahCollectionName;
    private String blahTypeCollectionName;
    private String whatsNewCollectionName;
    private String groupCollectionName;
    private String groupTypeCollectionName;
    private String userCollectionName;
    private String userAccountsCollectionName;
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

            String hostnames,
            String qaMongoDbHostname,
            String devMongoDbHostname,
            String mongoDbPort,
            String userDbName,
            String blahDbName,
            String inboxDbName,
            String trackerDbName,

            Integer connectionsPerHost
    ) {
        this.qaMongoDbHostname = qaMongoDbHostname;
        this.devMongoDbHostname = devMongoDbHostname;
        doInitialize(hostnames, mongoDbPort, userDbName, blahDbName, inboxDbName,trackerDbName, connectionsPerHost);
    }


    public String getInboxStateCollectionName() {
        return inboxStateCollectionName;
    }

    public String getBadgeAuthorityCollectionName() {
        return badgeAuthorityCollectionName;
    }

    public void setBadgeAuthorityCollectionName(String name) {
        badgeAuthorityCollectionName = name;
    }

    public String getBadgeCollectionName() {
        return badgeCollectionName;
    }

    public void setBadgeCollectionName(String name) {
        badgeCollectionName = name;
    }

    public String getBadgeTransactionCollectionName() {
        return badgeTransactionCollectionName;
    }

    public void setBadgeTransactionCollectionName(String name) {
        badgeTransactionCollectionName = name;
    }

    public void setInboxStateCollectionName(String name) {
        inboxStateCollectionName = name;
    }

    public String getMediaCollectionName() {
        return mediaCollectionName;
    }

    public void setMediaCollectionName(String name) {
        mediaCollectionName = name;
    }

    public String getBlahCollectionName() {
        return blahCollectionName;
    }

    public void setBlahCollectionName(String name) {
        blahCollectionName = name;
    }

    public String getWhatsNewCollectionName() {
        return whatsNewCollectionName;
    }

    public void setWhatsNewCollectionName(String name) {
        whatsNewCollectionName = name;
    }
    public String getBlahTypeCollectionName() {
        return blahTypeCollectionName;
    }

    public void setBlahTypeCollectionName(String name) {
        blahTypeCollectionName = name;
    }

    public String getGroupCollectionName() {
        return groupCollectionName;
    }

    public void setGroupCollectionName(String name) {
        groupCollectionName = name;
    }

    public String getGroupTypeCollectionName() {
        return groupTypeCollectionName;
    }

    public void setGroupTypeCollectionName(String name) {
        groupTypeCollectionName = name;
    }

    public String getUserCollectionName() {
        return userCollectionName;
    }

    public void setUserCollectionName(String name) {
        userCollectionName = name;
    }

    public String getUserAccountsCollectionName() {
        return userAccountsCollectionName;
    }

    public void setUserAccountsCollectionName(String name) {
        userAccountsCollectionName = name;
    }

    public String getUserProfileCollectionName() {
        return userProfileCollectionName;
    }

    public void setUserProfileCollectionName(String name) {
        userProfileCollectionName = name;
    }

    public String getUserGroupCollectionName() {
        return userGroupCollectionName;
    }

    public void setUserGroupCollectionName(String name) {
        userGroupCollectionName = name;
    }

    public String getCommentCollectionName() {
        return commentCollectionName;
    }

    public void setCommentCollectionName(String name) {
        commentCollectionName = name;
    }

    public String getUserBlahInfoCollectionName() {
        return userBlahInfoCollectionName;
    }

    public void setUserBlahInfoCollectionName(String name) {
        userBlahInfoCollectionName = name;
    }

    public String getUserCommentInfoCollectionName() {
        return userCommentInfoCollectionName;
    }

    public void setUserCommentInfoCollectionName(String name) {
        userCommentInfoCollectionName = name;
    }

    public String getTrackBlahCollectionName() {
        return trackBlahCollectionName;
    }

    public void setTrackBlahCollectionName(String name) {
        trackBlahCollectionName = name;
    }

    public String getTrackCommentCollectionName() {
        return trackCommentCollectionName;
    }

    public void setTrackCommentCollectionName(String name) {
        trackCommentCollectionName = name;
    }

    public String getTrackerCollectionName() {
        return trackerCollectionName;
    }

    public void setTrackerCollectionName(String name) {
        trackerCollectionName = name;
    }

    public String getTrackUserCollectionName() {
        return trackUserCollectionName;
    }

    public void setTrackUserCollectionName(String name) {
        trackUserCollectionName = name;
    }

    public String getBlahInboxCollectionName() {
        return blahInboxCollectionName;
    }

    public void setBlahInboxCollectionName(String name) {
        blahInboxCollectionName = name;
    }

    public String getDemographicsCollectionName() {
        return demographicsCollectionName;
    }

    public void setDemographicsCollectionName(String name) {
        demographicsCollectionName = name;
    }


    /**
     * Initializes the store manager. This method is public to allow
     * test units to initialize it outside the context of the web server.
     *
     * @param hostnames
     * @param port
     * @param userDbName
     * @param blahDbName
     * @param trackerDbName
     * @param connectionsPerHost
     */
    public void doInitialize(String hostnames, String port, String userDbName, String blahDbName,
                             String inboxDbName, String trackerDbName, Integer connectionsPerHost) {

        if (hostnames == null || hostnames.length() == 0) {
            throw new WebServiceException("Failed to initialize store manager: missing hostnames");
        }
        this.hostnames = Arrays.asList(hostnames.split("\\|"));
        this.mongoDbPort = Integer.parseInt(port);
        this.connectionsPerHost = connectionsPerHost;

        this.dbNameToDbMap = new HashMap<String, DB>(3);
        this.sysDbName = "sysdb";
//        this.mediaDbName = mediaDbName;
        this.userDbName = userDbName;
        this.blahDbName = blahDbName;
        this.trackerDbName = trackerDbName;
        this.inboxDbName = inboxDbName;

        dbNameToDbMap.put(sysDbName, null);
        dbNameToDbMap.put(userDbName, null);
//        dbNameToDbMap.put(mediaDbName, null);
        dbNameToDbMap.put(blahDbName, null);
        dbNameToDbMap.put(trackerDbName, null);
        dbNameToDbMap.put(inboxDbName, null);

        System.out.println("*** STORE MGR: known databases: " + dbNameToDbMap.keySet());
        MongoStoreManager.singleton = this;
        this.status = ManagerState.INITIALIZED;
    }

    public static final ObjectId makeObjectId(String id) throws SystemErrorException {
        try {
            return new ObjectId(id);
        } catch (IllegalArgumentException e) {
            throw new SystemErrorException("the id is not valid (it must be an UUID string); id '" + id + "'", e, ErrorCodes.SERVER_DB_ERROR);
        }
    }

    public void start() {
        try {
            setMongoDebuggingLevel();
            SystemManager sysMgr = null;
            try {
                sysMgr = SystemManager.getInstance();
            } catch (Exception e) {
                throw new WebServiceException("Failed to find System Manager?", e);
            }

            final RunMode runMode = sysMgr.getRunMode();

            // Set up connections per host
            if (runMode != RunMode.PROD) {
                this.connectionsPerHost = 10;
                logger.info("*** MongoDB hostname: " + (runMode == RunMode.QA ? qaMongoDbHostname : devMongoDbHostname) + " port " + this.mongoDbPort);
            } else {
                logger.info("MongoDB hostnames '" + this.hostnames + "' port '" + this.mongoDbPort + "'");
            }
            logger.info("*** mongodb connections = " + this.connectionsPerHost + " ***");

            //  Configure db host addresses
            final MongoClientOptions.Builder builder = new MongoClientOptions.Builder().connectionsPerHost(connectionsPerHost);
            List<ServerAddress> serverAddresses = new ArrayList<ServerAddress>();
            if (runMode != RunMode.PROD) {
                serverAddresses.add(new ServerAddress((runMode == RunMode.QA ? qaMongoDbHostname : devMongoDbHostname), mongoDbPort));
            } else {
                for (String hostname : hostnames) {
                    serverAddresses.add(new ServerAddress(hostname, mongoDbPort));
                }
            }
            if (serverAddresses.size() == 1) {
                builder.writeConcern(WriteConcern.SAFE);
                logger.info("*** Connecting as a standalone hostname " + serverAddresses.get(0) + " at port " + mongoDbPort + " ***");
            } else if (serverAddresses.size() > 0) {
                builder
                        .readPreference(ReadPreference.primaryPreferred()) // tries to read from primary
                        .writeConcern(WriteConcern.SAFE);      // Writes to secondaries before returning
                logger.info("*** Connecting to hostnames in replica set: " + serverAddresses + " at port " + mongoDbPort + " ***");
            } else {
                throw new WebServiceException("Neither using replica nor using standalone DB");
            }
            builder.autoConnectRetry(true)
                    .connectTimeout(30000)
                    .socketKeepAlive(true);


            this.mongo = new MongoClient(serverAddresses, builder.build());


            // Add a hook to keep it independent of Spring
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mongo != null) {
                        mongo.close();
                    }
                }
            }));

            for (String dbname : dbNameToDbMap.keySet()) {
                dbNameToDbMap.put(dbname, mongo.getDB(dbname));
            }
            checkCollection(collectionNameToCollectionMap, groupCollectionName);
            collectionNameToCollectionMap.put(groupCollectionName, getUserDb().getCollection(groupCollectionName));

            checkCollection(collectionNameToCollectionMap, groupTypeCollectionName);
            collectionNameToCollectionMap.put(groupTypeCollectionName, getUserDb().getCollection(groupTypeCollectionName));

            checkCollection(collectionNameToCollectionMap, userCollectionName);
            collectionNameToCollectionMap.put(userCollectionName, getUserDb().getCollection(userCollectionName));

            checkCollection(collectionNameToCollectionMap, userAccountsCollectionName);
            collectionNameToCollectionMap.put(userAccountsCollectionName, getUserDb().getCollection(userAccountsCollectionName));

            checkCollection(collectionNameToCollectionMap, userProfileCollectionName);
            collectionNameToCollectionMap.put(userProfileCollectionName, getUserDb().getCollection(userProfileCollectionName));

            checkCollection(collectionNameToCollectionMap, userGroupCollectionName);
            collectionNameToCollectionMap.put(userGroupCollectionName, getUserDb().getCollection(userGroupCollectionName));

            checkCollection(collectionNameToCollectionMap, badgeAuthorityCollectionName);
            collectionNameToCollectionMap.put(badgeAuthorityCollectionName, getUserDb().getCollection(badgeAuthorityCollectionName));

            checkCollection(collectionNameToCollectionMap, badgeTransactionCollectionName);
            collectionNameToCollectionMap.put(badgeTransactionCollectionName, getUserDb().getCollection(badgeTransactionCollectionName));

            checkCollection(collectionNameToCollectionMap, badgeCollectionName);
            collectionNameToCollectionMap.put(badgeCollectionName, getUserDb().getCollection(badgeCollectionName));

            checkCollection(collectionNameToCollectionMap, blahCollectionName);
            collectionNameToCollectionMap.put(blahCollectionName, getBlahDb().getCollection(blahCollectionName));

            checkCollection(collectionNameToCollectionMap, mediaCollectionName);
            collectionNameToCollectionMap.put(mediaCollectionName, getBlahDb().getCollection(mediaCollectionName));

            checkCollection(collectionNameToCollectionMap, inboxStateCollectionName);
            collectionNameToCollectionMap.put(inboxStateCollectionName, getBlahDb().getCollection(inboxStateCollectionName));

            checkCollection(collectionNameToCollectionMap, blahTypeCollectionName);
            collectionNameToCollectionMap.put(blahTypeCollectionName, getBlahDb().getCollection(blahTypeCollectionName));

            checkCollection(collectionNameToCollectionMap, whatsNewCollectionName);
            collectionNameToCollectionMap.put(whatsNewCollectionName, getUserDb().getCollection(whatsNewCollectionName));

            checkCollection(collectionNameToCollectionMap, commentCollectionName);
            collectionNameToCollectionMap.put(commentCollectionName, getBlahDb().getCollection(commentCollectionName));

            checkCollection(collectionNameToCollectionMap, userBlahInfoCollectionName);
            collectionNameToCollectionMap.put(userBlahInfoCollectionName, getUserDb().getCollection(userBlahInfoCollectionName));

            checkCollection(collectionNameToCollectionMap, blahInboxCollectionName);
            collectionNameToCollectionMap.put(blahInboxCollectionName, getBlahDb().getCollection(blahInboxCollectionName));

            checkCollection(collectionNameToCollectionMap, demographicsCollectionName);
            collectionNameToCollectionMap.put(demographicsCollectionName, getTrackerDb().getCollection(demographicsCollectionName));

            checkCollection(collectionNameToCollectionMap, userCommentInfoCollectionName);
            collectionNameToCollectionMap.put(userCommentInfoCollectionName, getUserDb().getCollection(userCommentInfoCollectionName));

            checkCollection(collectionNameToCollectionMap, trackBlahCollectionName);
            collectionNameToCollectionMap.put(trackBlahCollectionName, getTrackerDb().getCollection(trackBlahCollectionName));

            checkCollection(collectionNameToCollectionMap, trackCommentCollectionName);
            collectionNameToCollectionMap.put(trackCommentCollectionName, getTrackerDb().getCollection(trackCommentCollectionName));

            checkCollection(collectionNameToCollectionMap, trackUserCollectionName);
            collectionNameToCollectionMap.put(trackUserCollectionName, getTrackerDb().getCollection(trackUserCollectionName));

            checkCollection(collectionNameToCollectionMap, trackerCollectionName);
            collectionNameToCollectionMap.put(trackerCollectionName, getTrackerDb().getCollection(trackerCollectionName));

            this.status = ManagerState.STARTED;

            System.out.println("*** MongoStoreManager started *** (connected to MongoDB using hostnames " + serverAddresses + ":" + mongoDbPort +
                    " for dbs: " + dbNameToDbMap + ") # pooled connections=" + connectionsPerHost);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkCollection(Map<String, DBCollection> map, String key) throws SystemErrorException {
        if (map.containsKey(key)) {
            throw new SystemErrorException("Collection name '" + key + "' is not unique. Collection names must be unique across all databases.");
        }

    }

    private void setMongoDebuggingLevel() {
        Logger logger = Logger.getLogger("com.mongodb");
        if (logger != null) {
            logger.setLevel(Level.FINEST); // TODO make configurable
        } else {
            logger.log(Level.WARNING, "Didn't find mongo logger at com.mongodb");
        }
    }

    public void shutdown() {
        if (mongo != null) {
            mongo.close();
        }
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

    public DB getInboxDB() {
        return dbNameToDbMap.get(inboxDbName);
    }

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
    public WhatsNewDAO createWhatsNew(String userId) throws SystemErrorException {
        return new WhatsNewDAOImpl(userId);
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
    public UserAccountDAO createUserAccount() throws SystemErrorException {
        return new UserAccountDAOImpl();
    }

    @Override
    public UserAccountDAO createUserAccount(String id) throws SystemErrorException {
        return new UserAccountDAOImpl(id);
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
    public BadgeAuthorityDAO createBadgeAuthority() throws SystemErrorException {
        return new BadgeAuthorityDAOImpl();
    }

    @Override
    public BadgeAuthorityDAO createBadgeAuthority(String authorityId) throws SystemErrorException {
        return new BadgeAuthorityDAOImpl(authorityId);
    }

    @Override
    public BadgeDAO createBadge() throws SystemErrorException {
        return new BadgeDAOImpl();
    }

    @Override
    public BadgeDAO createBadge(String badgeId) throws SystemErrorException {
        return new BadgeDAOImpl(badgeId);
    }

    @Override
    public MediaDAO createMedia() {
        return new MediaDAOImpl();
    }

    @Override
    public MediaDAO createMedia(String mediaId) throws SystemErrorException {
        return new MediaDAOImpl(mediaId);
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
}
