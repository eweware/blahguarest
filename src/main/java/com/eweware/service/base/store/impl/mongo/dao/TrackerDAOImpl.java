package com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import com.eweware.service.base.error.ErrorCodes;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.TrackerDAO;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.dao.schema.TrackerSchema;
import com.eweware.service.base.store.dao.tracker.TrackerOperation;
import com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Currently not in use. Should replace the large by-month tracker when time allows.</p>
 * <p>Reason to replace it is that: (1) it ties us to MongoDB bigtime, and (2) even
 * for Mongo, we don't want to detal with large objects that can potentially
 * consume RAM workspace: ideally, we would have smaller consecutively
 * placed chunks in disc... since we might not be able to control or predict how Mongo lays it out,
 * the benefits of having smaller chunks in mongo are unclear without testing.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 9/25/12 Time: 1:07 PM
 */
@SuppressWarnings("deprecation")
public class TrackerDAOImpl extends BaseDAOImpl implements TrackerDAO {

    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>();

    static {  // TODO should be derived from schema
        TrackerDAOImpl.FIELD_TO_TYPE_MAP.put(OPERATION, MongoFieldTypes.STRING);
        TrackerDAOImpl.FIELD_TO_TYPE_MAP.put(USER_GENDER, MongoFieldTypes.STRING);
        TrackerDAOImpl.FIELD_TO_TYPE_MAP.put(USER_RACE, MongoFieldTypes.STRING);
        TrackerDAOImpl.FIELD_TO_TYPE_MAP.put(USER_INCOME_RANGE, MongoFieldTypes.STRING);
        TrackerDAOImpl.FIELD_TO_TYPE_MAP.put(USER_DATE_OF_BIRTH, MongoFieldTypes.DATE);
        TrackerDAOImpl.FIELD_TO_TYPE_MAP.put(PROMOTION, MongoFieldTypes.NUMBER);
        TrackerDAOImpl.FIELD_TO_TYPE_MAP.put(VIEWS, MongoFieldTypes.NUMBER);
        TrackerDAOImpl.FIELD_TO_TYPE_MAP.put(OPENS, MongoFieldTypes.NUMBER);
        TrackerDAOImpl.FIELD_TO_TYPE_MAP.put(STATE, MongoFieldTypes.STRING);
        TrackerDAOImpl.FIELD_TO_TYPE_MAP.put(GROUP_TYPE_ID, MongoFieldTypes.STRING);
        TrackerDAOImpl.FIELD_TO_TYPE_MAP.put(GROUP_ID, MongoFieldTypes.STRING);
        TrackerDAOImpl.FIELD_TO_TYPE_MAP.put(USER_ID, MongoFieldTypes.STRING);
        TrackerDAOImpl.FIELD_TO_TYPE_MAP.put(BLAH_ID, MongoFieldTypes.STRING);
        TrackerDAOImpl.FIELD_TO_TYPE_MAP.put(COMMENT_ID, MongoFieldTypes.STRING);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return TrackerDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (TrackerDAOImpl.collectionName == null) {
            TrackerDAOImpl.collectionName = MongoStoreManager.getInstance().getTrackerCollectionName();
        }
        return TrackerDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (TrackerDAOImpl.collection == null) {
            TrackerDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
        }
        return TrackerDAOImpl.collection;
    }
    TrackerDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public TrackerDAOImpl(TrackerOperation operation) throws SystemErrorException {
        super();
        if (operation == null) {
            throw new SystemErrorException("Missing tracker operation", ErrorCodes.SERVER_SEVERE_ERROR);
        }
        setOperation(operation.getCode());
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return TrackerSchema.getSchema(localeId);
    }

    @Override
    public String getOperation() {
        return (String) get(OPERATION);
    }

    @Override
    public void setOperation(String operation) {
        put(OPERATION, operation);
    }

    @Override
    public Long getVote() {
        return (Long) get(PROMOTION);
    }

    @Override
    public void setVote(Long vote) {
        put(PROMOTION, vote);
    }

    @Override
    public Long getViews() {
        return (Long) get(VIEWS);
    }

    @Override
    public void setViews(Long views) {
        put(VIEWS, views);
    }

    @Override
    public Long getOpens() {
        return (Long) get(OPENS);
    }

    @Override
    public void setOpens(Long opens) {
        put(OPENS, opens);
    }

    @Override
    public String getState() {
        return (String) get(STATE);
    }

    @Override
    public void setState(String state) {
        put(STATE, state);
    }

    @Override
    public String getGroupTypeId() {
        return (String) get(GROUP_TYPE_ID);
    }

    @Override
    public void setGroupTypeId(String groupTypeId) {
        put(GROUP_TYPE_ID, groupTypeId);
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
    public String getUserId() {
        return (String) get(USER_ID);
    }

    @Override
    public void setUserId(String userId) {
        put(USER_ID, userId);
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
    public String getCommentId() {
        return (String) get(COMMENT_ID);
    }

    @Override
    public void setCommentId(String commentId) {
        put(COMMENT_ID, commentId);
    }

    @Override
    public String getUserGender() {
        return (String) get(USER_GENDER);
    }

    @Override
    public void setUserGender(String gender) {
        put(USER_GENDER, gender);
    }

    @Override
    public String getUserRace() {
        return (String) get(USER_RACE);
    }

    @Override
    public void setUserRace(String race) {
        put(USER_RACE, race);
    }

    @Override
    public String getUserIncomeRange() {
        return (String) get(USER_INCOME_RANGE);
    }

    @Override
    public void setUserIncomeRange(String incomeRange) {
        put(USER_INCOME_RANGE, incomeRange);
    }

    @Override
    public Date getUserDateOfBirth() {
        return (Date) get(USER_DATE_OF_BIRTH);
    }

    @Override
    public void setUserDateOfBirth(Date dob) {
        put(USER_DATE_OF_BIRTH, dob);
    }
}
