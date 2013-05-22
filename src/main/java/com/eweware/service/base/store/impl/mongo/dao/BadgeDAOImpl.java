package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.store.dao.BadgeDAO;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 3/18/13 Time: 7:46 PM
 */
public class BadgeDAOImpl extends BaseDAOImpl implements BadgeDAO {

    private static String collectionName;
    private static DBCollection collection;
    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>(4);

    static {
        FIELD_TO_TYPE_MAP.put(AUTHORITY_BADGE_ID, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(AUTHORITY_ID, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(AUTHORITY_DISPLAY_NAME, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(DISPLAY_NAME, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(BADGE_TYPE, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(ICON_URL, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(USER_ID, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(EXPIRATION_DATE, MongoFieldTypes.DATE);
        addInheritedFieldToTypeMapItems(FIELD_TO_TYPE_MAP);
    }

    public BadgeDAOImpl() {
    }

    public BadgeDAOImpl(String badgeId) throws SystemErrorException {
        super(badgeId);
    }

    public BadgeDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }



    @Override
    public String getAuthorityBadgeId() {
        return (String) get(AUTHORITY_BADGE_ID);
    }

    @Override
    public void setAuthorityBadgeId(String badgeId) {
        put(AUTHORITY_BADGE_ID, badgeId);
    }

    @Override
    public String getAuthorityId() {
        return (String) get(AUTHORITY_ID);
    }

    @Override
    public void setAuthorityId(String authorityId) {
        put(AUTHORITY_ID, authorityId);
    }

    @Override
    public String getAuthorityDisplayName() {
        return (String) get(AUTHORITY_DISPLAY_NAME);
    }

    @Override
    public void setAuthorityDisplayName(String displayName) {
        put(AUTHORITY_DISPLAY_NAME, displayName);
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
    public String getBadgeType() {
        return (String) get(BADGE_TYPE);
    }

    @Override
    public void setBadgeType(String badgeType) {
        put(BADGE_TYPE, badgeType);
    }

    @Override
    public String getIconUrl() {
        return (String) get(ICON_URL);
    }

    @Override
    public void setIconUrl(String iconUrl) {
        put(ICON_URL, iconUrl);
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
    public Date getExpirationDate() {
        return (Date) get(EXPIRATION_DATE);
    }

    @Override
    public void setExpirationDate(Date expirationDate) {
        put(EXPIRATION_DATE, expirationDate);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return BadgeDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (collectionName == null) {
            collectionName = MongoStoreManager.getInstance().getBadgeCollectionName();
        }
        return collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (collection == null) {
            collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return collection;
    }
}
