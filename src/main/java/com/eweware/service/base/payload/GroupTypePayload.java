package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.GroupTypeDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.BlahSchema;
import main.java.com.eweware.service.base.store.dao.schema.GroupTypeSchema;

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

    public Integer getGroupCount() {
        return (Integer) get(GROUP_COUNT);
    }

    public void setGroupCount(Integer groupCount) {
        put(GROUP_COUNT, groupCount);
    }
}
