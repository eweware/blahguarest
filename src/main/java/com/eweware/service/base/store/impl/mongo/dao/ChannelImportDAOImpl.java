package com.eweware.service.base.store.impl.mongo.dao;

import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.ChannelImportDAO;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.dao.schema.ChannelImportSchema;
import com.eweware.service.base.store.impl.mongo.MongoFieldTypes;
import com.mongodb.DBCollection;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by ultradad on 3/6/15.
 */
public class ChannelImportDAOImpl extends BaseDAOImpl implements ChannelImportDAO {
    private static String collectionName;
    private static DBCollection collection;

    public static final Map<String, MongoFieldTypes> FIELD_TO_TYPE_MAP = new HashMap<String, MongoFieldTypes>();

    static {   // TODO should be derived from schema
        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(FEED_NAME, MongoFieldTypes.STRING);
        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(TARGET_GROUP, MongoFieldTypes.STRING);
        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(FEED_TYPE, MongoFieldTypes.NUMBER);
        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(AUTO_IMPORT, MongoFieldTypes.BOOLEAN);
        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(IMPORT_FREQUENCY, MongoFieldTypes.NUMBER);
        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(LAST_IMPORT_DATE, MongoFieldTypes.DATE);
        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(IMPORT_USERNAME, MongoFieldTypes.STRING);
        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(IMPORT_PASSWORD, MongoFieldTypes.STRING);
        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(IMPORT_AS_USER, MongoFieldTypes.BOOLEAN);

        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(RSS_URL, MongoFieldTypes.STRING);
        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(SUMMARIZE_URL_PAGE, MongoFieldTypes.BOOLEAN);
        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(RSS_USE_FEED_IMAGE, MongoFieldTypes.BOOLEAN);
        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(TITLE_FIELD, MongoFieldTypes.STRING);
        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(IMAGE_FIELD, MongoFieldTypes.STRING);
        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(BODY_FIELD, MongoFieldTypes.STRING);
        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(URL_FIELD, MongoFieldTypes.STRING);
        ChannelImportDAOImpl.FIELD_TO_TYPE_MAP.put(APPEND_URL, MongoFieldTypes.BOOLEAN);

        addInheritedFieldToTypeMapItems(FIELD_TO_TYPE_MAP);
    }

    @Override
    protected Map<String, MongoFieldTypes> _getFieldNameToTypeMap() {
        return ChannelImportDAOImpl.FIELD_TO_TYPE_MAP;
    }

    @Override
    protected String _getCollectionName() throws SystemErrorException {
        if (ChannelImportDAOImpl.collectionName == null) {
            ChannelImportDAOImpl.collectionName = MongoStoreManager.getInstance().getChannelImportCollectionName();
        }
        return ChannelImportDAOImpl.collectionName;
    }

    @Override
    protected DBCollection _getCollection() throws SystemErrorException {
        if (ChannelImportDAOImpl.collection == null) {
            try {
                ChannelImportDAOImpl.collection = MongoStoreManager.getInstance().getCollection(_getCollectionName());
            } catch (SystemErrorException e) {
                throw new RuntimeException("Missing mongo col for group", e);
            }
        }
        return ChannelImportDAOImpl.collection;
    }

    ChannelImportDAOImpl() {
        super();
    }

    ChannelImportDAOImpl(String groupId) throws SystemErrorException {
        super();

        setTargetGroup(groupId);
    }

    ChannelImportDAOImpl(Map<String, Object> map, boolean validateAndConvert) throws SystemErrorException {
        super(map, validateAndConvert);
    }

    public static BaseSchema getSchema(LocaleId localeId) throws SystemErrorException {
        return ChannelImportSchema.getSchema(localeId);
    }

    @Override
    public String getFeedName() {
        return (String) get(FEED_NAME);
    }

    @Override
    public void setFeedName(String theName) {
        put(FEED_NAME, theName);
    }



    @Override
    public String getTargetGroup() {
        return (String) get(TARGET_GROUP);
    }

    @Override
    public void setTargetGroup(String theMessage) {
        put(TARGET_GROUP, theMessage);
    }


    @Override
    public Integer getFeedType() { return (Integer) get(FEED_TYPE); }
    @Override
    public void setFeedType(Integer theType) { put(FEED_TYPE, theType) ; }

    @Override
    public Boolean getAutoImport() { return (Boolean) get(AUTO_IMPORT); }
    @Override
    public void setAutoImport(Boolean bAutoImport){ put(AUTO_IMPORT, bAutoImport) ; }

    @Override
    public Integer getImportFrequency() { return (Integer) get(IMPORT_FREQUENCY); }
    @Override
    public void setImportFrequency(Integer importFrequency){ put(IMPORT_FREQUENCY, importFrequency) ; }

    @Override
    public Date getLastImportDate() {
        Date theDate;
        final Object theVal = get(LAST_IMPORT_DATE);

        try {
            if (!(theVal instanceof Date )) {
                String dateString = theVal.toString();
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
                theDate = dateFormat.parse(dateString);
            } else {
                theDate = (Date)theVal;
            }
        }
        catch (Exception exp) {
            theDate = null;
        }


        return theDate;
    }

    @Override
    public void setLastImportDate(Date importDate){
        String dateStr;
        try {
            dateStr = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH).format(importDate);
        } catch (Exception exp) {
            dateStr = null;
        }
        put(LAST_IMPORT_DATE, dateStr) ;
    }

    @Override
    public String getImportUsername() { return (String) get(IMPORT_USERNAME); }
    @Override
    public void setImportUsername(String username){ put(IMPORT_USERNAME, username) ; }

    @Override
    public String getImportPassword() { return (String) get(IMPORT_PASSWORD); }
    @Override
    public void setImportPassword(String importPassword){ put(IMPORT_PASSWORD, importPassword) ; }

    @Override
    public Boolean getImportAsUser() { return (Boolean) get(IMPORT_AS_USER); }
    @Override
    public void setImportAsUser(Boolean importAsUser){ put(IMPORT_AS_USER, importAsUser) ; }

    // rss
    @Override
    public String getRSSURL() { return (String) get(RSS_URL); }
    @Override
    public void setRSSURL(String rssurl){ put(RSS_URL, rssurl) ; }

    @Override
    public Boolean getSummarizeURLPage() { return (Boolean) get(SUMMARIZE_URL_PAGE); }
    @Override
    public void setSummarizeURLPage(Boolean summarizeURLPage){ put(SUMMARIZE_URL_PAGE, summarizeURLPage) ; }

    @Override
    public Boolean getUseFeedImage() { return (Boolean) get(RSS_USE_FEED_IMAGE); }
    @Override
    public void setUseFeedImage(Boolean useFeedImage){ put(RSS_USE_FEED_IMAGE, useFeedImage) ; }


    @Override
    public String getTitleField() { return (String) get(TITLE_FIELD); }
    @Override
    public void setTitleField(String titleField){ put(TITLE_FIELD, titleField) ; }

    @Override
    public String getBodyField() { return (String) get(BODY_FIELD); }
    @Override
    public void setBodyField(String bodyField){ put(BODY_FIELD, bodyField) ; }

    @Override
    public String getImageField() { return (String) get(IMAGE_FIELD); }
    @Override
    public void setImageField(String imageField){ put(IMAGE_FIELD, imageField) ; }

    @Override
    public String getURLField() { return (String) get(URL_FIELD); }
    @Override
    public void setURLField(String urlField){ put(URL_FIELD, urlField) ; }

    @Override
    public Boolean getAppendURL() { return (Boolean) get(APPEND_URL); }
    @Override
    public void setAppendURL(Boolean appendURL){ put(APPEND_URL, appendURL) ; }


}
