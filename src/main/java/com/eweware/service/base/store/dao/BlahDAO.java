package main.java.com.eweware.service.base.store.dao;

import java.util.List;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/2/12 Time: 1:54 PM
 */
public interface BlahDAO extends BaseDAO, BlahDAOConstants {

    /**
     * @return String  Returns the blah's type id
     */
    public String getTypeId();

    /**
     * Sets the blah's type id
     *
     * @param type A valid blah type id
     */
    public void setTypeId(String type);

    /**
     * @return String   Returns the blah author's id
     */
    public String getAuthorId();

    /**
     * Sets the blah's author id.
     *
     * @param authorId The blah's author id
     */
    public void setAuthorId(String authorId);

    /**
     * @return String   Returns the blah's text
     */
    public String getText();

    /**
     * Sets the blah's text
     *
     * @param text The blah's text
     */
    public void setText(String text);

    /**
     * @return  String  The optional blah body text
     */
    public String getBody();

    /**
     * Sets the optional blah body text.
     *
     * @param body
     */
    public void setBody(String body);

    /**
     * @return List<String> A list of image ids associated with this blah.
     */
    public List<String> getImageIds();

    /**
     * Sets the image ids associated with this blah.
     * @param imageIds A list of image ids
     */
    public void setImageIds(List<String> imageIds);

    /**
     * @return String Returns the blah's injected group id
     */
    public String getGroupId();

    /**
     * Sets the blah's injection group id
     *
     * @param groupId String The injection group id
     */
    public void setGroupId(String groupId);

    /**
     * The blah's cumulative vote is the number of positive votes
     * minus the number of negative votes. Therefore, this number
     * may be negative.
     *
     * @return Integer Returns the blah's cumulative vote to this moment.
     */
    public Integer getVotes();

    /**
     * Sets the blah's cumulative vote.
     *
     * @param votes The votes
     */
    public void setVotes(Integer votes);

    /**
     * @return Integer Number of up votes.
     */
    public Integer getUpVotes();

    /**
     * Sets the number of up votes
     *
     * @param votes The up votes
     */
    public void setUpVotes(Integer votes);

    /**
     * @return Integer Returns the number of down votes
     */
    public Integer getDownVotes();

    /**
     * Sets the number of down votes
     *
     * @param votes The down votes
     */
    public void setDownVotes(Integer votes);

    /**
     * @return Integer  Returns the number of views of this blah to this moment.
     */
    public Integer getViews();

    /**
     * Sets the views for this blah to this moment.
     *
     * @param views The number of views.
     */
    public void setViews(Integer views);

    /**
     * @return Integer Returns the number of times the blah has been opened to this moment.
     */
    public Integer getOpens();

    /**
     * Sets the number of times the blah has been opened.
     *
     * @param opens The number of times the blah has been opened.
     */
    public void setOpens(Integer opens);

    /**
     * @return Integer  Returns the number of comments for this blah.
     */
    public Integer getComments();

    /**
     * Sets the number of comments for this blah.
     * @param comments Number of comments for this blah.
     */
    public void setComments(Integer comments);

    /**
     * @return List<BlabTrackerDAO> Returns the possibly empty list of statistics for this blah.
     */
    public List<BlahTrackerDAO> getStats();

    /**
     * Gets the stats requested for this blah.
     *
     * @param stats The list of tracker daos (the stats)
     */
    public void setStats(List<BlahTrackerDAO> stats);

    /**
     * Returns the blah's all-time strength.
     * @return Double   Returns the blah's strength
     */
    public Double getStrength();

    /**
     * Sets the blah's all-time strength.
     * @param strength The blah's strength
     */
    public void setStrength(Double strength);

    /**
     * Returns recent strength. The definition of
     * "recent" may vary (e.g., a week) from time to time.
     * @return Double   The blah's recent strength
     */
    public Double getRecentStrength();

    /**
     * Sets the blah's "recent" strength
     * @param strength
     */
    public void setRecentStrength(Double strength);
}
