package com.eweware.service.base.store.impl.mongo.dao;

import com.mongodb.DBCollection;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.GroupDAO;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.dao.schema.GroupSchema;
import com.eweware.service.base.store.impl.mongo.MongoFieldTypes;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 */
public class GroupDAOImpl extends BaseDAOImpl implements GroupDAO {

    private static final long serialVersionUID = 1L;



    private static String collectionName;
    private static DBCollection collection;

    public static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>();

    static {   // TODO should be derived from schema
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(GROUP_TYPE_ID, MongoFieldTypes.STRING);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(RANK, MongoFieldTypes.NUMBER);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(DISPLAY_NAME, MongoFieldTypes.STRING);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(DESCRIPTION, MongoFieldTypes.STRING);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(DESCRIPTOR, MongoFieldTypes.STRING);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(STATE, MongoFieldTypes.STRING);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(USER_COUNT, MongoFieldTypes.NUMBER);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(BLAH_COUNT, MongoFieldTypes.NUMBER);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(CURRENT_VIEWER_COUNT, MongoFieldTypes.NUMBER);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(FIRST_INBOX_NUMBER, MongoFieldTypes.NUMBER);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(LAST_INBOX_NUMBER, MongoFieldTypes.NUMBER);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(FIRST_SAFE_INBOX_NUMBER, MongoFieldTypes.NUMBER);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(LAST_SAFE_INBOX_NUMBER, MongoFieldTypes.NUMBER);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(LAST_TIME_INBOXES_GENERATED, MongoFieldTypes.DATE);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(INBOX_GENERATION_DURATION, MongoFieldTypes.DATE);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(ADMIN, MongoFieldTypes.ARRAY);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(HEADER_IMAGE, MongoFieldTypes.STRING);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(AD_BLAH_ID, MongoFieldTypes.STRING);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(JOIN_BADGE_LIST, MongoFieldTypes.ARRAY);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(COMMENT_BADGE_LIST, MongoFieldTypes.ARRAY);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(POST_BADGE_LIST, MongoFieldTypes.ARRAY);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(CONTENT_EXPIRATION_DAYS, MongoFieldTypes.NUMBER);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(POSTS_MODERATION_BADGE_LIST, MongoFieldTypes.ARRAY);
        GroupDAOImpl.FIELD_TO_TYPE_MAP.put(MODERATED, MongoFieldTypes.BOOLEAN);
        addInheritedFieldToTypeMapItems(FIELD_TO_TYPE_MAP);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return GroupDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (GroupDAOImpl.collectionName == null) {
            GroupDAOImpl.collectionName = MongoStoreManager.getInstance().getGroupCollectionName();
        }
        return GroupDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (GroupDAOImpl.collection == null) {
            try {
                GroupDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
            } catch (SystemErrorException e) {
                throw new RuntimeException("Missing mongo col for group", e);
            }
        }
        return GroupDAOImpl.collection;
    }

    GroupDAOImpl() {
        super();
    }

    GroupDAOImpl(String id) throws SystemErrorException {
        super(id);
    }

    GroupDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return GroupSchema.getSchema(localeId);
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
    public Integer getRank() {
        return (Integer) get(RANK);
    }

    @Override
    public void setRank(Integer rank) {
        put(RANK, rank);
    }

    @Override
    public String getDisplayName() {
        return (String) get(DISPLAY_NAME);
    }

    @Override
    public void setDisplayName(String displayName) {
        put(DISPLAY_NAME, displayName);
    }

    @Override
    public String getDescription() {
        return (String) get(DESCRIPTION);
    }

    @Override
    public void setDescription(String description) {
        put(DESCRIPTION, description);
    }

    @Override
    public String getDescriptor() {
        return (String) get(DESCRIPTOR);
    }

    @Override
    public void setDescriptor(String descriptor) {
        put(DESCRIPTOR, descriptor);
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
    public Long getUserCount() {
        return (Long) get(USER_COUNT);
    }

    @Override
    public void setUserCount(Long userCount) {
        put(USER_COUNT, userCount);
    }

    @Override
    public Long getBlahCount() {
        return (Long) get(BLAH_COUNT);
    }

    @Override
    public void setBlahCount(Long blahCount) {
        put(BLAH_COUNT, blahCount);
    }

    @Override
    public Long getCurrentViewerCount() {
        return (Long) get(CURRENT_VIEWER_COUNT);
    }

    @Override
    public void setCurrentViewerCount(Long currentViewerCount) {
        put(CURRENT_VIEWER_COUNT, currentViewerCount);
    }


    @Override
    public Integer getFirstInboxNumber() {
        return (Integer) get(FIRST_INBOX_NUMBER);
    }

    @Override
    public void setFirstInboxNumber(Integer number) {
        put(FIRST_INBOX_NUMBER, number);
    }

    @Override
    public Integer getLastInboxNumber() {
        return (Integer) get(LAST_INBOX_NUMBER);
    }

    @Override
    public void setLastInboxNumber(Integer number) {
        put(LAST_INBOX_NUMBER, number);
    }

    @Override
    public Integer getFirstSafeInboxNumber() {
        return (Integer) get(FIRST_SAFE_INBOX_NUMBER);
    }

    @Override
    public void setFirstSafeInboxNumber(Integer number) {
        put(FIRST_SAFE_INBOX_NUMBER, number);
    }

    @Override
    public Integer getLastSafeInboxNumber() {
        return (Integer) get(LAST_SAFE_INBOX_NUMBER);
    }

    @Override
    public void setLastSafeInboxNumber(Integer number) {
        put(LAST_SAFE_INBOX_NUMBER, number);
    }

    @Override
    public Date getLastInboxGenerated() {
        return (Date) get(LAST_TIME_INBOXES_GENERATED);
    }



    @Override
    public void setLastInboxGenerated(Date date) {
        put(LAST_TIME_INBOXES_GENERATED, date);
    }

    @Override
    public Long getLastInboxGeneratedDuration() {
        return (Long) get(INBOX_GENERATION_DURATION);
    }

    @Override
    public void setLastInboxGeneratedDuration(Long duration) {
        put(INBOX_GENERATION_DURATION, duration);
    }

    @Override
    public List<String> getAdmin() { return (List<String>) get(ADMIN); }

    @Override
    public void setAdmin(List<String> adminIDs) { put(ADMIN, adminIDs); }

    @Override
    public String getHeaderImage() { return (String) get(HEADER_IMAGE); }

    @Override
    public void setHeaderImage(String headerImage) { put(HEADER_IMAGE, headerImage); }

    @Override
    public String getAdBlahID() { return (String) get(AD_BLAH_ID); }

    @Override
    public void setAdBlahID(String adBlahId) { put (AD_BLAH_ID, adBlahId); }

    @Override
    public List<String> getJoinBadgeList() { return (List<String>) get(JOIN_BADGE_LIST); }

    @Override
    public void setJoinBadgeList(List<String> badgeList) { put(JOIN_BADGE_LIST, badgeList); }

    @Override
    public List<String> getCommentBadgeList() { return (List<String>) get(COMMENT_BADGE_LIST); }

    @Override
    public void setCommentBadgeList(List<String> badgeList) { put(COMMENT_BADGE_LIST, badgeList); }

    @Override
    public List<String> getPostBadgeList() { return (List<String>) get(POST_BADGE_LIST); }

    @Override
    public void setPostBadgeList(List<String> badgeList) { put(POST_BADGE_LIST, badgeList); }

    @Override
    public List<String> getModerateBadgeList() { return (List<String>) get(POSTS_MODERATION_BADGE_LIST); }

    @Override
    public void setModerateBadgeList(List<String> badgeList) { put(POSTS_MODERATION_BADGE_LIST, badgeList); }

    @Override
    public int getContextExpirationDays() { return (Integer) get(CONTENT_EXPIRATION_DAYS); }

    @Override
    public void setContentExpirationDays(int numDays) { put(JOIN_BADGE_LIST, numDays); }

    @Override
    public Boolean getModerated() { return (Boolean) get(MODERATED); }

    @Override
    public void setModerated(Boolean isModerated) { put(MODERATED, isModerated); }

    @Override
    public Integer getCommentModerationStyle() { return (Integer) get(COMMENT_MODERATION_STYLE); }

    @Override
    public void setCommentModerationStyle(Integer modStyle) { put(COMMENT_MODERATION_STYLE, modStyle); }


    // permission helpers


}
