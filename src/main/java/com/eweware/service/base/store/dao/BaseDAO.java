package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.error.SystemErrorException;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/2/12 Time: 1:05 PM
 */
public interface BaseDAO extends Serializable, BaseDAOConstants, DAOMethods, Map<String, Object> {

    public String getId();

    public void setId(String id) throws SystemErrorException;

    public Boolean getDeleted();

    public void setDeleted(Boolean deleted);

    public Date getCreated();

    public void setCreated(Date created);

    public Date getUpdated();

    public void setUpdated(Date updated);

    public Map<String, Object> toMap();
}
