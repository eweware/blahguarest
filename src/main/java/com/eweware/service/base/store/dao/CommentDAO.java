package com.eweware.service.base.store.dao;

import java.util.Date;
import java.util.List;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/2/12 Time: 1:09 PM
 */
public interface CommentDAO extends BaseDAO, CommentDAOConstants {

    public String getAuthorId();

    public void setAuthorId(String authorId);

    public String getBlahId();

    public void setBlahId(String blahId);

    public String getText();

    public void setText(String text);

    public List<String> getImageIds();

    public void setImageIds(List<String> imageIds);

    public Long getBlahVote();

    public void setBlahVote(Long vote);

    public Long getCommentUpVotes();

    public void setCommentUpVotes(Long votes);

    public Long getCommentDownVotes();

    public void setCommentDownVotes(Long votes);

    public Long getCommentVotes();

    public void setCommentVotes(Long votes);

    public Double getStrength();

    public void setStrength(Double strength);

    public Long getViews();

    public void setViews(Long views);

    public Long getOpens();

    public void setOpens(Long opens);

    public List<CommentTrackerDAO> getStats();

    public void setStats(List<CommentTrackerDAO> stats);

    public Date getCreated();

    public void setCreated(Date created);
}
