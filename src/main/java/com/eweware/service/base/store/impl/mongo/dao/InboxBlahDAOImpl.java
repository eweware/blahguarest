package com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.InboxBlahDAO;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.dao.schema.InboxBlahSchema;
import com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/9/12 Time: 4:11 PM
 */
public class InboxBlahDAOImpl extends BaseDAOImpl implements InboxBlahDAO {


    private static String collectionName;
    private static DBCollection collection;

    private static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>(10);

    static {   // TODO should be derived from schema
        InboxBlahDAOImpl.FIELD_TO_TYPE_MAP.put(BLAH_ID, MongoFieldTypes.STRING);
        InboxBlahDAOImpl.FIELD_TO_TYPE_MAP.put(AUTHOR_ID, MongoFieldTypes.STRING);
        InboxBlahDAOImpl.FIELD_TO_TYPE_MAP.put(BLAH_TEXT, MongoFieldTypes.STRING);
        InboxBlahDAOImpl.FIELD_TO_TYPE_MAP.put(TYPE, MongoFieldTypes.STRING);
        InboxBlahDAOImpl.FIELD_TO_TYPE_MAP.put(GROUP_ID, MongoFieldTypes.STRING);
        InboxBlahDAOImpl.FIELD_TO_TYPE_MAP.put(INBOX_NUMBER, MongoFieldTypes.NUMBER);
        InboxBlahDAOImpl.FIELD_TO_TYPE_MAP.put(BLAH_STRENGTH, MongoFieldTypes.NUMBER);
        InboxBlahDAOImpl.FIELD_TO_TYPE_MAP.put(RECENT_BLAH_STRENGTH, MongoFieldTypes.NUMBER);
        InboxBlahDAOImpl.FIELD_TO_TYPE_MAP.put(UP_VOTES, MongoFieldTypes.NUMBER);
        InboxBlahDAOImpl.FIELD_TO_TYPE_MAP.put(DOWN_VOTES, MongoFieldTypes.NUMBER);
        InboxBlahDAOImpl.FIELD_TO_TYPE_MAP.put(VIEWS, MongoFieldTypes.NUMBER);
        InboxBlahDAOImpl.FIELD_TO_TYPE_MAP.put(OPENS, MongoFieldTypes.NUMBER);
        InboxBlahDAOImpl.FIELD_TO_TYPE_MAP.put(IMAGE_IDS, MongoFieldTypes.ARRAY);
        InboxBlahDAOImpl.FIELD_TO_TYPE_MAP.put(BADGE_INDICATOR, MongoFieldTypes.STRING);
        InboxBlahDAOImpl.FIELD_TO_TYPE_MAP.put(AUTHOR_NICKNAME, MongoFieldTypes.STRING);
        InboxBlahDAOImpl.FIELD_TO_TYPE_MAP.put(RECENTLY_ACTIVE, MongoFieldTypes.BOOLEAN);
        addInheritedFieldToTypeMapItems(FIELD_TO_TYPE_MAP);
    }

    @Override
    public Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return InboxBlahDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (InboxBlahDAOImpl.collectionName == null) {
            InboxBlahDAOImpl.collectionName = MongoStoreManager.getInstance().getBlahInboxCollectionName();
        }
        return InboxBlahDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (InboxBlahDAOImpl.collection == null) {
            try {
                InboxBlahDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
            } catch (SystemErrorException e) {
                e.printStackTrace(); // TODO use logger
            }
        }
        return InboxBlahDAOImpl.collection;
    }


    InboxBlahDAOImpl() {
        super();
    }

    InboxBlahDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    InboxBlahDAOImpl(Map<String, Object> obj, boolean validateAndConvert) throws SystemErrorException {
        super(obj, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return InboxBlahSchema.getSchema(localeId);
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
    public String getAuthorId() {
        return (String) get(AUTHOR_ID);
    }

    @Override
    public void setAuthorId(String authorId) {
        put(AUTHOR_ID, authorId);
    }

    @Override
    public String getType() {
        return (String) get(TYPE);
    }

    @Override
    public void setType(String type) {
        put(TYPE, type);
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
    public String getBlahText() {
        return (String) get(BLAH_TEXT);
    }


    @Override
    public void setBlahText(String text) {
        put(BLAH_TEXT, text);
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
    public Integer getInboxNumber() {
        return (Integer) get(INBOX_NUMBER);
    }

    @Override
    public void setInboxNumber(Integer sequenceNumber) {
        put(INBOX_NUMBER, sequenceNumber);
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
    public Long getUpVotes() {
        return (Long) get(UP_VOTES);
    }

    @Override
    public void setUpVotes(Long upVotes) {
        put(UP_VOTES, upVotes);
    }

    @Override
    public Long getDownVotes() {
        return (Long) get(DOWN_VOTES);
    }

    @Override
    public void setDownVotes(Long downVotes) {
        put(DOWN_VOTES, downVotes);
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
    public Long getViews() {
        return (Long) get(VIEWS);
    }

    @Override
    public void setViews(Long views) {
        put(VIEWS, views);
    }

    @Override
    public String getBadgeIndicator() {
        return (String) get(BADGE_INDICATOR);
    }

    @Override
    public void setBadgeIndicator(String indicator) {
        put(BADGE_INDICATOR, indicator);
    }

    @Override
    public String getAuthorNickname() {
        return (String) get(AUTHOR_NICKNAME);
    }

    @Override
    public void setAuthorNickname(String nickname) {
        put(AUTHOR_NICKNAME, nickname);
    }

    @Override
    public Boolean getBlahIsRecent() {
        return (Boolean) get(RECENTLY_ACTIVE);
    }

    @Override
    public void setBlahIsRecent(Boolean isRecent) {
        put(RECENTLY_ACTIVE, isRecent);
    }

}
