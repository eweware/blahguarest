package com.eweware.service.base.store.dao;

/**
 * <p>DAO represents a badging authority.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 3/18/13 Time: 4:05 PM
 */
public interface BadgeAuthorityDAO extends BaseDAO, BadgeAuthorityDAOConstants {

    public String getDisplayName();

    public void setDisplayName(String displayName);

    public String getDescription();

    public void setDescription(String description);

    public String getBadgeTypeDescription();

    public void setBadgeTypeDescription(String description);

    public String getEndpointUrl();

    public void setEndpointUrl(String endpointUrl);

    public String getRestEndpointUrl();

    public void setRestEndpointUrl(String restEndpointUrl);
}
