package main.java.com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.BlahDAO;
import main.java.com.eweware.service.base.store.dao.BlahTrackerDAO;
import main.java.com.eweware.service.base.store.dao.PollOptionTextDAO;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.BlahSchema;
import main.java.com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author rk@post.harvard.edu
 */

public class BlahDAOImpl extends BaseDAOImpl implements BlahDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>();

    static {  // TODO should be derived from schema
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(TYPE_ID, MongoFieldTypes.STRING);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(TEXT, MongoFieldTypes.STRING);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(BODY, MongoFieldTypes.STRING);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(GROUP_ID, MongoFieldTypes.STRING);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(AUTHOR_ID, MongoFieldTypes.STRING);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(PROMOTED_COUNT, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(DEMOTED_COUNT, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(EXPIRATION_DATE, MongoFieldTypes.DATE);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(PREDICTION_USER_AGREE_COUNT, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(PREDICTION_USER_DISAGREE_COUNT, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(PREDICTION_USER_UNCLEAR_COUNT, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(PREDICTION_RESULT_CORRECT_COUNT, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(PREDICTION_RESULT_INCORRECT_COUNT, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(PREDICTION_RESULT_UNCLEAR_COUNT, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(VIEWS, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(OPENS, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(COMMENTS, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(BLAH_STRENGTH, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(RECENT_BLAH_STRENGTH, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(POLL_OPTION_COUNT, MongoFieldTypes.NUMBER);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(POLL_OPTIONS_TEXT, MongoFieldTypes.ARRAY);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(POLL_OPTION_VOTES, MongoFieldTypes.ARRAY);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(STATS, MongoFieldTypes.ARRAY);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(IMAGE_IDS, MongoFieldTypes.ARRAY);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(BADGE_IDS, MongoFieldTypes.ARRAY);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(CREATED, MongoFieldTypes.DATE);
        BlahDAOImpl.FIELD_TO_TYPE_MAP.put(UPDATED, MongoFieldTypes.DATE);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return BlahDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (BlahDAOImpl.collectionName == null) {
            BlahDAOImpl.collectionName = MongoStoreManager.getInstance().getBlahCollectionName();
        }
        return BlahDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (BlahDAOImpl.collection == null) {
            BlahDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return BlahDAOImpl.collection;
    }

    BlahDAOImpl() {
        super();
    }

    BlahDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    BlahDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return BlahSchema.getSchema(localeId);
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
    public String getText() {
        return (String) get(TEXT);
    }

    @Override
    public void setText(String text) {
        put(TEXT, text);
    }

    @Override
    public String getBody() {
        return (String) get(BODY);
    }

    @Override
    public void setBody(String body) {
        put(BODY, body);
    }

    @Override
    public List<String> getImageIds() {
        return (List<String>) get(IMAGE_IDS);
    }

    @Override
    public void setImageIds(List<String> imageIds) {
        put(IMAGE_IDS, imageIds);
    }

    @Override
    public List<String> getBadgeIds() {
        return (List<String>) get(BADGE_IDS);
    }

    @Override
    public void setBadgeIds(List<String> badgeIds) {
        put(BADGE_IDS, badgeIds);
    }

    @Override
    public Integer getPollOptionCount() {
        return (Integer) get(POLL_OPTION_COUNT);
    }

    @Override
    public void setPollOptionCount(Integer pollOptionCount) {
        put(POLL_OPTION_COUNT, pollOptionCount);
    }

    @Override
    public List<PollOptionTextDAO> getPollOptionsText() {
        return (List<PollOptionTextDAO>) get(POLL_OPTIONS_TEXT);
    }

    @Override
    public void setPollOptionsText(List<PollOptionTextDAO> pollOptionText) {
        put(POLL_OPTIONS_TEXT, pollOptionText);
    }

    @Override
    public List<Integer> getPollOptionVotes() {
        return (List<Integer>) get(POLL_OPTION_VOTES);
    }

    @Override
    public void setPollOptionVotes(List<Integer> pollOptionVotes) {
        put(POLL_OPTION_VOTES, pollOptionVotes);
    }



    /**
     * Adds one vote to this blah's poll for the specified
     * option index.
     * @param pollOptionIndex The option index
     */
    public void addPollOptionVote_immediate(Integer pollOptionIndex) throws SystemErrorException {
        final StringBuilder b = new StringBuilder(POLL_OPTION_VOTES);
        b.append('.');
        b.append(pollOptionIndex);
        String index = b.toString();
        final Map<String, Object> update = new HashMap<String, Object>();
        update.put(index, 1);
        final BasicDBObject criteria = new BasicDBObject(BlahDAO.ID, get(ID));
        final WriteResult result = _getCollection().update(criteria, new BasicDBObject("$inc", update));
        if (result.getError() != null) {
            throw new SystemErrorException("Failed to vote on poll with pollIndex '" + pollOptionIndex + "'", result.getError(), ErrorCodes.SERVER_DB_ERROR);
        }
    }

    @Override
    public String getGroupId() {
        return (String) get(GROUP_ID);
    }

    @Override
    public void setGroupId(String groupId) {
        put(GROUP_ID, groupId);
    }

    @Override
    public String getTypeId() {
        return (String) get(TYPE_ID);
    }

    @Override
    public void setTypeId(String type) {
        put(TYPE_ID, type);
    }

    @Override
    public Integer getViews() {
        return (Integer) get(VIEWS);
    }

    @Override
    public void setViews(Integer views) {
        put(VIEWS, views);
    }

    @Override
    public Integer getOpens() {
        return (Integer) get(OPENS);
    }

    @Override
    public void setOpens(Integer opens) {
        put(OPENS, opens);
    }

    @Override
    public Integer getComments() {
        return (Integer) get(COMMENTS);
    }

    @Override
    public void setComments(Integer comments) {
        put(COMMENTS, comments);
    }

    @Override
    public List<BlahTrackerDAO> getStats() {
        return (List<BlahTrackerDAO>) get(STATS);
    }

    @Override
    public void setStats(List<BlahTrackerDAO> stats) {
        put(STATS, stats);
    }

    @Override
    public Double getStrength() {
        return (Double) get(BLAH_STRENGTH);
    }

    @Override
    public void setStrength(Double strength) {
        put(BLAH_STRENGTH, strength);
    }

    @Override
    public Double getRecentStrength() {
        return (Double) get(RECENT_BLAH_STRENGTH);
    }

    @Override
    public void setRecentStrength(Double strength) {
        put(RECENT_BLAH_STRENGTH, strength);
    }

    @Override
    public Integer getPromotedCount() {
        return (Integer) get(PROMOTED_COUNT);
    }

    @Override
    public void setPromotedCount(Integer promotions) {
        put(PROMOTED_COUNT, promotions);
    }

    @Override
    public Integer getDemotedCount() {
        return (Integer) get(DEMOTED_COUNT);
    }

    @Override
    public void setDemotedCount(Integer demotions) {
        put(DEMOTED_COUNT, demotions);
    }

    @Override
    public Date getExpirationDate() {
        return (Date) get(EXPIRATION_DATE);
    }

    @Override
    public void setExpirationDate(Date date) {
        put(EXPIRATION_DATE, date);
    }

    @Override
    public Integer getPredictionAgreeCount() {
        return (Integer) get(PREDICTION_USER_AGREE_COUNT);
    }

    @Override
    public void setPredictionAgreeCount(Integer count) {
        put(PREDICTION_USER_AGREE_COUNT, count);
    }

    @Override
    public Integer getPredictionDisagreeCount() {
        return (Integer) get(PREDICTION_USER_DISAGREE_COUNT);
    }

    @Override
    public void setPredictionDisagreeCount(Integer count) {
        put(PREDICTION_USER_DISAGREE_COUNT, count);
    }

    @Override
    public Integer getPredictionUnclearCount() {
        return (Integer) get(PREDICTION_USER_UNCLEAR_COUNT);
    }

    @Override
    public void setPredictionUnclearCount(Integer count) {
        put(PREDICTION_USER_UNCLEAR_COUNT, count);
    }

    @Override
    public Integer getPredictionResultCorrectCount() {
        return (Integer) get(PREDICTION_RESULT_CORRECT_COUNT);
    }

    @Override
    public void setPredictionResultCorrectCount(Integer count) {
        put(PREDICTION_RESULT_CORRECT_COUNT, count);
    }

    @Override
    public Integer getPredictionResultIncorrectCount() {
        return (Integer) get(PREDICTION_RESULT_INCORRECT_COUNT);
    }

    @Override
    public void setPredictionResultIncorrectCount(Integer count) {
        put(PREDICTION_RESULT_INCORRECT_COUNT, count);
    }

    @Override
    public Integer getPredictionResultUnclearCount() {
        return (Integer) get(PREDICTION_RESULT_UNCLEAR_COUNT);
    }

    @Override
    public void setPredictionResultUnclearCount(Integer count) {
        put(PREDICTION_RESULT_UNCLEAR_COUNT, count);
    }
}
