package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.UserDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.BlahSchema;
import main.java.com.eweware.service.base.store.dao.schema.UserSchema;

import java.util.List;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 */
public final class UserPayload extends BasePayload implements UserDAOConstants {

    protected static final BaseSchema getSchema() {
        return UserSchema.getSchema(LocaleId.en_us);
    }

    public UserPayload() {
        super();
    }

    public UserPayload(String id) {
        super(id);
    }

    public UserPayload(Map<String, Object> map) {
        super(map);
    }

    public String getUsername() {
        return (String) get(USERNAME);
    }

    public void setUsername(String username) {
        put(USERNAME, username);
    }

    public Double getStrength() {
        return (Double) get(USER_STRENGTH);
    }

    public void setStrength(Double strength) {
        put(USER_STRENGTH, strength);
    }

    public Double getControversyStrength() {
        return (Double) get(USER_CONTROVERSY_STRENGTH);
    }

    public void setControversyStrength(Double strength) {
        put(USER_CONTROVERSY_STRENGTH, strength);
    }

    public List<UserTrackerPayload> getStats() {
        return (List<UserTrackerPayload>) get(STATS);
    }

    public void setStats(List<UserTrackerPayload> stats) {
        put(STATS, stats);
    }
}
