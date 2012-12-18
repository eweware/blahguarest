package main.java.com.eweware.service.base.store.dao;

import java.util.Date;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/22/12 Time: 3:09 PM
 */
public interface TrackerDAO extends BaseDAO, TrackerDAOConstants {

    public String getOperation();

    public void setOperation(String operation);

    public Integer getVote();

    public void setVote(Integer vote);

    public Integer getViews();

    public void setViews(Integer views);

    public Integer getOpens();

    public void setOpens(Integer opens);

    public String getState();

    public void setState(String state);

    public String getGroupTypeId();

    public void setGroupTypeId(String groupTypeId);

    public String getGroupId();

    public void setGroupId(String groupId);

    public String getUserId();

    public void setUserId(String userId);

    public String getBlahAuthorId();

    public void setBlahAuthorId(String authorId);

    public String getCommentAuthorId();

    public void setCommentAuthorId(String authorId);

    public String getBlahId();

    public void setBlahId(String blahId);

    public String getCommentId();

    public void setCommentId(String commentId);

    public String getUserGender();

    public void setUserGender(String gender);

    public String getUserRace();

    public void setUserRace(String race);

    public String getUserIncomeRange();

    public void setUserIncomeRange(String incomeRange);

    public Date getUserDateOfBirth();

    public void setUserDateOfBirth(Date dob);
}
