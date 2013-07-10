package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.UserCommentInfoDAO;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.UserCommentInfoSchema;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/3/12 Time: 10:38 PM
 */
public class UserCommentInfoDAOImpl extends BaseDAOImpl implements UserCommentInfoDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>();

    static {   // TODO should be derived from schema
        UserCommentInfoDAOImpl.FIELD_TO_TYPE_MAP.put(USER_ID, MongoFieldTypes.STRING);
        UserCommentInfoDAOImpl.FIELD_TO_TYPE_MAP.put(AUTHOR_ID, MongoFieldTypes.STRING);
        UserCommentInfoDAOImpl.FIELD_TO_TYPE_MAP.put(COMMENT_ID, MongoFieldTypes.STRING);
        UserCommentInfoDAOImpl.FIELD_TO_TYPE_MAP.put(VOTE, MongoFieldTypes.NUMBER);
        UserCommentInfoDAOImpl.FIELD_TO_TYPE_MAP.put(VIEWS, MongoFieldTypes.NUMBER);
        UserCommentInfoDAOImpl.FIELD_TO_TYPE_MAP.put(OPENS, MongoFieldTypes.NUMBER);
        addInheritedFieldToTypeMapItems(FIELD_TO_TYPE_MAP);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return UserCommentInfoDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (UserCommentInfoDAOImpl.collectionName == null) {
            UserCommentInfoDAOImpl.collectionName = MongoStoreManager.getInstance().getUserCommentInfoCollectionName();
        }
        return UserCommentInfoDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (UserCommentInfoDAOImpl.collection == null) {
            UserCommentInfoDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return UserCommentInfoDAOImpl.collection;
    }

    UserCommentInfoDAOImpl() {
        super();
    }

    UserCommentInfoDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    UserCommentInfoDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return UserCommentInfoSchema.getSchema(localeId);
    }

    @Override
    public String getUserId() {
        return (String) get(USER_ID);
    }

    @Override
    public void setUserId(String id) {
        put(USER_ID, id);
    }

    @Override
    public String getAuthorId() {
        return (String) get(AUTHOR_ID);
    }

    @Override
    public void setAuthorId(String authorId) {
        put(AUTHOR_ID, authorId);
    }

    @Override
    public String getCommentId() {
        return (String) get(COMMENT_ID);
    }

    @Override
    public void setCommentId(String id) {
        put(COMMENT_ID, id);
    }

    @Override
    public Long getVote() {
        return (Long) get(VOTE);
    }

    @Override
    public void setVote(Long count) {
        put(VOTE, count);
    }

    @Override
    public Long getViews() {
        return (Long) get(VIEWS);
    }

    @Override
    public void setViews(Long count) {
        put(VIEWS, count);
    }

    @Override
    public Long getOpens() {
        return (Long) get(OPENS);
    }

    @Override
    public void setOpens(Long count) {
        put(OPENS, count);
    }
}
