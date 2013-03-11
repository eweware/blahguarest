package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.GroupDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.BlahSchema;
import main.java.com.eweware.service.base.store.dao.schema.GroupSchema;

import java.util.Map;

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

    public Integer getUserCount() {
        return (Integer) get(USER_COUNT);
    }

    public void setUserCount(Integer userCount) {
        put(USER_COUNT, userCount);
    }

    public Integer getCurrentViewerCount() {
        return (Integer) get(CURRENT_VIEWER_COUNT);
    }

    public void setCurrentViewerCount(Integer count) {
        put(CURRENT_VIEWER_COUNT, count);
    }
}
