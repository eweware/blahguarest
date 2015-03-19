package com.eweware.service.base.store.dao;

import com.eweware.service.base.store.dao.schema.type.SchemaDataType;
import com.eweware.service.base.store.dao.schema.type.SchemaDataTypeFieldMap;
import org.apache.xpath.operations.Bool;

import java.util.Date;

/**
 * Created by ultradad on 3/6/15.
 */
public interface ChannelImportDAO  extends BaseDAO, ChannelImportDAOConstants {

    public String getTargetGroup();
    public void setTargetGroup(String groupId);

    public String getFeedName();
    public void setFeedName(String theName);

    public Integer getFeedType();
    public void setFeedType(Integer theType);

    public Boolean getAutoImport();
    public void setAutoImport(Boolean bAutoImport);

    public Integer getImportFrequency();
    public void setImportFrequency(Integer importFrequency);

    public Date getLastImportDate();
    public void setLastImportDate(Date importDate);

    public String getImportUsername();
    public void setImportUsername(String username);

    public String getImportPassword();
    public void setImportPassword(String importPassword);

    public Boolean getImportAsUser();
    public void setImportAsUser(Boolean importAsUser);

    // rss
    public String getRSSURL();
    public void setRSSURL(String rssurl);

    public Boolean getSummarizeURLPage();
    public void setSummarizeURLPage(Boolean summarizeURLPage);

    public Boolean getUseFeedImage();
    public void setUseFeedImage(Boolean useFeedImage);

    public String getTitleField();
    public void setTitleField(String titleField);

    public String getBodyField();
    public void setBodyField(String bodyField);

    public String getImageField();
    public void setImageField(String imageField);

    public String getURLField();
    public void setURLField(String urlField);

    public Boolean getAppendURL();
    public void setAppendURL(Boolean appendURL);




}
