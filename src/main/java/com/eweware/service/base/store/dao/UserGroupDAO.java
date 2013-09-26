package com.eweware.service.base.store.dao;

import java.util.Date;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/2/12 Time: 1:47 PM
 */
public interface UserGroupDAO extends BaseDAO, UserGroupDAOConstants {

    public String getState();

    public void setState(String state);

    public String getGroupId();

    public void setGroupId(String groupId);

    public String getGroupDisplayName();

    public void setGroupDisplayName(String name);

    public String getUserId();

    public void setUserId(String userId);

    public Integer getFirstInboxNumber();

    public void setFirstInboxNumber(Integer number);

    public Integer getLastInboxNumber();

    public void setLastInboxNumber(Integer number);

    public Date getCreated();

    public void setCreated(Date created);
}
