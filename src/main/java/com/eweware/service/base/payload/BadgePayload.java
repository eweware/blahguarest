package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.BadgeDAO;
import main.java.com.eweware.service.base.store.dao.BadgeDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.BadgeSchema;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;

import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 3/19/13 Time: 3:56 PM
 */
public class BadgePayload extends BasePayload implements BadgeDAOConstants {

    protected static final BaseSchema getSchema() {
        return BadgeSchema.getSchema(LocaleId.en_us);
    }


    public BadgePayload() {
        super();
    }

    public BadgePayload(String id) {
        super(id);
    }

    public BadgePayload(Map<String, Object> map) {
        super(map);
    }

   
    public String getBadgeId() {
        return (String) get(BADGE_ID);
    }

   
    public void setBadgeId(String badgeId) {
        put(BADGE_ID, badgeId);
    }

   
    public String getAuthorityId() {
        return (String) get(AUTHORITY_ID);
    }

   
    public void setAuthorityId(String authorityId) {
        put(AUTHORITY_ID, authorityId);
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

   
    public String getState() {
        return (String) get(STATE);
    }

   
    public void setState(String state) {
        put(STATE, state);
    }

   
    public String getReason() {
        return (String) get(REASON);
    }

   
    public void setReason(String reason) {
        put(REASON, reason);
    }

   
    public String getIconUrl() {
        return (String) get(ICON_URL);
    }

   
    public void setIconUrl(String iconUrl) {
        put(ICON_URL, iconUrl);
    }

   
    public String getUserToken() {
        return null;
    }

   
    public void setUserToken(String userToken) {

    }
}
