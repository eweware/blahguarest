package main.java.com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/3/12 Time: 10:35 PM
 */
public interface UserCommentInfoDAO extends BaseDAO, UserCommentInfoDAOConstants {

    public String getUserId();

    public void setUserId(String id);

    public String getCommentId();

    public void setCommentId(String id);

    public Integer getVote();

    public void setVote(Integer count);

    public Integer getViews();

    public void setViews(Integer count);

    public Integer getOpens();

    public void setOpens(Integer count);
}
