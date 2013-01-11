package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.store.dao.MediaDAO;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.MediaSchema;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 12/22/12 Time: 7:42 PM
 */
public class MediaDAOImpl extends BaseDAOImpl implements MediaDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>();

    static {  // TODO should be derived from schema
        MediaDAOImpl.FIELD_TO_TYPE_MAP.put(TYPE, MongoFieldTypes.STRING);
        MediaDAOImpl.FIELD_TO_TYPE_MAP.put(BUCKET, MongoFieldTypes.STRING);
        MediaDAOImpl.FIELD_TO_TYPE_MAP.put(PATHNAME, MongoFieldTypes.STRING);
        MediaDAOImpl.FIELD_TO_TYPE_MAP.put(CREATED, MongoFieldTypes.DATE);
        MediaDAOImpl.FIELD_TO_TYPE_MAP.put(UPDATED, MongoFieldTypes.DATE);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return MediaDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (MediaDAOImpl.collectionName == null) {
            MediaDAOImpl.collectionName = MongoStoreManager.getInstance().getMediaCollectionName();
        }
        return MediaDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (MediaDAOImpl.collection == null) {
            MediaDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return MediaDAOImpl.collection;
    }

    MediaDAOImpl() {
        super();
    }

    MediaDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    MediaDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(main.java.com.eweware.service.base.i18n.LocaleId localeId) throws SystemErrorException {
        return MediaSchema.getSchema(localeId);
    }

    @Override
    public String getType() {
        return (String) get(TYPE);
    }

    @Override
    public void setType(String type) {
        put(TYPE, type);
    }

    @Override
    public String getBucketName() {
        return (String) get(BUCKET);
    }

    @Override
    public void setBucketName(String bucket) {
        put(BUCKET, bucket);
    }

    @Override
    public String getPathname() {
        return (String) get(PATHNAME);
    }

    @Override
    public void setPathname(String pathname) {
        put(PATHNAME, pathname);
    }

    @Override
    public Map<String, Object> toMap() {
        return super.toMap();
    }
}
