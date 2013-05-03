package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.UserTrackerDAO;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/5/12 Time: 10:27 AM
 *         <p/>
 *         TODO this is just a place-holder for a large document that doesn't lend itself very well to a DAO design
 */
public class UserTrackerDAOImpl extends BaseDAOImpl implements UserTrackerDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>(0);

    static {  // TODO should be derived from schema
        // TODO this list is probably not up to date... updates are done manually by the TrackingManager: ok as long as we don't use ._X methods on these objects
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_NUMBER_OF_DAYS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_BLAHS_CREATED_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_BLAHS_CREATED_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_VOTES_FOR_OWNED_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_VOTES_FOR_OWNED_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_UP_VOTES_FOR_OWNED_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_UP_VOTES_FOR_OWNED_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_DOWN_VOTES_FOR_OWNED_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_DOWN_VOTES_FOR_OWNED_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_COMMENTS_ON_OWNED_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_COMMENTS_ON_OWNED_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_VIEWS_OF_OWNED_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_VIEWS_OF_OWNED_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_OPENS_OF_OWNED_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_OPENS_OF_OWNED_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_BLAH_STRENGTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_BLAH_STRENGTH_MAXIMUM, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_COMMENTS_CREATED_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_COMMENTS_CREATED_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_VOTES_FOR_OWNED_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_UP_VOTES_FOR_OWNED_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_UP_VOTES_FOR_OWNED_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_DOWN_VOTES_FOR_OWNED_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_DOWN_VOTES_FOR_OWNED_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_VIEWS_OF_OWNED_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_VIEWS_OF_OWNED_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_OPENS_OF_OWNED_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_OPENS_OF_OWNED_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_COMMENT_STRENGTH_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_COMMENT_STRENGTH_MAXIMUM_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_UP_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_UP_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_DOWN_VOTES_FOR_OTHERS_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_VIEWS_OF_OTHERS_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_VIEWS_OF_OTHERS_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_OPENS_OF_OTHERS_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_OPENS_OF_OTHERS_BLAHS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_UP_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_DOWN_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_DOWN_VOTES_FOR_OTHERS_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_VIEWS_OF_OTHERS_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_VIEWS_OF_OTHERS_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_OPENS_OF_OTHERS_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_AVE_OPENS_OF_OTHERS_COMMENTS_IN_MONTH, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_DAILY_STATS_ARRAY, MongoFieldTypes.NUMBER);
        UserTrackerDAOImpl.FIELD_TO_TYPE_MAP.put(UT_DAILY_STATS_ARRAY, MongoFieldTypes.NUMBER);

        addInheritedFieldToTypeMapItems(FIELD_TO_TYPE_MAP);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (UserTrackerDAOImpl.collectionName == null) {
            UserTrackerDAOImpl.collectionName = MongoStoreManager.getInstance().getTrackUserCollectionName();
        }
        return UserTrackerDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (UserTrackerDAOImpl.collection == null) {
            try {
                UserTrackerDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
            } catch (SystemErrorException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        return UserTrackerDAOImpl.collection;
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

    UserTrackerDAOImpl() {
        super();
    }

    UserTrackerDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    UserTrackerDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return null;    // TODO implement
    }


    @Override
    public String userId() {
        return (String) get(UT_USER_ID);
    }

    @Override
    public void setUserId(String userId) {
        put(UT_USER_ID, userId);
    }
}
