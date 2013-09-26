package com.eweware.service.base.payload;


import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.UserGroupDAOConstants;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.dao.schema.UserGroupSchema;

import java.util.Map;

/**
 * @author rk@post.harvard.edu
 */
public final class UserGroupPayload extends BasePayload implements UserGroupDAOConstants {

    protected static final BaseSchema getSchema() {
        return UserGroupSchema.getSchema(LocaleId.en_us);
    }

    // Email address is not stored in DB (used for validation)
    final String VALIDATION_EMAIL_ADDRESS = "emailAddress";

    public UserGroupPayload() {
        super();
    }

    public UserGroupPayload(String id) {
        super(id);
    }

    public UserGroupPayload(Map<String, Object> map) {
        super(map);
    }

    public UserGroupPayload(String userId, String groupId) {
        super();
        setUserId(userId);
        setGroupId(groupId);
    }

    public String getUserId() {
        return (String) get(USER_ID);
    }

    public void setUserId(String userId) {
        put(USER_ID, userId);
    }

    public String getGroupId() {
        return (String) get(GROUP_ID);
    }

    public void setGroupId(String groupId) {
        put(GROUP_ID, groupId);
    }

    public String getState() {
        return (String) get(STATE);
    }

    public void setState(String state) {
        put(STATE, state);
    }

    public String getValidationEmailAddress() {
        return (String) get(VALIDATION_EMAIL_ADDRESS);
    }

    public void setValidationEmailAddress(String emailAddress) {
        put(VALIDATION_EMAIL_ADDRESS, emailAddress);
    }
}
