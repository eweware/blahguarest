package com.eweware.service.base.store.impl.mongo.dao;

import com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.payload.AuthorizedState;
import com.eweware.service.base.store.dao.UserGroupDAO;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.dao.schema.UserGroupSchema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 */
public class UserGroupDAOImpl extends BaseDAOImpl implements UserGroupDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>(5);

    static {   // TODO should be derived from schema
        UserGroupDAOImpl.FIELD_TO_TYPE_MAP.put(GROUP_ID, MongoFieldTypes.STRING);
        UserGroupDAOImpl.FIELD_TO_TYPE_MAP.put(USER_ID, MongoFieldTypes.STRING);
//        UserGroupDAOImpl.FIELD_TO_TYPE_MAP.put(VALIDATION_CODE, MongoFieldTypes.STRING);
        UserGroupDAOImpl.FIELD_TO_TYPE_MAP.put(STATE, MongoFieldTypes.STRING);
        UserGroupDAOImpl.FIELD_TO_TYPE_MAP.put(FIRST_INBOX_NUMBER, MongoFieldTypes.NUMBER);
        UserGroupDAOImpl.FIELD_TO_TYPE_MAP.put(LAST_INBOX_NUMBER, MongoFieldTypes.NUMBER);
        addInheritedFieldToTypeMapItems(FIELD_TO_TYPE_MAP);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return UserGroupDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (UserGroupDAOImpl.collectionName == null) {
            UserGroupDAOImpl.collectionName = MongoStoreManager.getInstance().getUserGroupCollectionName();
        }
        return UserGroupDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (UserGroupDAOImpl.collection == null) {
            try {
                UserGroupDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
            } catch (SystemErrorException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return UserGroupDAOImpl.collection;
    }

    @Override
    public Integer getFirstInboxNumber() {
        return (Integer) get(FIRST_INBOX_NUMBER);
    }

    @Override
    public void setFirstInboxNumber(Integer number) {
        put(FIRST_INBOX_NUMBER, number);
    }

    @Override
    public Integer getLastInboxNumber() {
        return (Integer) get(LAST_INBOX_NUMBER);
    }

    @Override
    public void setLastInboxNumber(Integer number) {
        put(LAST_INBOX_NUMBER, number);
    }

    UserGroupDAOImpl() {
        super();
    }

    UserGroupDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    UserGroupDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return UserGroupSchema.getSchema(localeId);
    }

    @Override
    public String getUserId() {
        return (String) get(USER_ID);
    }

    @Override
    public void setUserId(String userId) {
        put(USER_ID, userId);
    }

//    @Override
//    public String getValidationCode() {
//        return (String) get(VALIDATION_CODE);
//    }
//
//    @Override
//    public void setValidationCode(String validationCode) {
//        put(VALIDATION_CODE, validationCode);
//    }

    @Override
    public String getGroupId() {
        return (String) get(GROUP_ID);
    }

    @Override
    public void setGroupId(String groupId) {
        put(GROUP_ID, groupId);
    }

    @Override
    public String getGroupDisplayName() {
        return (String) get(GROUP_DISPLAY_NAME);
    }

    @Override
    public void setGroupDisplayName(String name) {
        put(GROUP_DISPLAY_NAME, name);
    }

    @Override
    public String getState() {
        return (String) get(STATE);
    }

    @Override
    public void setState(String state) {
        put(STATE, state);
    }

    /**
     * @param col
     * @param userId
     * @param state  A state or null if any state.
     * @return List<String> A possibly empty list of group ids matching the description
     * @throws com.eweware.service.base.error.SystemErrorException
     *
     */
    public static List<String> findUserGroupIds(DBCollection col, String userId, AuthorizedState state) throws SystemErrorException {
        DBObject query = new BasicDBObject(USER_ID, userId);
        if (state != null) {
            query.put(STATE, state.toString());
        }
        DBCursor cursor = col.find(query); //, new BasicDBObject(FieldNames.USER_TO_GROUPS_GROUP_ID, 1));
        List<String> groupIds = new ArrayList<String>(cursor.size());
        while (cursor.hasNext()) {
            DBObject groupId = cursor.next();
            groupIds.add((String) groupId.get(GROUP_ID));
        }
        return groupIds;
    }

    public static final DBObject makeQuery(String userId, String groupId) {
        DBObject query = new BasicDBObject(USER_ID, userId);
        query.put(GROUP_ID, groupId);
        return query;
    }

    public static final DBObject makeQuery(String userId, String groupId, AuthorizedState state) {
        DBObject query = UserGroupDAOImpl.makeQuery(userId, groupId);
        if (state != null) {
            query.put(STATE, state.toString());
        }
        return query;
    }
}
