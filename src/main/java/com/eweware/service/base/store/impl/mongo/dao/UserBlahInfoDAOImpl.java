package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.UserBlahInfoDAO;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.UserBlahInfoSchema;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/3/12 Time: 9:15 PM
 */
public class UserBlahInfoDAOImpl extends BaseDAOImpl implements UserBlahInfoDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>();

    static {  // TODO should be derived from schema
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(USER_ID, MongoFieldTypes.STRING);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(AUTHOR_ID, MongoFieldTypes.STRING);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(BLAH_ID, MongoFieldTypes.STRING);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(ORIGINAL_GROUP_ID, MongoFieldTypes.STRING);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(BLAH_TYPE_ID, MongoFieldTypes.STRING);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(PROMOTION, MongoFieldTypes.NUMBER);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(POLL_VOTE_INDEX, MongoFieldTypes.NUMBER);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(POLL_VOTE_TIMESTAMP, MongoFieldTypes.DATE);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(PREDICTION_VOTE, MongoFieldTypes.STRING);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(PREDICTION_RESULT_VOTE, MongoFieldTypes.STRING);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(VIEWS, MongoFieldTypes.NUMBER);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(OPENS, MongoFieldTypes.NUMBER);
        UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP.put(COMMENTS_ON_THIS_BLAH, MongoFieldTypes.NUMBER);
        addInheritedFieldToTypeMapItems(FIELD_TO_TYPE_MAP);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return UserBlahInfoDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (UserBlahInfoDAOImpl.collectionName == null) {
            UserBlahInfoDAOImpl.collectionName = MongoStoreManager.getInstance().getUserBlahInfoCollectionName();
        }
        return UserBlahInfoDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (UserBlahInfoDAOImpl.collection == null) {
            UserBlahInfoDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return UserBlahInfoDAOImpl.collection;
    }

    UserBlahInfoDAOImpl() {
        super();
    }

    UserBlahInfoDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    UserBlahInfoDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return UserBlahInfoSchema.getSchema(localeId);
    }

    @Override
    public String getUserId() {
        return (String) get(USER_ID);
    }

    @Override
    public void setUserId(String userId) {
        put(USER_ID, userId);
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
    public String getBlahId() {
        return (String) get(BLAH_ID);
    }

    @Override
    public void setBlahId(String blahId) {
        put(BLAH_ID, blahId);
    }

    @Override
    public String getGroupId() {
        return (String) get(ORIGINAL_GROUP_ID);
    }

    @Override
    public void setGroupId(String groupId) {
        put(ORIGINAL_GROUP_ID, groupId);
    }

    @Override
    public String getBlahTypeId() {
        return (String) get(BLAH_TYPE_ID);
    }

    @Override
    public void setBlahTypeId(String blahTypeId) {
        put(BLAH_TYPE_ID, blahTypeId);
    }

    @Override
    public Long getPromotedOrDemoted() {
        return (Long) get(PROMOTION);
    }

    @Override
    public void setPromotedOrDemoted(Long promotedOrDemoted) {
        put(PROMOTION, promotedOrDemoted);
    }

    @Override
    public Long getPollVoteIndex() {
        return (Long) get(POLL_VOTE_INDEX);
    }

    @Override
    public void setPollVoteIndex(Long pollIndex) {
        put(POLL_VOTE_INDEX, pollIndex);
    }

    @Override
    public Date getPollVoteTimestamp() {
        return (Date) get(POLL_VOTE_TIMESTAMP);
    }

    @Override
    public void setPollVoteTimestamp(Date timestamp) {
        put(POLL_VOTE_TIMESTAMP, timestamp);
    }

    @Override
    public String getPredictionVote() {
        return (String) get(PREDICTION_VOTE);
    }

    @Override
    public void setPredictionVote(String vote) {
        put(PREDICTION_VOTE, vote);
    }

    @Override
    public String getPredictionResultVote() {
        return (String) get(PREDICTION_RESULT_VOTE);
    }

    @Override
    public void setPredictionResultVote(String vote) {
        put(PREDICTION_RESULT_VOTE, vote);
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

    @Override
    public Long getComments() {
        return (Long) get(COMMENTS_ON_THIS_BLAH);
    }

    @Override
    public void setComments(Long comments) {
        put(COMMENTS_ON_THIS_BLAH, comments);
    }
}
