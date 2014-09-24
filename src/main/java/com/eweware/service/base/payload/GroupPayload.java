package com.eweware.service.base.payload;

import com.mongodb.DBCollection;
import com.eweware.service.base.CommonUtilities;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.GroupDAOConstants;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.dao.schema.GroupSchema;

import java.util.Date;
import java.util.Map;
import java.util.Random;

/**
 * @author rk@post.harvard.edu
 */
public final class GroupPayload extends BasePayload implements GroupDAOConstants {

    protected static final BaseSchema getSchema() {
        return GroupSchema.getSchema(LocaleId.en_us);
    }

    public GroupPayload() {
        super();
    }

    public GroupPayload(String id) {
        super(id);
    }

    public GroupPayload(Map<String, Object> map) {
        super(map);
    }

    public String getGroupTypeId() {
        return (String) get(GROUP_TYPE_ID);
    }

    public void setGroupTypeId(String groupTypeId) {
        put(GROUP_TYPE_ID, groupTypeId);
    }

    public String getDisplayName() {
        return (String) get(DISPLAY_NAME);
    }

    public void setDisplayName(String displayName) {
        put(DISPLAY_NAME, displayName);
    }

    public String getDescription() {
        return (String) get(DESCRIPTION);
    }

    public void setDescription(String description) {
        put(DESCRIPTION, description);
    }

    public String getDescriptor() {
        return (String) get(DESCRIPTOR);
    }

    public void setDescriptor(String descriptor) {
        put(DESCRIPTOR, descriptor);
    }

    public Integer getRank() {
        return (Integer) get(RANK);
    }

    public void setRank(Integer rank) {
        put(RANK, rank);
    }

    public String getState() {
        return (String) get(STATE);
    }

    public void setState(String state) {
        put(STATE, state);
    }

    public String getValidationMethod() {
        return (String) get(USER_VALIDATION_METHOD);
    }

    public void setValidationMethod(String method) {
        put(USER_VALIDATION_METHOD, method);
    }

    public String getValidationParameters() {
        return (String) get(USER_VALIDATION_PARAMETERS);
    }

    public void setValidationParameters(String params) {
        put(USER_VALIDATION_PARAMETERS, params);
    }

    public Long getUserCount() {
        return (Long) get(USER_COUNT);
    }

    public void setUserCount(Long userCount) {
        put(USER_COUNT, userCount);
    }

    public Long getCurrentViewerCount() {
        return (Long) get(CURRENT_VIEWER_COUNT);
    }

    public void setCurrentViewerCount(Long count) {
        put(CURRENT_VIEWER_COUNT, count);
    }

    public Integer getFirstSafeInboxNumber() {
        return (Integer) get(FIRST_SAFE_INBOX_NUMBER);
    }

    public void setFirstSafeInboxNumber(Integer number) {
        put(FIRST_SAFE_INBOX_NUMBER, number);
    }

    public Integer getLastSafeInboxNumber() {
        return (Integer) get(LAST_SAFE_INBOX_NUMBER);
    }

    public void setLastSafeInboxNumber(Integer number) {
        put(LAST_SAFE_INBOX_NUMBER, number);
    }

    public Integer getFirstInboxNumber() {
        return (Integer) get(FIRST_INBOX_NUMBER);
    }

    public void setFirstInboxNumber(Integer number) {
        put(FIRST_INBOX_NUMBER, number);
    }

    public Integer getLastInboxNumber() {
        return (Integer) get(LAST_INBOX_NUMBER);
    }

    public void setLastInboxNumber(Integer number) {
        put(LAST_INBOX_NUMBER, number);
    }

    public Date getLastInboxGenerated() {
        return (Date) get(LAST_TIME_INBOXES_GENERATED);
    }

    public void setLastInboxGenerated(Date date) {
        put(LAST_TIME_INBOXES_GENERATED, date);
    }

    public Long getLastInboxGeneratedDuration() {
        return (Long) get(INBOX_GENERATION_DURATION);
    }

    public void setLastInboxGeneratedDuration(Long duration) {
        put(INBOX_GENERATION_DURATION, duration);
    }


}
