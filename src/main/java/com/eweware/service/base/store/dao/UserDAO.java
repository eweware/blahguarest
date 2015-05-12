package com.eweware.service.base.store.dao;

import java.util.Date;
import java.util.List;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/2/12 Time: 12:58 PM
 */
public interface UserDAO extends BaseDAO, UserDAOConstants {

    public String getUsername();

    public void setUsername(String username);

    public List<UserTrackerDAO> getStats();

    public void setStats(List<UserTrackerDAO> stats);

    public Double getStrength();

    public void setStrength(Double strength);

    public Double getControversyStrength();

    public void setControversyStrength(Double strength);

    public Date getCreated();

    public void setCreated(Date created);

    public List<String> getBadgeIds();

    public void setBadgeIds(List<String> badgeIds);

    public List<String> getImageids();

    public void setImageIds(List<String> imageIds);

    public Boolean getIsAdmin();

    public Boolean getWantsMature();

    public void setWantsMature(Boolean bool);

    public Boolean getIsSpammer();

    public void setIsSpammer(Boolean bool);


    /**
     * <p>Returns the last signin date for the user</p>
     *
     * @return The expiration date or null if none.
     */
    public Date getLastSignInDate();

    /**
     * <p>Sets the last signin date for the user.</p>
     *
     * @param date The expiration date
     */
    public void setLastSignInDate(Date date);
}
