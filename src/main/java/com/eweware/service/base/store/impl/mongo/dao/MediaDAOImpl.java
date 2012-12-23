package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.store.dao.MediaDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.MediaSchema;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 12/22/12 Time: 7:42 PM
 */
public class MediaDAOImpl extends BaseDAOImpl implements MediaDAOConstants {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes>();

    static {  // TODO should be derived from schema
        MediaDAOImpl.FIELD_TO_TYPE_MAP.put(TYPE, main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes.STRING);
        MediaDAOImpl.FIELD_TO_TYPE_MAP.put(BUCKET, main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes.STRING);
        MediaDAOImpl.FIELD_TO_TYPE_MAP.put(PATHNAME, main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes.STRING);
        MediaDAOImpl.FIELD_TO_TYPE_MAP.put(CREATED, main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes.DATE);
        MediaDAOImpl.FIELD_TO_TYPE_MAP.put(UPDATED, main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes.DATE);
    }

    @Override
    protected Map<String, main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes> _getFieldNameToTypeMap() {
        return MediaDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws main.java.com.eweware.service.base.error.SystemErrorException {
        if (MediaDAOImpl.collectionName == null) {
            MediaDAOImpl.collectionName = main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager.getInstance().getMediaCollectionName();
        }
        return MediaDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws main.java.com.eweware.service.base.error.SystemErrorException {
        if (MediaDAOImpl.collection == null) {
            MediaDAOImpl.collection = main.java.com.eweware.service.base.store.impl.mongo.dao.MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return MediaDAOImpl.collection;
    }

    MediaDAOImpl() {
        super();
    }

    MediaDAOImpl(String id) throws main.java.com.eweware.service.base.error.SystemErrorException {
        super(id);
    }

    MediaDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws main.java.com.eweware.service.base.error.SystemErrorException {
        super(map, validateAndConvert);
    }

    public static main.java.com.eweware.service.base.store.dao.schema.BaseSchema getSchema(main.java.com.eweware.service.base.i18n.LocaleId localeId) throws main.java.com.eweware.service.base.error.SystemErrorException {
        return MediaSchema.getSchema(localeId);
    }
}
