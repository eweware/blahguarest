package main.java.com.eweware.service.base.store.dao;

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

    public String getGroupTypeId();

    public void setGroupTypeId(String groupTypeId);

    public String getState();

    public void setState(String state);

    public Integer getUserCount();

    public void setUserCount(Integer userCount);

    public Integer getBlahCount();

    public void setBlahCount(Integer blahCount);

    public String getValidationMethod();

    public void setValidationMethod(String method);

    public String getValidationParameters();

    public void setValidationParameters(String parameters);

    public Date getCreated();

    public void setCreated(Date created);
}
