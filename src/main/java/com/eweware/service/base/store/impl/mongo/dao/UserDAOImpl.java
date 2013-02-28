package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.UserDAO;
import main.java.com.eweware.service.base.store.dao.UserTrackerDAO;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.UserSchema;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 */
public class UserDAOImpl extends BaseDAOImpl implements UserDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>(4);

    static {  // TODO should be derived from schema
        UserDAOImpl.FIELD_TO_TYPE_MAP.put(USERNAME, MongoFieldTypes.STRING);
        UserDAOImpl.FIELD_TO_TYPE_MAP.put(LAST_INBOX, MongoFieldTypes.NUMBER);
        UserDAOImpl.FIELD_TO_TYPE_MAP.put(USER_STRENGTH, MongoFieldTypes.NUMBER);
        UserDAOImpl.FIELD_TO_TYPE_MAP.put(USER_CONTROVERSY_STRENGTH, MongoFieldTypes.NUMBER);
        UserDAOImpl.FIELD_TO_TYPE_MAP.put(CREATED, MongoFieldTypes.DATE);
        UserDAOImpl.FIELD_TO_TYPE_MAP.put(UPDATED, MongoFieldTypes.DATE);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return UserDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (UserDAOImpl.collectionName == null) {
            UserDAOImpl.collectionName = MongoStoreManager.getInstance().getUserCollectionName();
        }
        return UserDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (UserDAOImpl.collection == null) {
            UserDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return UserDAOImpl.collection;
    }


    UserDAOImpl() {
        super();
    }

    UserDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    UserDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return UserSchema.getSchema(localeId);
    }

    @Override
    public String getUsername() {
        return (String) get(USERNAME);
    }

    @Override
    public void setUsername(String username) {
        put(USERNAME, username);
    }

    @Override
    public Integer getLastInbox() {
        return (Integer) get(LAST_INBOX);
    }

    @Override
    public void setLastInbox(Integer nextInbox) {
        put(LAST_INBOX, nextInbox);
    }

    @Override
    public List<UserTrackerDAO> getStats() {
        return (List<UserTrackerDAO>) get(STATS);
    }

    @Override
    public void setStats(List<UserTrackerDAO> stats) {
        put(STATS, stats);
    }

    @Override
    public Double getStrength() {
        return (Double) get(USER_STRENGTH);
    }

    @Override
    public void setStrength(Double strength) {
        put(USER_STRENGTH, strength);
    }

    @Override
    public Double getControversyStrength() {
        return (Double) get(USER_CONTROVERSY_STRENGTH);
    }

    @Override
    public void setControversyStrength(Double strength) {
        put(USER_CONTROVERSY_STRENGTH, strength);
    }

    @Override
    public Map<String, Object> toMap() {
        return super.toMap();
    }
}
