package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.UserBlahInfoDAO;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.UserBlahInfoSchema;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/3/12 Time: 9:15 PM
 */
public class UserBlahInfoDAOImpl extends BaseDAOImpl implements UserBlahInfoDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>();

    static {  // TODO should be derived from schema
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(USER_ID, MongoFieldTypes.STRING);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(BLAH_ID, MongoFieldTypes.STRING);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(ORIGINAL_GROUP_ID, MongoFieldTypes.STRING);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(BLAH_TYPE_ID, MongoFieldTypes.STRING);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(VOTE, MongoFieldTypes.NUMBER);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(VIEWS, MongoFieldTypes.NUMBER);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(OPENS, MongoFieldTypes.NUMBER);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(COMMENTS_ON_THIS_BLAH, MongoFieldTypes.NUMBER);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(CREATED, MongoFieldTypes.DATE);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(UPDATED, MongoFieldTypes.DATE);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (UserBlahInfoDAOImpl.collectionName == null) {
            UserBlahInfoDAOImpl.collectionName = MongoStoreManager.getInstance().getUserBlahInfoCollectionName();
        }
        return UserBlahInfoDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (UserBlahInfoDAOImpl.collection == null) {
            UserBlahInfoDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return UserBlahInfoDAOImpl.collection;
    }

    UserBlahInfoDAOImpl() {
        super();
    }

    UserBlahInfoDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    UserBlahInfoDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return UserBlahInfoSchema.getSchema(localeId);
    }

    @Override
    public String getUserId() {
        return (String) get(USER_ID);
    }

    @Override
    public void setUserId(String userId) {
        put(USER_ID, userId);
    }

    @Override
    public String getBlahId() {
        return (String) get(BLAH_ID);
    }

    @Override
    public void setBlahId(String blahId) {
        put(BLAH_ID, blahId);
    }

    @Override
    public String getGroupId() {
        return (String) get(ORIGINAL_GROUP_ID);
    }

    @Override
    public void setGroupId(String groupId) {
        put(ORIGINAL_GROUP_ID, groupId);
    }

    @Override
    public String getBlahTypeId() {
        return (String) get(BLAH_TYPE_ID);
    }

    @Override
    public void setBlahTypeId(String blahTypeId) {
        put(BLAH_TYPE_ID, blahTypeId);
    }

    @Override
    public Integer getVote() {
        return (Integer) get(VOTE);
    }

    @Override
    public void setVote(Integer vote) {
        put(VOTE, vote);
    }

    @Override
    public Integer getViews() {
        return (Integer) get(VIEWS);
    }

    @Override
    public void setViews(Integer count) {
        put(VIEWS, count);
    }

    @Override
    public Integer getOpens() {
        return (Integer) get(OPENS);
    }

    @Override
    public void setOpens(Integer count) {
        put(OPENS, count);
    }

    @Override
    public Integer getComments() {
        return (Integer) get(COMMENTS_ON_THIS_BLAH);
    }

    @Override
    public void setComments(Integer comments) {
        put(COMMENTS_ON_THIS_BLAH, comments);
    }
}
