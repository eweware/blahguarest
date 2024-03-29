package com.eweware.service.base.payload;

import com.eweware.service.base.CommonUtilities;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.GroupTypeDAOConstants;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.dao.schema.GroupTypeSchema;

import java.util.Map;

/**
 * @author rk@post.harvard.edu
 */
public final class GroupTypePayload extends BasePayload implements GroupTypeDAOConstants {

    protected static final BaseSchema getSchema() {
        return GroupTypeSchema.getSchema(LocaleId.en_us);
    }

    public GroupTypePayload() {
        super();
    }

    public GroupTypePayload(String id) {
        super(id);
    }

    public GroupTypePayload(Map<String, Object> map) {
        super(map);
    }

    public String getDisplayName() {
        return (String) get(DISPLAY_NAME);
    }

    public void setDisplayName(String displayName) {
        put(DISPLAY_NAME, displayName);
    }

    public Long getGroupCount() {
        return CommonUtilities.getValueAsLong(get(GROUP_COUNT), null);
    }

    public void setGroupCount(Long groupCount) {
        put(GROUP_COUNT, groupCount);
    }

    public Boolean getIsAdmin() { return (Boolean) get (GROUP_ADMIN);}

    public void setIsAdmin(Boolean isAdmin)  { put(GROUP_ADMIN, isAdmin); }
}
