package main.java.com.eweware.service.base.store.dao;

import java.util.Date;

/**
 * <p>Represents a badge.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 3/18/13 Time: 7:41 PM
 */
public interface BadgeDAO extends BaseDAO, BadgeDAOConstants {

    public String getAuthorityBadgeId();

    public void setAuthorityBadgeId(String badgeId);

    public String getAuthorityId();

    public void setAuthorityId(String authorityId);

    public String getAuthorityDisplayName();

    public void setAuthorityDisplayName(String displayName);

    public String getDisplayName();

    public void setDisplayName(String displayName);

    public String getBadgeType();

    public void setBadgeType(String badgeType);

    public String getIconUrl();

    public void setIconUrl(String iconUrl);

    public String getUserId();

    public void setUserId(String userId);

    public Date getExpirationDate();

    public void setExpirationDate(Date expirationDate);
}
