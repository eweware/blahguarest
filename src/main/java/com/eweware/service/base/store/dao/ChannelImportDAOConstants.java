package com.eweware.service.base.store.dao;

import com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;

/**
 * Created by ultradad on 3/6/15.
 */
public interface ChannelImportDAOConstants {

    // for all feeds
    static final String FEED_NAME = "feedname";
    static final String TARGET_GROUP = "channel";
    static final String FEED_TYPE = "feedtype";
    static final String AUTO_IMPORT = "autoimport";
    static final String IMPORT_FREQUENCY = "importfrequency";
    static final String LAST_IMPORT_DATE = "lastimport";
    static final String IMPORT_USERNAME = "importusername";
    static final String IMPORT_PASSWORD = "importpassword";
    static final String IMPORT_AS_USER = "importasuser";

    // for RSS feeds
    static final String RSS_URL = "RSSurl";
    static final String RSS_USE_FEED_IMAGE = "usefeedimage";
    static final String SUMMARIZE_URL_PAGE = "summarizepage";
    static final String TITLE_FIELD = "titlefield";
    static final String IMAGE_FIELD = "imagefield";
    static final String BODY_FIELD = "bodyfield";
    static final String URL_FIELD = "urlfield";
    static final String APPEND_URL = "appendurl";

    // for Twitter feeds

    // for API feeds


    // schema
    static final SchemaDataTypeFieldMap[] SIMPLE_FIELD_TYPES = new SchemaDataTypeFieldMap[]{
            new SchemaDataTypeFieldMap(SchemaDataType.B, new String[]{AUTO_IMPORT, IMPORT_AS_USER, APPEND_URL, SUMMARIZE_URL_PAGE, RSS_USE_FEED_IMAGE}),
            new SchemaDataTypeFieldMap(SchemaDataType.S, new String[]{FEED_NAME, TARGET_GROUP, IMPORT_USERNAME, IMPORT_PASSWORD, RSS_URL, TITLE_FIELD, BODY_FIELD, IMAGE_FIELD, URL_FIELD}),
            new SchemaDataTypeFieldMap(SchemaDataType.DT, new String[]{LAST_IMPORT_DATE}),
            new SchemaDataTypeFieldMap(SchemaDataType.I, new String[]{IMPORT_FREQUENCY, FEED_TYPE})
    };


    public static enum ImportFeedType {
        RSS_FEED(0),
        TWITTER_FEED(1),
        API_FEED(2);

        private final Integer code;

        ImportFeedType(Integer theCode) { this.code = theCode; }

        public Integer getValue() {
            return code;
        }
    }

}
