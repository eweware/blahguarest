package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.BadgeDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.BadgeSchema;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;

import java.util.Date;
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

    public String getAuthorityBadgeId() {
        return (String) get(AUTHORITY_BADGE_ID);
    }

    public void setAuthorityBadgeId(String badgeId) {
        put(AUTHORITY_BADGE_ID, badgeId);
    }

    public String getAuthorityId() {
        return (String) get(AUTHORITY_ID);
    }

    public void setAuthorityId(String authorityId) {
        put(AUTHORITY_ID, authorityId);
    }

    public String getAuthorityDisplayName() {
        return (String) get(AUTHORITY_DISPLAY_NAME);
    }

    public void setAuthorityDisplayName(String displayName) {
        put(AUTHORITY_DISPLAY_NAME, displayName);
    }

    public String getDisplayName() {
        return (String) get(DISPLAY_NAME);
    }


    public void setDisplayName(String displayName) {
        put(DISPLAY_NAME, displayName);
    }

    public String getBadgeType() {
        return (String) get(BADGE_TYPE);
    }

    public void setBadgeType(String badgeType) {
        put(BADGE_TYPE, badgeType);
    }

    public String getIconUrl() {
        return (String) get(ICON_URL);
    }

    public void setIconUrl(String iconUrl) {
        put(ICON_URL, iconUrl);
    }

    public String getUserId() {
        return (String) get(USER_ID);
    }

    public void setUserId(String userId) {
        put(USER_ID, userId);
    }

    public Date getExpirationDate() {
        return (Date) get(EXPIRATION_DATE);
    }

    public void setExpirationDate(Date expirationDate) {
        put(EXPIRATION_DATE, expirationDate);
    }
}
