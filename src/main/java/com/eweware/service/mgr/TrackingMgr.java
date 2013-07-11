package main.java.com.eweware.service.mgr;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.mgr.ManagerInterface;
import main.java.com.eweware.service.base.mgr.ManagerState;
import main.java.com.eweware.service.base.store.dao.TrackerDAOConstants;
import main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;

import javax.xml.ws.WebServiceException;
import java.util.Date;

/**
 * @author rk@post.harvard.edu
 *         Date: 5/9/13 Time: 6:43 PM
 */
public final class TrackingMgr implements ManagerInterface {

    private static TrackingMgr singleton;

    private DBCollection _trackerCollection;
    private ManagerState _state = ManagerState.UNKNOWN;

    /**
     * Empty constructor.
     */
    public TrackingMgr() {
        TrackingMgr.singleton = this;
        _state = ManagerState.INITIALIZED;
    }

    public static TrackingMgr getInstance() {
        return singleton;
    }

    @Override
    public void start() {
        try {
            final MongoStoreManager storeManager = MongoStoreManager.getInstance();
            _trackerCollection = storeManager.getCollection(storeManager.getTrackerCollectionName());
            _state = ManagerState.STARTED;
            System.out.println("*** TrackingMgr Started ***");
        } catch (Exception e) {
            throw new WebServiceException("Failed to start", e);
        }
    }

    @Override
    public void shutdown() {
        System.out.println("*** TrackingMgr Shutdown ***");
    }

    public void trackBlahUpdate(String blahId, String userId, Long promoted, Long viewCount, Long openCount, Long pollVotedIndex) throws SystemErrorException {
        ensureReady();
        if (userId == null) {
            userId = "a";  // anonymous
        }
        final BasicDBObject tracker = new BasicDBObject(TrackerDAOConstants.BLAH_ID, blahId);
        tracker.put(TrackerDAOConstants.CREATED, new Date());
        tracker.put(TrackerDAOConstants.USER_ID, userId);
        if (promoted != null && promoted != 0) {
            tracker.put(TrackerDAOConstants.PROMOTION, promoted);
        }
        if (viewCount != null && viewCount != 0) {
            tracker.put(TrackerDAOConstants.VIEWS, viewCount);
        }
        if (openCount != null && openCount != 0) {
            tracker.put(TrackerDAOConstants.OPENS, openCount);
        }
        if (pollVotedIndex != null && pollVotedIndex != 0) {
            tracker.put(TrackerDAOConstants.VOTED_POLL_INDEX, pollVotedIndex);
        }
        try {
            _trackerCollection.insert(tracker);
        } catch (Exception e) {
            throw new SystemErrorException("Error creating blah update tracker for blah id '" + blahId + "'", e, ErrorCodes.SERVER_DB_ERROR);
        }
    }

    private void ensureReady() throws SystemErrorException {
        if (_state != ManagerState.STARTED) {
            throw new SystemErrorException("System not ready", ErrorCodes.SERVER_NOT_INITIALIZED);
        }
    }
}
