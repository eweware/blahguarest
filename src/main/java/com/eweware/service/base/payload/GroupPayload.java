package main.java.com.eweware.service.base.payload;

import com.mongodb.DBCollection;
import main.java.com.eweware.service.base.CommonUtilities;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.GroupDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.GroupSchema;

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

    /**
     * <p>Returns the name of a random inbox collection. Looks
     * only for currently active collections.</p>
     * <p>For the edge case where there are no inboxes in the group,
     * it returns the first inbox.</p>
     * @return  The name of a currently active collection in the group.
     */
    public String randomInboxCollectionName() {
        Integer first = getFirstInboxNumber();
        if (first == null) first = 0;
        Integer last = getLastInboxNumber();
        if (last == null) last = 0;
        if (first == last) {
            // if first == 0, we don't update the group's first/last inbox #s: let the inboxer worry about it
            return CommonUtilities.makeInboxCollectionName(getId(), first);
        }
        final int inboxNumber = new Random().nextInt(last - first + 1) + first;
        return CommonUtilities.makeInboxCollectionName(getId(), inboxNumber);
    }
}
