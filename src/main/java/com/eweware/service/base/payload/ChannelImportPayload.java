package com.eweware.service.base.payload;

import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.ChannelImportDAOConstants;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.dao.schema.ChannelImportSchema;

import java.util.Date;


/**
 * Created by ultradad on 3/6/15.
 */
public class ChannelImportPayload extends BasePayload implements ChannelImportDAOConstants {

    protected static final BaseSchema getSchema() {
        return ChannelImportSchema.getSchema(LocaleId.en_us);
    }

    public String getTargetGroup() { return (String) get(TARGET_GROUP);  }
    public void setTargetGroup(String theMessage) {
        put(TARGET_GROUP, theMessage);
    }

    public Integer getFeedType() { return (Integer) get(FEED_TYPE); }
    public void setFeedType(Integer theType) { put(FEED_TYPE, theType) ; }

    public Boolean getAutoImport() { return (Boolean) get(AUTO_IMPORT); }
    public void setAutoImport(Boolean bAutoImport){ put(AUTO_IMPORT, bAutoImport) ; }

    public Integer getImportFrequency() { return (Integer) get(IMPORT_FREQUENCY); }
    public void setImportFrequency(Integer importFrequency){ put(IMPORT_FREQUENCY, importFrequency) ; }

    public Date getLastImportDate() { return (Date) get(LAST_IMPORT_DATE); }
    public void setLastImportDate(Date importDate){ put(LAST_IMPORT_DATE, importDate) ; }

    public String getImportUsername() { return (String) get(IMPORT_USERNAME); }
    public void setImportUsername(String username){ put(IMPORT_USERNAME, username) ; }

    public String getImportPassword() { return (String) get(IMPORT_PASSWORD); }
    public void setImportPassword(String importPassword){ put(IMPORT_PASSWORD, importPassword) ; }

    public Boolean getImportAsUser() { return (Boolean) get(IMPORT_AS_USER); }
    public void setImportAsUser(Boolean importAsUser){ put(IMPORT_AS_USER, importAsUser) ; }

    // rss
    public String getRSSURL() { return (String) get(RSS_URL); }
    public void setRSSURL(String rssurl){ put(RSS_URL, rssurl) ; }

    public Boolean getSummarizeURLPage() { return (Boolean) get(SUMMARIZE_URL_PAGE); }
    public void setSummarizeURLPage(Boolean summarizeURLPage){ put(SUMMARIZE_URL_PAGE, summarizeURLPage) ; }

    public String getTitleField() { return (String) get(TITLE_FIELD); }
    public void setTitleField(String titleField){ put(TITLE_FIELD, titleField) ; }

    public String getBodyField() { return (String) get(BODY_FIELD); }
    public void setBodyField(String bodyField){ put(BODY_FIELD, bodyField) ; }

    public String getImageField() { return (String) get(IMAGE_FIELD); }
    public void setImageField(String imageField){ put(IMAGE_FIELD, imageField) ; }

    public String getURLField() { return (String) get(URL_FIELD); }
    public void setURLField(String urlField){ put(URL_FIELD, urlField) ; }

    public Boolean getAppendURL() { return (Boolean) get(APPEND_URL); }
    public void setAppendURL(Boolean appendURL){ put(APPEND_URL, appendURL) ; }
}
