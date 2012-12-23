package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.GroupDAO;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.GroupSchema;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 */
public class GroupDAOImpl extends BaseDAOImpl implements GroupDAO {

    private static final long serialVersionUID = 1L;

    private static String collectionName;
    private static DBCollection collection;

    public static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>();

    static {   // TODO should be derived from schema
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(GROUP_TYPE_ID, MongoFieldTypes.STRING);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(DISPLAY_NAME, MongoFieldTypes.STRING);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(DESCRIPTION, MongoFieldTypes.STRING);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(USER_VALIDATION_METHOD, MongoFieldTypes.STRING);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(USER_VALIDATION_PARAMETERS, MongoFieldTypes.STRING);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(STATE, MongoFieldTypes.STRING);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(USER_COUNT, MongoFieldTypes.NUMBER);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(BLAH_COUNT, MongoFieldTypes.NUMBER);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(CREATED, MongoFieldTypes.DATE);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(UPDATED, MongoFieldTypes.DATE);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return GroupDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (GroupDAOImpl.collectionName == null) {
            GroupDAOImpl.collectionName = MongoStoreManager.getInstance().getGroupCollectionName();
        }
        return GroupDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (GroupDAOImpl.collection == null) {
            try {
                GroupDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
            } catch (SystemErrorException e) {
                e.printStackTrace();  // TODO catch
            }
        }
        return GroupDAOImpl.collection;
    }

    GroupDAOImpl() {
        super();
    }

    GroupDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    GroupDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return GroupSchema.getSchema(localeId);
    }

    @Override
    public String getGroupTypeId() {
        return (String) get(GROUP_TYPE_ID);
    }

    @Override
    public void setGroupTypeId(String groupTypeId) {
        put(GROUP_TYPE_ID, groupTypeId);
    }

    @Override
    public String getDisplayName() {
        return (String) get(DISPLAY_NAME);
    }

    @Override
    public void setDisplayName(String displayName) {
        put(DISPLAY_NAME, displayName);
    }

    @Override
    public String getDescription() {
        return (String) get(DESCRIPTION);
    }

    @Override
    public void setDescription(String description) {
        put(DESCRIPTION, description);
    }

    @Override
    public String getState() {
        return (String) get(STATE);
    }

    @Override
    public void setState(String state) {
        put(STATE, state);
    }

    @Override
    public Integer getUserCount() {
        return (Integer) get(USER_COUNT);
    }

    @Override
    public void setUserCount(Integer userCount) {
        put(USER_COUNT, userCount);
    }

    @Override
    public Integer getBlahCount() {
        return (Integer) get(BLAH_COUNT);
    }

    @Override
    public void setBlahCount(Integer blahCount) {
        put(BLAH_COUNT, blahCount);
    }

    @Override
    public String getValidationMethod() {
        return (String) get(USER_VALIDATION_METHOD);
    }

    @Override
    public void setValidationMethod(String method) {
        put(USER_VALIDATION_METHOD, method);
    }

    @Override
    public String getValidationParameters() {
        return (String) get(USER_VALIDATION_PARAMETERS);
    }

    @Override
    public void setValidationParameters(String params) {
        put(USER_VALIDATION_PARAMETERS, params);
    }

    @Override
    public Map<String, Object> toMap() {
        return super.toMap();
    }
}
