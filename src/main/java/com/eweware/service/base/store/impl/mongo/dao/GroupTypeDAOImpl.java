package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.GroupTypeDAO;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.GroupTypeSchema;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 */
public class GroupTypeDAOImpl extends BaseDAOImpl implements GroupTypeDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>(2);

    static {  // TODO should be derived from schema
        GroupTypeDAOImpl.FIELD_TO_TYPE_MAP.put(DISPLAY_NAME, MongoFieldTypes.STRING);
        GroupTypeDAOImpl.FIELD_TO_TYPE_MAP.put(GROUP_COUNT, MongoFieldTypes.NUMBER);
        addInheritedFieldToTypeMapItems(FIELD_TO_TYPE_MAP);
    }

    @Override
    public Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return GroupTypeDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (GroupTypeDAOImpl.collectionName == null) {
            GroupTypeDAOImpl.collectionName = MongoStoreManager.getInstance().getGroupTypeCollectionName();
        }
        return GroupTypeDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (GroupTypeDAOImpl.collection == null) {
            GroupTypeDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return GroupTypeDAOImpl.collection;
    }

    GroupTypeDAOImpl() {
        super();
    }

    GroupTypeDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    GroupTypeDAOImpl(Map<String, Object> obj, boolean validateAndConvert) throws SystemErrorException {
        super(obj, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return GroupTypeSchema.getSchema(localeId);
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
    public Long getGroupCount() {
        return (Long) get(GROUP_COUNT);
    }

    @Override
    public void setGroupCount(Long groupCount) {
        put(GROUP_COUNT, groupCount);
    }
}