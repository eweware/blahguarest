package com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.UserDAO;
import com.eweware.service.base.store.dao.UserTrackerDAO;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.dao.schema.UserSchema;
import com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 */
public class UserDAOImpl extends BaseDAOImpl implements UserDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>();

    static {  // TODO should be derived from schema
        UserDAOImpl.FIELD_TO_TYPE_MAP.put(USERNAME, MongoFieldTypes.STRING);
        UserDAOImpl.FIELD_TO_TYPE_MAP.put(USER_STRENGTH, MongoFieldTypes.NUMBER);
        UserDAOImpl.FIELD_TO_TYPE_MAP.put(LAST_LOGIN, MongoFieldTypes.DATE);
        UserDAOImpl.FIELD_TO_TYPE_MAP.put(USER_CONTROVERSY_STRENGTH, MongoFieldTypes.NUMBER);
        UserDAOImpl.FIELD_TO_TYPE_MAP.put(BADGE_IDS, MongoFieldTypes.ARRAY);
        UserDAOImpl.FIELD_TO_TYPE_MAP.put(IMAGE_IDS, MongoFieldTypes.ARRAY);
        addInheritedFieldToTypeMapItems(FIELD_TO_TYPE_MAP);
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
    public Date getLastSignInDate() {
        return (Date) get(LAST_LOGIN);
    }

    @Override
    public void setLastSignInDate(Date lastLogin) {
        put(LAST_LOGIN, lastLogin);
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
    public List<String> getBadgeIds() {
        return (List<String>) get(BADGE_IDS);
    }

    @Override
    public void setBadgeIds(List<String> badgeIds) {
        put(BADGE_IDS, badgeIds);
    }

    @Override
    public List<String> getImageids() {
        return (List<String>) get(IMAGE_IDS);
    }

    @Override
    public void setImageIds(List<String> imageIds) {
        put(IMAGE_IDS, imageIds);
    }

    @Override
    public Boolean getIsAdmin() {
        return (Boolean) get(IS_ADMIN);
    }
}
