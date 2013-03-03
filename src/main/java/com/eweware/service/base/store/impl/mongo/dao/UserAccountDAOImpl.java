package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.store.dao.UserAccountDAO;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 2/26/13 Time: 4:31 PM
 */
public class UserAccountDAOImpl extends BaseDAOImpl implements UserAccountDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>();

    static {  // TODO should be derived from schema
        UserAccountDAOImpl.FIELD_TO_TYPE_MAP.put(CANONICAL_USERNAME, MongoFieldTypes.STRING);
        UserAccountDAOImpl.FIELD_TO_TYPE_MAP.put(PASSWORD_DIGEST, MongoFieldTypes.STRING);
        UserAccountDAOImpl.FIELD_TO_TYPE_MAP.put(PASSWORD_SALT, MongoFieldTypes.STRING);
        addInheritedFieldToTypeMapItems(UserAccountDAOImpl.FIELD_TO_TYPE_MAP);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return UserAccountDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (UserAccountDAOImpl.collectionName == null) {
            UserAccountDAOImpl.collectionName = MongoStoreManager.getInstance().getUserAccountsCollectionName();
        }
        return UserAccountDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (UserAccountDAOImpl.collection == null) {
            UserAccountDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return UserAccountDAOImpl.collection;
    }

    public UserAccountDAOImpl() {
    }

    public UserAccountDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    public UserAccountDAOImpl(Map<String, Object> map, boolean convert) throws SystemErrorException {
        super(map, convert);
    }


    @Override
    public String getCanonicalUsername() {
        return (String) get(CANONICAL_USERNAME);
    }

    @Override
    public void setCanonicalUsername(String canonicalUsername) {
        put(CANONICAL_USERNAME, canonicalUsername);
    }

    @Override
    public String getDigest() {
        return (String) get(PASSWORD_DIGEST);
    }

    @Override
    public void setDigest(String digest) {
        put(PASSWORD_DIGEST, digest);
    }

    @Override
    public String getSalt() {
        return (String) get(PASSWORD_SALT);
    }

    @Override
    public void setSalt(String salt) {
        put(PASSWORD_SALT, salt);
    }

    @Override
    public Map<String, Object> toMap() {
        return super.toMap();
    }
}
