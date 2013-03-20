package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.store.dao.BadgeDAO;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 3/18/13 Time: 7:46 PM
 */
public class BadgeDAOImpl extends BaseDAOImpl implements BadgeDAO {

    public BadgeDAOImpl() {
    }

    public BadgeDAOImpl(String badgeId) throws SystemErrorException {
        super(badgeId);
    }

    public BadgeDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }


    @Override
    public String getBadgeId() {
        return (String) get(BADGE_ID);
    }

    @Override
    public void setBadgeId(String badgeId) {
        put(BADGE_ID, badgeId);
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
    public String getReason() {
        return (String) getReason();
    }

    @Override
    public void setReason(String reason) {
        put(REASON, reason);
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
    public String getUserToken() {
        return (String) get(USER_TOKEN);
    }

    @Override
    public void setUserToken(String userToken) {
        put(USER_TOKEN, userToken);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return null;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (collectionName == null) {
            collectionName = MongoStoreManager.getInstance().getUserCollectionName();
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

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>(4);
    static {
        FIELD_TO_TYPE_MAP.put(BADGE_ID, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(AUTHORITY_ID, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(DISPLAY_NAME, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(DESCRIPTION, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(STATE, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(REASON, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(ICON_URL, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(USER_TOKEN, MongoFieldTypes.STRING);
    }
}
