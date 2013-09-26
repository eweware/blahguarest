package com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/5/12 Time: 10:26 AM
 *
 *         TODO create structure for clients and trackingmanager
 */
public interface UserTrackerDAO extends BaseDAO, UserTrackerDAOConstants {

    public String userId();
    public void setUserId(String userId);

}
