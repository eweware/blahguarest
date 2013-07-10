package main.java.com.eweware.service.base.store.dao;

import java.util.Date;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/2/12 Time: 2:35 PM
 */
public interface UserBlahInfoDAO extends BaseDAO, UserBlahInfoDAOConstants {

    public String getUserId();

    public void setUserId(String userId);

    public String getAuthorId();

    public void setAuthorId(String authorId);

    public String getBlahId();

    public void setBlahId(String blahId);

    public String getGroupId();

    public void setGroupId(String groupId);

    public String getBlahTypeId();

    public void setBlahTypeId(String blahTypeId);

    public Long getPromotedOrDemoted();

    public void setPromotedOrDemoted(Long promotedOrDemoted);

    public Long getPollVoteIndex();

    public void setPollVoteIndex(Long pollIndex);

    public Date getPollVoteTimestamp();

    public void setPollVoteTimestamp(Date timestamp);

    public String getPredictionVote();

    public void setPredictionVote(String vote);

    public String getPredictionResultVote();

    public void setPredictionResultVote(String vote);

    public Long getViews();

    public void setViews(Long count);

    public Long getOpens();

    public void setOpens(Long count);

    public Long getComments();

    public void setComments(Long comment);
}
