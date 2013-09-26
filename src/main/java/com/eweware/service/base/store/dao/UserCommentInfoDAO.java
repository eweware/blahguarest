package com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/3/12 Time: 10:35 PM
 */
public interface UserCommentInfoDAO extends BaseDAO, UserCommentInfoDAOConstants {

    public String getUserId();

    public void setUserId(String id);

    public String getAuthorId();

    public void setAuthorId(String authorId);

    public String getCommentId();

    public void setCommentId(String id);

    public Long getVote();

    public void setVote(Long count);

    public Long getViews();

    public void setViews(Long count);

    public Long getOpens();

    public void setOpens(Long count);
}
