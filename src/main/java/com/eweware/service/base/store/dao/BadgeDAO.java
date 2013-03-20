package main.java.com.eweware.service.base.store.dao;

/**
 * <p>Represents a badge.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 3/18/13 Time: 7:41 PM
 */
public interface BadgeDAO extends BaseDAO, BadgeDAOConstants {

    public String getBadgeId();

    public void setBadgeId(String badgeId);

    public String getAuthorityId();

    public void setAuthorityId(String authorityId);

    public String getDisplayName();

    public void setDisplayName(String displayName);

    public String getDescription();

    public void setDescription(String description);

    public String getState();

    public void setState(String state);

    public String getReason();

    public void setReason(String reason);

    public String getIconUrl();

    public void setIconUrl(String iconUrl);

    public String getUserToken();

    public void setUserToken(String userToken);
}
