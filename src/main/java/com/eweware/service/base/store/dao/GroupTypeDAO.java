package com.eweware.service.base.store.dao;

import com.eweware.service.base.store.dao.schema.type.BooleanDataTypeValidator;

import java.util.Date;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/2/12 Time: 1:43 PM
 */
public interface GroupTypeDAO extends BaseDAO, GroupTypeDAOConstants {

    public String getDisplayName();

    public void setDisplayName(String displayName);

    public Long getGroupCount();

    public void setGroupCount(Long groupCount);

    public Date getCreated();

    public void setCreated(Date created);

    public Boolean getIsAdmin();

    public void setIsAdmin(Boolean isAdmin);
}
