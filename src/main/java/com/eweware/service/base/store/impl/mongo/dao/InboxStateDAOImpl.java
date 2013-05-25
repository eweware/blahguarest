package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.store.dao.InboxStateDAO;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 10/6/12 Time: 11:27 AM
 */
public class InboxStateDAOImpl extends BaseDAOImpl implements InboxStateDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>(10);

    static {   // TODO should be derived from schema
        InboxStateDAOImpl.FIELD_TO_TYPE_MAP.put(INBOX_ITEM_IDS, MongoFieldTypes.ARRAY);
        InboxStateDAOImpl.FIELD_TO_TYPE_MAP.put(INBOX_NUMBER_TOP, MongoFieldTypes.NUMBER);
        addInheritedFieldToTypeMapItems(FIELD_TO_TYPE_MAP);
    }

    @Override
    public List<ObjectId> getInboxItemIds() {
        return (List<ObjectId>) get(INBOX_ITEM_IDS);
    }

    @Override
    public void setInboxItemIds(List<ObjectId> ids) {
        put(INBOX_ITEM_IDS, ids);
    }

    @Override
    public Long getHighestInboxNumber() {
        return (Long) get(INBOX_NUMBER_TOP);
    }

    @Override
    public void setHighestInboxNumber(Long highMark) {
        put(INBOX_NUMBER_TOP, highMark);
    }

    @Override
    public Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return InboxStateDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (InboxStateDAOImpl.collectionName == null) {
            InboxStateDAOImpl.collectionName = MongoStoreManager.getInstance().getInboxStateCollectionName();
        }
        return InboxStateDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (InboxStateDAOImpl.collection == null) {
            try {
                InboxStateDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
            } catch (SystemErrorException e) {
                e.printStackTrace();
            }
        }
        return InboxStateDAOImpl.collection;
    }
}
