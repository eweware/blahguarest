package main.java.com.eweware.service.base.store.dao;

import java.util.Date;

/**
 * <p>Currently not in use. Should replace the large by-month tracker when time allows.</p>
 * <p>Reason to replace it is that: (1) it ties us to MongoDB bigtime, and (2) even
 * for Mongo, we don't want to detal with large objects that can potentially
 * consume RAM workspace: ideally, we would have smaller consecutively
 * placed chunks in disc... since we might not be able to control or predict how Mongo lays it out,
 * the benefits of having smaller chunks in mongo are unclear without testing.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 9/22/12 Time: 3:09 PM
 */
public interface TrackerDAO extends BaseDAO, TrackerDAOConstants {

    public String getOperation();

    public void setOperation(String operation);

    public Long getVote();

    public void setVote(Long vote);

    public Long getViews();

    public void setViews(Long views);

    public Long getOpens();

    public void setOpens(Long opens);

    public String getState();

    public void setState(String state);

    public String getGroupTypeId();

    public void setGroupTypeId(String groupTypeId);

    public String getGroupId();

    public void setGroupId(String groupId);

    public String getUserId();

    public void setUserId(String userId);

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
