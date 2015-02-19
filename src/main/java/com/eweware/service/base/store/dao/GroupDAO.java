package com.eweware.service.base.store.dao;

import org.apache.xpath.operations.Bool;

import java.util.Date;
import java.util.List;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/2/12 Time: 1:14 PM
 */
public interface GroupDAO extends BaseDAO, GroupDAOConstants {

    public String getDisplayName();

    public void setDisplayName(String displayName);

    public String getDescription();

    public void setDescription(String description);

    public String getDescriptor();

    public void setDescriptor(String descriptor);

    public String getGroupTypeId();

    public void setGroupTypeId(String groupTypeId);

    public Integer getRank();

    public void setRank(Integer rank);

    public String getState();

    public void setState(String state);

    public Long getUserCount();

    public void setUserCount(Long userCount);

    public Long getBlahCount();

    public void setBlahCount(Long blahCount);

    public Long getCurrentViewerCount();

    public void setCurrentViewerCount(Long activeViewerCount);

    public Integer getFirstInboxNumber();

    public void setFirstInboxNumber(Integer number);

    public Integer getLastInboxNumber();

    public void setLastInboxNumber(Integer number);

    public Integer getFirstSafeInboxNumber();

    public void setFirstSafeInboxNumber(Integer number);

    public Integer getLastSafeInboxNumber();

    public void setLastSafeInboxNumber(Integer number);

    public Date getLastInboxGenerated();

    public void setLastInboxGenerated(Date date);

    public Long getLastInboxGeneratedDuration();

    public void setLastInboxGeneratedDuration(Long duration);

    public Date getCreated();

    public void setCreated(Date created);

    public List<String> getAdmin();

    public void setAdmin(List<String> adminID);

    public String getHeaderImage();

    public void setHeaderImage(String headerImage);

    public String getAdBlahID();

    public void setAdBlahID(String adBlahId);

    public List<String> getJoinBadgeList();

    public void setJoinBadgeList(List<String> badgeList);

    public List<String> getCommentBadgeList();

    public void setCommentBadgeList(List<String> badgeList);

    public List<String> getPostBadgeList();

    public void setPostBadgeList(List<String> badgeList);

    public List<String> getModerateBadgeList();

    public void setModerateBadgeList(List<String> badgeList);

    public int getContextExpirationDays();

    public void setContentExpirationDays(int numDays);

    public Boolean getModerated();

    public void setModerated(Boolean isModerated);



}
