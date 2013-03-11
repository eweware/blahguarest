package main.java.com.eweware.service.base.store.dao;

import main.java.com.eweware.service.base.error.SystemErrorException;

import java.util.Date;
import java.util.List;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/2/12 Time: 1:54 PM
 *         <p/>
 *         <p>This is the basic blah data object. The fields in a blah dependend on
 *         the type of blah it is. They all require the 'text' field, which
 *         is a short line.</p>
 *         <p><b>Says Blahs:</b> Optionally uses a 'body' field.</p>
 *         <p><b>Poll Blahs:</b> Optionally uses a 'body' field, which might amplify on what the
 *         poll is about or give instructions.</p>
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
     * @return String  The optional blah body text
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
     *
     * @param imageIds A list of image ids
     */
    public void setImageIds(List<String> imageIds);

    /**
     * Returns the number of poll options (number of
     * entries in poll options array.
     *
     * @return The number of poll options
     */
    public Integer getPollOptionCount();

    /**
     * Sets the number of poll options
     *
     * @param pollOptionCount The number of poll options
     */
    public void setPollOptionCount(Integer pollOptionCount);

    /**
     * Returns the poll option items' text
     *
     * @return An array with the text option's text
     */
    public List<PollOptionTextDAO> getPollOptionsText();

    /**
     * Sets the text for each of the poll option's text fields.
     *
     * @param pollOptionText The poll option's text
     */
    public void setPollOptionsText(List<PollOptionTextDAO> pollOptionText);

    /**
     * Returns the votes for the poll options. The vote for the Nth (0-origin)
     * poll option text element is in the Nth element in this array.
     *
     * @return The votes for the poll options
     */
    public List<Integer> getPollOptionVotes();

    /**
     * Sets the votes for the poll options
     *
     * @param pollOptionVotes The votes for the poll options
     */
    public void setPollOptionVotes(List<Integer> pollOptionVotes);

    /**
     * Adds one vote to this blah's poll for the specified
     * option index.
     *
     * @param pollOptionIndex The option index
     */
    public void addPollOptionVote_immediate(Integer pollOptionIndex) throws SystemErrorException;

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
     * <p>Returns the number of promotions for this blah.</p>
     *
     * @return Integer Count of users who have promoted this blah.
     */
    public Integer getPromotedCount();

    /**
     * Sets the times this blah has been promoted.
     *
     * @param promotions The number of promotions
     */
    public void setPromotedCount(Integer promotions);

    /**
     * <p>Returns the number of demotions for this blah.</p>
     *
     * @return Integer Returns the number of demotions
     */
    public Integer getDemotedCount();

    /**
     * <p> Sets the number of demotions for this blah.</p>
     *
     * @param demotions The number of demotions
     */
    public void setDemotedCount(Integer demotions);

    /**
     * <p>Returns the expiration date, if any, of this blah. Used, e.g.,
     * for predictions.</p>
     * @return The expiration date or null if none.
     */
    public Date getExpirationDate();

    /**
     * <p>Sets this blah's expiration date. Used, e.g., in predictions.</p>
     * @param date The expiration date
     */
    public void setExpirationDate(Date date);

    /**
     * <p>Returns number of times users have agreed with this prediction blah</p>
     * @return  Number of times users have agreed with this prediction blah
     */
    public Integer getPredictionAgreeCount();

    /**
     * <p>Sets number of times users have agreed with this prediction blah</p>
     * @param count The count
     */
    public void setPredictionAgreeCount(Integer count);

    /**
     * <p>Returns number of times users have disagreed with this prediction blah</p>
     * @return  Number of times users have agreed with this prediction blah
     */
    public Integer getPredictionDisagreeCount();

    /**
     * <p>Sets number of times users have disgreed with this prediction blah</p>
     * @param count The count
     */
    public void setPredictionDisagreeCount(Integer count);

    /**
     * <p>Returns number of times users have thought this prediction blah was unclear</p>
     * @return  the count
     */
    public Integer getPredictionUnclearCount();

    /**
     * <p>Sets number of times users have thought that this prediction blah was unclear.</p>
     * @param count The count
     */
    public void setPredictionUnclearCount(Integer count);

    /**
     * <p>Returns number of times users have indicated that this prediction was correct</p>
     * @return  the count
     */
    public Integer getPredictionResultCorrectCount();

    /**
     * <p>Sets number of times users have indicated that this prediction was correct.</p>
     * @param count The count
     */
    public void setPredictionResultCorrectCount(Integer count);

    /**
     * <p>Returns number of times users have indicated that this prediction blah was incorrect</p>
     * @return  the count
     */
    public Integer getPredictionResultIncorrectCount();

    /**
     * <p>Sets number of times users have indicated that this prediction was incorrect.</p>
     * @param count The count
     */
    public void setPredictionResultIncorrectCount(Integer count);

    /**
     * <p>Returns number of times users have indicated that this prediction's result was unclear</p>
     * @return  the count
     */
    public Integer getPredictionResultUnclearCount();

    /**
     * <p>Sets number of times users have indicated that this prediction's result was unclear.</p>
     * @param count The count
     */
    public void setPredictionResultUnclearCount(Integer count);

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
     *
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
     *
     * @return Double   Returns the blah's strength
     */
    public Double getStrength();

    /**
     * Sets the blah's all-time strength.
     *
     * @param strength The blah's strength
     */
    public void setStrength(Double strength);

    /**
     * Returns recent strength. The definition of
     * "recent" may vary (e.g., a week) from time to time.
     *
     * @return Double   The blah's recent strength
     */
    public Double getRecentStrength();

    /**
     * Sets the blah's "recent" strength
     *
     * @param strength
     */
    public void setRecentStrength(Double strength);
}
