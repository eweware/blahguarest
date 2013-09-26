package com.eweware.service.base.payload;

import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.BadgeAuthorityDAOConstants;
import com.eweware.service.base.store.dao.schema.BadgeAuthoritySchema;
import com.eweware.service.base.store.dao.schema.BaseSchema;

import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 3/19/13 Time: 3:52 PM
 */
public class BadgeAuthorityPayload extends BasePayload implements BadgeAuthorityDAOConstants {

    protected static final BaseSchema getSchema() {
        return BadgeAuthoritySchema.getSchema(LocaleId.en_us);
    }

    public BadgeAuthorityPayload() {
        super();
    }

    public BadgeAuthorityPayload(String id) {
        super(id);
    }

    public BadgeAuthorityPayload(Map<String, Object> map) {
        super(map);
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


    public String getBadgeTypeDescription() {
        return (String) get(BADGE_TYPE_DESCRIPTION);
    }


    public void setBadgeTypeDescription(String description) {
        put(BADGE_TYPE_DESCRIPTION, description);
    }

   
    public String getEndpointUrl() {
        return (String) get(ENDPOINT_URL);
    }

   
    public void setEndpointUrl(String endpointUrl) {
        put(ENDPOINT_URL, endpointUrl);
    }

    public String getRestEndpointUrl() {
        return (String) get(REST_ENDPOINT_URL);
    }

    public void setRestEndpointUrl(String restEndpointUrl) {
        put(REST_ENDPOINT_URL, restEndpointUrl);
    }
}
