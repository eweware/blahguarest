package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.UserDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.UserSchema;

import java.util.List;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 */
public final class UserPayload extends BasePayload implements UserDAOConstants {

    /**
     * <p>A password. This is a transient property which is never stored as-is.
     * Actual password and all other sensitive user information is stored in
     * a separate user account record.
     * It's sole use is to transmit the password from the client to the server. A string.</p>
     * @see main.java.com.eweware.service.base.store.dao.UserAccountDAOConstants
     */
    public static final String PASSWORD = "pwd";

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

    public List<String> getBadgeIds() {
        return (List<String>) get(BADGE_IDS);
    }

    public void setBadgeIds(List<String> badgeIds) {
        put(BADGE_IDS, badgeIds);
    }

    public List<String> getImageIds() {
        return (List<String>) get(IMAGE_IDS);
    }

    public void setImageIds(List<String> imageIds) {
        put(IMAGE_IDS, imageIds);
    }

    public List<UserTrackerPayload> getStats() {
        return (List<UserTrackerPayload>) get(STATS);
    }

    public void setStats(List<UserTrackerPayload> stats) {
        put(STATS, stats);
    }
}
