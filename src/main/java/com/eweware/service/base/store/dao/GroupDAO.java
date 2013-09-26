package com.eweware.service.base.store.dao;

import java.util.Date;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/2/12 Time: 1:14 PM
 */
public interface GroupDAO extends BaseDAO, GroupDAOConstants {

    public String getDisplayName();

    public void setDisplayName(String displayName);

    public String getDescription();

    public void setDescription(String description);

    public String getDescriptor();

    public void setDescriptor(String descriptor);

    public String getGroupTypeId();

    public void setGroupTypeId(String groupTypeId);

    public Integer getRank();

    public void setRank(Integer rank);

    public String getState();

    public void setState(String state);

    public Long getUserCount();

    public void setUserCount(Long userCount);

    public Long getBlahCount();

    public void setBlahCount(Long blahCount);

    public Long getCurrentViewerCount();

    public void setCurrentViewerCount(Long activeViewerCount);

    public String getValidationMethod();

    public void setValidationMethod(String method);

    public String getValidationParameters();

    public void setValidationParameters(String parameters);

    public Integer getFirstInboxNumber();

    public void setFirstInboxNumber(Integer number);

    public Integer getLastInboxNumber();

    public void setLastInboxNumber(Integer number);

    public Date getLastInboxGenerated();

    public void setLastInboxGenerated(Date date);

    public Long getLastInboxGeneratedDuration();

    public void setLastInboxGeneratedDuration(Long duration);

    public Date getCreated();

    public void setCreated(Date created);
}
