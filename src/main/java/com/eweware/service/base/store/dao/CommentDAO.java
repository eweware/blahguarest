package main.java.com.eweware.service.base.store.dao;

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

    public Integer getBlahVote();

    public void setBlahVote(Integer vote);

    public Integer getCommentUpVotes();

    public void setCommentUpVotes(Integer votes);

    public Integer getCommentDownVotes();

    public void setCommentDownVotes(Integer votes);

    public Integer getCommentVotes();

    public void setCommentVotes(Integer votes);

    public Double getString();

    public void setStrength(Double strength);

    public Integer getViews();

    public void setViews(Integer views);

    public Integer getOpens();

    public void setOpens(Integer opens);

    public List<CommentTrackerDAO> getStats();

    public void setStats(List<CommentTrackerDAO> stats);

    public Date getCreated();

    public void setCreated(Date created);
}
