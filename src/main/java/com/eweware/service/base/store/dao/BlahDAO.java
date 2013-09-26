package com.eweware.service.base.store.dao;

import com.eweware.service.base.error.SystemErrorException;

import java.util.Date;
import java.util.List;

/**
 * <p>This is the basic blah data object. The fields in a blah dependend on
 * the type of blah it is. They all require the 'text' field, which
 * is a short line.</p>
 * <p><b>Says Blahs:</b> Optionally uses a 'body' field.</p>
 * <p><b>Poll Blahs:</b> Optionally uses a 'body' field, which might amplify on what the
 * poll is about or give instructions.</p>
 * <p/>
 *
 * @author rk@post.harvard.edu
 *         Date: 7/2/12 Time: 1:54 PM
 */
public interface BlahDAO extends BaseDAO, BlahDAOConstants {

    /**
     * <p>Returns the blah's type id.</p>
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
     * <p>Returns the blah's author's id.</p>
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
     * <p>Returns the blah's text (tagline).</p>
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
     * <p>Returns the optional blah body text.</p>
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
     * <p> A list of image ids associated with this blah.</p>
     * @return A list of image ids associated with this blah.
     */
    public List<String> getImageIds();

    /**
     * Sets the image ids associated with this blah.
     *
     * @param imageIds A list of image ids
     */
    public void setImageIds(List<String> imageIds);

    /**
     * <p>Returns the badge ids associated with this blah or null if there are none.</p>
     * @return List of badge ids or null.
     */
    public List<String> getBadgeIds();

    /**
     * <p>Sets this blah's badge ids.</p>
     * @param badgeIds
     */
    public void setBadgeIds(List<String> badgeIds);

    /**
     * Returns the number of poll options (number of
     * entries in poll options array.
     *
     * @return The number of poll options
     */
    public Long getPollOptionCount();

    /**
     * Sets the number of poll options
     *
     * @param pollOptionCount The number of poll options
     */
    public void setPollOptionCount(Long pollOptionCount);

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
    public List<Long> getPollOptionVotes();

    /**
     * Sets the votes for the poll options
     *
     * @param pollOptionVotes The votes for the poll options
     */
    public void setPollOptionVotes(List<Long> pollOptionVotes);

    /**
     * Adds one vote to this blah's poll for the specified
     * option index.
     *
     * @param pollOptionIndex The option index
     */
    public void addPollOptionVote_immediate(Long pollOptionIndex) throws SystemErrorException;

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
     * @return Long Count of users who have promoted this blah.
     */
    public Long getPromotedCount();

    /**
     * Sets the times this blah has been promoted.
     *
     * @param promotions The number of promotions
     */
    public void setPromotedCount(Long promotions);

    /**
     * <p>Returns the number of demotions for this blah.</p>
     *
     * @return Long Returns the number of demotions
     */
    public Long getDemotedCount();

    /**
     * <p> Sets the number of demotions for this blah.</p>
     *
     * @param demotions The number of demotions
     */
    public void setDemotedCount(Long demotions);

    /**
     * <p>Returns the expiration date, if any, of this blah. Used, e.g.,
     * for predictions.</p>
     *
     * @return The expiration date or null if none.
     */
    public Date getExpirationDate();

    /**
     * <p>Sets this blah's expiration date. Used, e.g., in predictions.</p>
     *
     * @param date The expiration date
     */
    public void setExpirationDate(Date date);

    /**
     * <p>Returns number of times users have agreed with this prediction blah</p>
     *
     * @return Number of times users have agreed with this prediction blah
     */
    public Long getPredictionAgreeCount();

    /**
     * <p>Sets number of times users have agreed with this prediction blah</p>
     *
     * @param count The count
     */
    public void setPredictionAgreeCount(Long count);

    /**
     * <p>Returns number of times users have disagreed with this prediction blah</p>
     *
     * @return Number of times users have agreed with this prediction blah
     */
    public Long getPredictionDisagreeCount();

    /**
     * <p>Sets number of times users have disgreed with this prediction blah</p>
     *
     * @param count The count
     */
    public void setPredictionDisagreeCount(Long count);

    /**
     * <p>Returns number of times users have thought this prediction blah was unclear</p>
     *
     * @return the count
     */
    public Long getPredictionUnclearCount();

    /**
     * <p>Sets number of times users have thought that this prediction blah was unclear.</p>
     *
     * @param count The count
     */
    public void setPredictionUnclearCount(Long count);

    /**
     * <p>Returns number of times users have indicated that this prediction was correct</p>
     *
     * @return the count
     */
    public Long getPredictionResultCorrectCount();

    /**
     * <p>Sets number of times users have indicated that this prediction was correct.</p>
     *
     * @param count The count
     */
    public void setPredictionResultCorrectCount(Long count);

    /**
     * <p>Returns number of times users have indicated that this prediction blah was incorrect</p>
     *
     * @return the count
     */
    public Long getPredictionResultIncorrectCount();

    /**
     * <p>Sets number of times users have indicated that this prediction was incorrect.</p>
     *
     * @param count The count
     */
    public void setPredictionResultIncorrectCount(Long count);

    /**
     * <p>Returns number of times users have indicated that this prediction's result was unclear</p>
     *
     * @return the count
     */
    public Long getPredictionResultUnclearCount();

    /**
     * <p>Sets number of times users have indicated that this prediction's result was unclear.</p>
     *
     * @param count The count
     */
    public void setPredictionResultUnclearCount(Long count);

    /**
     * @return Long  Returns the number of views of this blah to this moment.
     */
    public Long getViews();

    /**
     * Sets the views for this blah to this moment.
     *
     * @param views The number of views.
     */
    public void setViews(Long views);

    /**
     * @return Long Returns the number of times the blah has been opened to this moment.
     */
    public Long getOpens();

    /**
     * Sets the number of times the blah has been opened.
     *
     * @param opens The number of times the blah has been opened.
     */
    public void setOpens(Long opens);

    /**
     * @return Long  Returns the number of comments for this blah.
     */
    public Long getComments();

    /**
     * Sets the number of comments for this blah.
     *
     * @param comments Number of comments for this blah.
     */
    public void setComments(Long comments);

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
