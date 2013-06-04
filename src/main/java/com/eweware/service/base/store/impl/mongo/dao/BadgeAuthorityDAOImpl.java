package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.store.dao.BadgeAuthorityDAO;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Implements the badge authority dao.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 3/18/13 Time: 4:09 PM                                                                                           `
 */
public class BadgeAuthorityDAOImpl extends BaseDAOImpl implements BadgeAuthorityDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>();

    static {
        FIELD_TO_TYPE_MAP.put(DISPLAY_NAME, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(DESCRIPTION, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(ENDPOINT_URL, MongoFieldTypes.STRING);
    }

    public BadgeAuthorityDAOImpl() {
    }

    public BadgeAuthorityDAOImpl(String authorityId) throws SystemErrorException {
        super(authorityId);
    }

    public BadgeAuthorityDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    @Override
    public void setId(String id) throws SystemErrorException {
        put(ID, id);
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
    public String getBadgeTypeDescription() {
        return (String) get(BADGE_TYPE_DESCRIPTION);
    }

    @Override
    public void setBadgeTypeDescription(String description) {
        put(BADGE_TYPE_DESCRIPTION, description);
    }

    @Override
    public String getEndpointUrl() {
        return (String) get(ENDPOINT_URL);
    }

    @Override
    public void setEndpointUrl(String endpointUrl) {
        put(ENDPOINT_URL, endpointUrl);
    }

    @Override
    public String getRestEndpointUrl() {
        return (String) get(REST_ENDPOINT_URL);
    }

    @Override
    public void setRestEndpointUrl(String restEndpointUrl) {
        put(REST_ENDPOINT_URL, restEndpointUrl);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (collectionName == null) {
            collectionName = MongoStoreManager.getInstance().getBadgeAuthorityCollectionName();
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
