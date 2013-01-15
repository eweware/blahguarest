package main.java.com.eweware.service.mgr;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.mgr.ManagerState;
import main.java.com.eweware.service.base.payload.BlahTypePayload;
import main.java.com.eweware.service.base.store.dao.BaseDAO;
import main.java.com.eweware.service.base.store.dao.demo.DemographicsObjectDAOConstants;
import main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager;

import javax.xml.ws.WebServiceException;
import java.util.List;

/**
 * @author rk@post.harvard.edu
 *         Date: 10/1/12 Time: 11:23 AM
 */
public class StatisticsManager implements ManagerInterface {

    private static StatisticsManager singleton;
    private ManagerState state = ManagerState.UNINITIALIZED;
    private MongoStoreManager storeManager;
    private DBCollection demoCollection;

    public static StatisticsManager getInstance() throws SystemErrorException {
        if (StatisticsManager.singleton == null) {
            throw new SystemErrorException("statistics manager not initialized", ErrorCodes.SERVER_NOT_INITIALIZED);
        }
        return StatisticsManager.singleton;
    }

    public StatisticsManager() {
        StatisticsManager.singleton = this;
        this.state = ManagerState.INITIALIZED;
        System.out.println("*** StatisticsManager initialized ***");
    }

    @Override
    public void start() {
        try {
            storeManager = MongoStoreManager.getInstance();
            this.demoCollection = storeManager.getCollection(storeManager.getDemographicsCollectionName());
            this.state = ManagerState.STARTED;
            System.out.println("*** StatisticsManager started ***");
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
    }

    @Override
    public void shutdown() {
        this.state = ManagerState.SHUTDOWN;
        System.out.println("*** StatisticsManager shut down ***");
    }

    public Object getGroupDemographics(String groupId) throws InvalidRequestException {
        if (groupId == null) {
            throw new InvalidRequestException("missing group id", ErrorCodes.MISSING_GROUP_ID);
        }
        return getDemographicsForObject(groupId, DemographicsObjectDAOConstants.BLAH_PER_GROUP_FIELD_NAME_PREFIXES);
    }

    public Object getBlahDemographics(boolean type) throws SystemErrorException {
        if (type) {
            return getBlahTypeDemographics();
        }
        return getDemographics(DemographicsObjectDAOConstants.BLAH_DEMOGRAPHIC_IDS);
    }

    public Object getCommentDemographics() {
        return getDemographics(DemographicsObjectDAOConstants.COMMENT_DEMOGRAPHIC_IDS);
    }


    private Object getDemographics(String[] ids) {
        if (ids == null) {
            return new Object[0];
        }
        final BasicDBObject clause = new BasicDBObject("$in", ids);
        final DBObject query = new BasicDBObject(BaseDAO.ID, clause);
        final DBCursor cursor = demoCollection.find(query);
        final Object[] demos = new Object[cursor.count()];
        int i = 0;
        for (DBObject rec : cursor) {
            demos[i++] = rec;
        }
        return demos;
    }

    private Object getDemographicsForObject(String objectId, String[] idPrefixes) {
        final String[] ids = new String[idPrefixes.length];
        for (int i = 0; i < idPrefixes.length; i++) {
            ids[i] = idPrefixes[i] + objectId;
        }
        return getDemographics(ids);
    }

    public Object getBlahTypeDemographics() throws SystemErrorException {
        final List<BlahTypePayload> types = BlahManager.getInstance().getCachedBlahTypes();
        final String[] ids = new String[types.size()];
        int i = 0;
        for (BlahTypePayload bt : types) {
            ids[i++] = bt.getId();
        }
        return getDemographics(ids);
    }
}
