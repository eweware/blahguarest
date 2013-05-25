package main.java.com.eweware.service.base.store.dao;

import org.bson.types.ObjectId;

import java.util.List;

/**
 * @author rk@post.harvard.edu
 *         Date: 10/6/12 Time: 11:15 AM
 */
public interface InboxStateDAO extends BaseDAO, InboxStateDAOConstants {

    public List<ObjectId> getInboxItemIds();

    public void setInboxItemIds(List<ObjectId> ids);

    public Long getHighestInboxNumber();

    public void setHighestInboxNumber(Long highMark);
}
