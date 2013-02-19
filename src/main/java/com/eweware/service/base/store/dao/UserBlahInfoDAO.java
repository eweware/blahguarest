package main.java.com.eweware.service.base.store.dao;

import java.util.Date;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/2/12 Time: 2:35 PM
 */
public interface UserBlahInfoDAO extends BaseDAO, UserBlahInfoDAOConstants {

    public String getUserId();

    public void setUserId(String userId);

    public String getBlahId();

    public void setBlahId(String blahId);

    public String getGroupId();

    public void setGroupId(String groupId);

    public String getBlahTypeId();

    public void setBlahTypeId(String blahTypeId);

    public Integer getVote();

    public void setVote(Integer vote);

    public Integer getPollVoteIndex();

    public void setPollVoteIndex(Integer pollIndex);

    public Date getPollVoteTimestamp();

    public void setPollVoteTimestamp(Date timestamp);

    public Integer getViews();

    public void setViews(Integer count);

    public Integer getOpens();

    public void setOpens(Integer count);

    public Integer getComments();

    public void setComments(Integer comment);
}
