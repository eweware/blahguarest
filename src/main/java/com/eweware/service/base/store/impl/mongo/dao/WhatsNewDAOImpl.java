package com.eweware.service.base.store.impl.mongo.dao;

import com.eweware.service.base.error.ErrorCodes;
import com.eweware.service.base.store.dao.BaseDAO;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.WhatsNewDAO;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.dao.schema.WhatsNewSchema;
import com.eweware.service.base.store.impl.mongo.MongoFieldTypes;
import com.mongodb.DBObject;
import org.omg.CORBA.MARSHAL;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dave on 1/25/14.
 */
public class WhatsNewDAOImpl extends BaseDAOImpl implements WhatsNewDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>(6);

    static {   // TODO should be derived from schema
        WhatsNewDAOImpl.FIELD_TO_TYPE_MAP.put(MESSAGE, MongoFieldTypes.STRING);
        WhatsNewDAOImpl.FIELD_TO_TYPE_MAP.put(NEW_COMMENTS, MongoFieldTypes.STRING);
        WhatsNewDAOImpl.FIELD_TO_TYPE_MAP.put(NEW_OPENS, MongoFieldTypes.STRING);
        WhatsNewDAOImpl.FIELD_TO_TYPE_MAP.put(NEW_UP_VOTES, MongoFieldTypes.STRING);
        WhatsNewDAOImpl.FIELD_TO_TYPE_MAP.put(NEW_DOWN_VOTES, MongoFieldTypes.STRING);
        WhatsNewDAOImpl.FIELD_TO_TYPE_MAP.put(TARGET_USER, MongoFieldTypes.STRING);
        WhatsNewDAOImpl.FIELD_TO_TYPE_MAP.put(NEW_MESSAGES, MongoFieldTypes.NUMBER);
        addInheritedFieldToTypeMapItems(FIELD_TO_TYPE_MAP);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return GroupDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (WhatsNewDAOImpl.collectionName == null) {
            WhatsNewDAOImpl.collectionName = MongoStoreManager.getInstance().getWhatsNewCollectionName();
        }
        return WhatsNewDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (WhatsNewDAOImpl.collection == null) {
            try {
                WhatsNewDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
            } catch (SystemErrorException e) {
                throw new RuntimeException("Missing mongo col for group", e);
            }
        }
        return WhatsNewDAOImpl.collection;
    }

    WhatsNewDAOImpl() {
        super();
    }

    WhatsNewDAOImpl(String id) throws SystemErrorException {
        super();

        setTargetUser(id);
    }

    WhatsNewDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return WhatsNewDAOImpl.getSchema(localeId);
    }

    @Override
    public String getMessage() {
        return (String) get(MESSAGE);
    }

    @Override
    public void setMessage(String theMessage) {
        put(MESSAGE, theMessage);
    }

    @Override
    public String getTargetUser() {
        return (String) get(TARGET_USER);
    }

    @Override
    public void setTargetUser(String theMessage) {
        put(TARGET_USER, theMessage);
    }

    @Override
    public Integer getNewComments() {
        return (Integer) get(NEW_COMMENTS);
    }

    @Override
    public void setNewComments(Integer theMessage) {
        put(NEW_COMMENTS, theMessage);
    }

    @Override
    public Integer getNewOpens() {
        return (Integer) get(NEW_OPENS);
    }

    @Override
    public void setNewOpens(Integer theMessage) {
        put(NEW_OPENS, theMessage);
    }

    @Override
    public Integer getNewUpVotes() {
        return (Integer) get(NEW_UP_VOTES);
    }

    @Override
    public void setNewUpVotes(Integer theMessage) {
        put(NEW_UP_VOTES, theMessage);
    }

    @Override
    public Integer getNewDownVotes() {
        return (Integer) get(NEW_DOWN_VOTES);
    }

    @Override
    public void setNewDownVotes(Integer theMessage) {
        put(NEW_DOWN_VOTES, theMessage);
    }

    @Override
    public Integer getNewMessages() {
        return (Integer) get(NEW_MESSAGES);
    }

    @Override
    public void setNewMessages(Integer theMessage) {
        put(NEW_MESSAGES, theMessage);
    }

    public WhatsNewDAO _findNewestInfoByTargetID(String theId) throws SystemErrorException {
        try {
            final DBObject id = new BasicDBObject(WhatsNewDAO.TARGET_USER, theId);
            DBObject fields = null;
            DBObject orderBy = new BasicDBObject(BaseDAO.CREATED, -1);
            final DBCollection collection = _getCollection();
            DBObject dao = null;
            try {
                dao = findRecentRetry(id, fields, orderBy, collection);
            } catch (SystemErrorException e) {
                throw e;
            } catch (Exception e) {
                throw new SystemErrorException("Failed to find newest for object=" + this, e, ErrorCodes.SERVER_DB_ERROR);
            }
            return (dao == null) ? null : (WhatsNewDAO)findDAOConstructor().newInstance(dao, false);
        } catch (Exception e) {
            throw new SystemErrorException(makeErrorMessage("_findNewestInfoByID", "find", null, e, null), e, ErrorCodes.SERVER_SEVERE_ERROR);
        }
    }



}
