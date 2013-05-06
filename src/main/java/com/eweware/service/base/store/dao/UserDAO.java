package main.java.com.eweware.service.base.store.dao;

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
}
