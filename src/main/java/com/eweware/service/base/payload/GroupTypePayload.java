package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.store.dao.GroupTypeDAOConstants;

import java.util.Map;

/**
 * @author rk@post.harvard.edu
 */
public final class GroupTypePayload extends BasePayload implements GroupTypeDAOConstants {

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
