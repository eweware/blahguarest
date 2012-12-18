package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.BlahTrackerDAO;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/24/12 Time: 7:04 PM
 */
public class BlahTrackerDAOImpl extends BaseDAOImpl implements BlahTrackerDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>(0);

    static {   // TODO should be derived from schema
        FIELD_TO_TYPE_MAP.put(BT_OBJECT_ID, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(BT_AUTHOR_ID, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(BT_COMMENTS, MongoFieldTypes.NUMBER);
        FIELD_TO_TYPE_MAP.put(BT_UP_VOTES, MongoFieldTypes.NUMBER);
        FIELD_TO_TYPE_MAP.put(BT_DOWN_VOTES, MongoFieldTypes.NUMBER);
        FIELD_TO_TYPE_MAP.put(BT_VIEWS, MongoFieldTypes.NUMBER);
        FIELD_TO_TYPE_MAP.put(BT_OPENS, MongoFieldTypes.NUMBER);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (BlahTrackerDAOImpl.collectionName == null) {
            BlahTrackerDAOImpl.collectionName = MongoStoreManager.getInstance().getTrackBlahCollectionName();
        }
        return BlahTrackerDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (BlahTrackerDAOImpl.collection == null) {
            BlahTrackerDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return BlahTrackerDAOImpl.collection;
    }

    /**
     * This class uses a composite String id.
     *
     * @param id The id as a string
     * @return String A String version of the id, suitable for storage and use in Mongo
     * @throws main.java.com.eweware.service.base.error.SystemErrorException
     *          Not thrown by this superclass but must be declared
     *          because Java is just so
     */
    @Override
    protected Object makeMongoId(String id) throws SystemErrorException {
        return id;
    }

    BlahTrackerDAOImpl() {
        super();
    }

    BlahTrackerDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    BlahTrackerDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return null; // TODO
    }
}
