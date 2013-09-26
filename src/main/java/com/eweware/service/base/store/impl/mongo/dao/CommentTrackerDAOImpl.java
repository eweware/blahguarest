package com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.CommentTrackerDAO;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/24/12 Time: 7:10 PM
 */
public class CommentTrackerDAOImpl extends BaseDAOImpl implements CommentTrackerDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>(0);

    static {   // TODO should be derived from schema
        FIELD_TO_TYPE_MAP.put(CT_OBJECT_ID, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(CT_AUTHOR_ID, MongoFieldTypes.STRING);
        FIELD_TO_TYPE_MAP.put(CT_UP_VOTES_FOR_COMMENT, MongoFieldTypes.NUMBER);
        FIELD_TO_TYPE_MAP.put(CT_DOWN_VOTES_FOR_COMMENT, MongoFieldTypes.NUMBER);
        FIELD_TO_TYPE_MAP.put(CT_UP_VOTE_FOR_BLAH, MongoFieldTypes.NUMBER);
        FIELD_TO_TYPE_MAP.put(CT_DOWN_VOTE_FOR_BLAH, MongoFieldTypes.NUMBER);
        FIELD_TO_TYPE_MAP.put(CT_VIEWS, MongoFieldTypes.NUMBER);
        FIELD_TO_TYPE_MAP.put(CT_OPENS, MongoFieldTypes.NUMBER);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (CommentTrackerDAOImpl.collectionName == null) {
            CommentTrackerDAOImpl.collectionName = MongoStoreManager.getInstance().getTrackCommentCollectionName();
        }
        return CommentTrackerDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (CommentTrackerDAOImpl.collection == null) {
            CommentTrackerDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return CommentTrackerDAOImpl.collection;
    }

    /**
     * This class uses a composite String id.
     *
     * @param id The id as a string
     * @return String A String version of the id, suitable for storage and use in Mongo
     * @throws com.eweware.service.base.error.SystemErrorException
     *          Not thrown by this superclass but must be declared
     *          because Java is just so
     */
    @Override
    protected Object makeMongoId(String id) throws SystemErrorException {
        return id;
    }

    CommentTrackerDAOImpl() {
        super();
    }

    CommentTrackerDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    CommentTrackerDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return null;
    }
}
