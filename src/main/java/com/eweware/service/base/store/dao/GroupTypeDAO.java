package main.java.com.eweware.service.base.store.dao;

import java.util.Date;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/2/12 Time: 1:43 PM
 */
public interface GroupTypeDAO extends BaseDAO, GroupTypeDAOConstants {

    public String getDisplayName();

    public void setDisplayName(String displayName);

    public Integer getGroupCount();

    public void setGroupCount(Integer groupCount);

    public Date getCreated();

    public void setCreated(Date created);
}
