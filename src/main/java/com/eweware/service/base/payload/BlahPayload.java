package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.CommonUtilities;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.BlahDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.BlahSchema;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>Entity typically used to communicate about a blah's contents
 * between the server and clients.</p>
 *
 * @author rk@post.harvard.edu
 */
public final class BlahPayload extends BasePayload implements BlahDAOConstants {



    /**
     * <p>This is the id of the group into which this blah
     * was originally injected.</p>
     */
    static final String ORIGINAL_GROUP_ID = "G";

    /**
     * <p>Records whether the user promoted this blah up or down.</p>
     * <p> This is used between the client and the service, but
     * is not stored in the db.</p>
     */
    static final String USER_PROMOTION = "uv";

    /**
     * <p>Records the number of times this user has viewed this blah.</p>
     * <p> This is used between the client and the service, but
     * is not stored in the db.</p>
     */
    static final String USER_VIEWS = "uw";

    /**
     * <p>Records the number of times this user has opened this blah.</p>
     * <p> This is used between the client and the service, but
     * is not stored in the db.</p>
     */
    static final String USER_OPENS = "uo";

    /**
     * <p>Returns the poll option vote index from a client.
     * This is an index that must be less than the number
     * of poll options on a poll type blah. A value
     * in this field means that the client's user wants
     * to vote for the poll option with the specified index.</p>
     */
    static final String POLL_OPTION_VOTE_INDEX = "povi";

    protected static final BaseSchema getSchema() {
        return BlahSchema.getSchema(LocaleId.en_us);
    }

    public BlahPayload() {
        super();
    }

    public BlahPayload(String id) {
        super(id);
    }

    public BlahPayload(Map<String, Object> map) {
        super(map);
    }

    /**
     * <p>Returns the blah's type id</p>
     * @return  The blah's type id
     */
    public String getTypeId() {
        return (String) get(TYPE_ID);
    }

    /**
     * <p>Sets the blah's type id</p>
     * @param type  The blah's type id
     */
    public void setTypeId(String type) {
        put(TYPE_ID, type);
    }

    /**
     * <p>Returns the blah's text. This is the one liner
     * that is required of all blahs.</p>
     * @return  The blah's text
     */
    public String getText() {
        return (String) get(TEXT);
    }

    /***
     * <p>Sets the blah's text.</p>
     * @param text The blah's text
     */
    public void setText(String text) {
        if (text == null) {
            remove(TEXT);
        } else {
            put(TEXT, text);
        }
    }

    /**
     * <p>Returns the blah's body. This is additional text
     * and it may be up to 4000 characters in length.</p>
     * @return  The blah's body text or null if the blah has no body text.
     */
    public String getBody() {
        return (String) get(BODY);
    }

    /**
     * <p>Sets the blah's body text</p>
     * @param body  The blah's body text
     */
    public void setBody(String body) {
        if (body == null) {
            remove(BODY);
        } else {
            put(BODY, body);
        }
    }

    /**
     * <p>Returns the id of the blah's author.</p>
     * @return  The blah author's id
     */
    public String getAuthorId() {
        return (String) get(AUTHOR_ID);
    }

    /**
     * <p>Sets the blah's author id</p>
     * @param authorId  The author id
     */
    public void setAuthorId(String authorId) {
        put(AUTHOR_ID, authorId);
    }

    /**
     * <p>Returns the count of users who have promoted this blah.</p>
     * @return  count of users who have promoted this blah
     */
    public Long getPromotedCount() {
        return CommonUtilities.getValueAsLong(get(PROMOTED_COUNT), null);
    }

    public void setPromotedCount(Long promotions) {
        put(PROMOTED_COUNT, promotions);
    }

    /**
     * <p>Returns the count of users who have demoted this blah.</p>
     * @return count of users who have demoted this blah
     */
    public Long getDemotedCount() {
        return CommonUtilities.getValueAsLong(get(DEMOTED_COUNT), null);
    }

    public void setDemotedCount(Long demotions) {
        put(DEMOTED_COUNT, demotions);
    }

    /**
     * <p>Returns the number of views of this blah.</p>
     * @return  number of blah views
     */
    public Long getViews() {
        return CommonUtilities.getValueAsLong(get(VIEWS), null);
    }

    public void setViews(Long views) {
        put(VIEWS, views);
    }

    /**
     * <p>Returns the number of times users have opened this blah</p>
     * @return  number of blah opens
     */
    public Long getOpens() {
        return CommonUtilities.getValueAsLong(get(OPENS), null);
    }

    public void setOpens(Long opens) {
        put(OPENS, opens);
    }

    /**
     * <p>Returns the number of comments on this blah.</p>
     * @return  Number of comments
     */
    public Long getComments() {
        return CommonUtilities.getValueAsLong(get(COMMENTS), null);
    }

    public void setComments(Long comments) {
        put(COMMENTS, comments);
    }

    /**
     * <p>Returns the id of the group into which this blah was
     * originally inserted.</p>
     * @return  The group id
     */
    public String getGroupId() {
        return (String) get(ORIGINAL_GROUP_ID);
    }

    public void setGroupId(String groupId) {
        put(ORIGINAL_GROUP_ID, groupId);
    }

    /**
     * <p>Returns an indication of whether the user has promoted or
     * demoted the blah. +1 means promoted, -1 means demoted, </p>
     * @return   The user promotion/demotion flag
     */
    public Long getUserPromotion() {
        return CommonUtilities.getValueAsLong(get(USER_PROMOTION), null);
    }

    public void setUserPromotion(Long promotion) {
        put(USER_PROMOTION, promotion);
    }

    /**
     * <p>Returns the number of times the blah has been viewed.</p>
     * @return the number of times the blah has been viewed
     */
    public Long getUserViews() {
        return CommonUtilities.getValueAsLong(get(USER_VIEWS), null);
    }

    public void setUserViews(Long userViews) {
        put(USER_VIEWS, userViews);
    }

    /**
     * <p>Returns the number of times the blah has been opened.</p>
     * @return the number of times the blah has been opened
     */
    public Long getUserOpens() {
        return CommonUtilities.getValueAsLong(get(USER_OPENS), null);
    }

    public void setUserOpens(Long userOpens) {
        put(USER_OPENS, userOpens);
    }

    /**
     * <p>Returns a list of image ids associated with this blah</p>
     * @return A list of image ids associated with this blah.
     */
    public List<String> getImageIds() {
        return (List<String>) get(IMAGE_IDS);
    }

    /**
     * <p>Sets the image ids associated with this blah.</p>
     * @param imageIds A list of image ids
     */
    public void setImageIds(List<String> imageIds) {
        put(IMAGE_IDS, imageIds);
    }

    /**
     * <p>Returns the list of badge ids associated with this blah.</p>
     * @return  List of badge ids or null if there are none.
     */
    public List<String> getBadgeIds() {
        return (List<String>) get(BADGE_IDS);
    }

    /**
     * <p>Sets the list of badge ids associated with this blah.</p>
     * @param badgeIds
     */
    public void setBadgeIds(List<String> badgeIds) {
        put(BADGE_IDS, badgeIds);
    }

    /**
     * <p>Returns the number of poll options (number of
     * entries in poll options array.</p>
     * @return  The number of poll options
     */
    public Long getPollOptionCount() {
        return CommonUtilities.getValueAsLong(get(POLL_OPTION_COUNT), null);
    }


    /**
     * <p>Sets the number of poll options</p>
     * @param pollOptionCount   The number of poll options
     */
    public void setPollOptionCount(Long pollOptionCount) {
        put(POLL_OPTION_COUNT, pollOptionCount);
    }

    /**
     * <p>Returns the poll option items' text</p>
     * @return An array with the text option's text
     */
    public List<PollOptionsTextPayload> getPollOptionsText() {
        return (List<PollOptionsTextPayload>) get(POLL_OPTIONS_TEXT);
    }

    /**
     * <p>Sets the text for each of the poll options.</p>
     * @param options   The options
     */
    public void setPollOptionsText(List<PollOptionsTextPayload> options) {
        put(POLL_OPTIONS_TEXT, options);
    }

    /**
     * <p>Returns a list of user votes on poll options.</p>
     * <p>The order of the votes in the list correspond to the
     * order of poll options.</p>
     * @return votes on poll options
     */
    public List<Long> getPollOptionVotes() {
        return CommonUtilities.getListAsLongs(get(POLL_OPTION_VOTES), null);
    }

    public void setPollOptionVotes(List<Long> pollOptionVotes) {
        put(POLL_OPTION_VOTES, pollOptionVotes);
    }

    /**
     * <p>Returns the poll option vote index from a client.
     * This is an index that must be less than the number
     * of poll options on a poll type blah. A value
     * in this field means that the client's user wants
     * to vote for the poll option with the specified index.</p>
     * @return  The poll option vote index
     */
    public Integer getPollOptionVoteIndex() {
        return (Integer) get(POLL_OPTION_VOTE_INDEX);
    }

    /**
     * <p>Returns the expiration date,
     * if any, of this blah. Used, e.g., for predictions.</p>
     * @return The expiration date or null if none.
     */
    public Date getExpirationDate() {
        return (Date) get(EXPIRATION_DATE);
    }

    /**
     * <p>Sets this blah's expiration date. Used, e.g., in predictions.</p>
     * @param date The expiration date
     */
    public void setExpirationDate(Date date) {
        put(EXPIRATION_DATE, date);
    }

    /**
     * <p>Returns number of times users have agreed with this prediction blah</p>
     * @return  Number of times users have agreed with this prediction blah
     */
    public Long getPredictionAgreeCount() {
        return CommonUtilities.getValueAsLong(get(PREDICTION_USER_AGREE_COUNT), null);
    }

    /**
     * <p>Sets number of times users have agreed with this prediction blah</p>
     * @param count The count
     */
    public void setPredictionAgreeCount(Long count) {
        put(PREDICTION_USER_AGREE_COUNT, count);
    }

    /**
     * <p>Returns number of times users have disagreed with this prediction blah</p>
     * @return  Number of times users have agreed with this prediction blah
     */
    public Long getPredictionDisagreeCount() {
        return CommonUtilities.getValueAsLong(get(PREDICTION_USER_DISAGREE_COUNT), null);
    }

    /**
     * <p>Sets number of times users have disgreed with this prediction blah</p>
     * @param count The count
     */
    public void setPredictionDisagreeCount(Long count) {
        put(PREDICTION_USER_DISAGREE_COUNT, count);
    }

    /**
     * <p>Returns number of times users have thought this prediction blah was unclear</p>
     * @return  the count
     */
    public Long getPredictionUnclearCount() {
        return CommonUtilities.getValueAsLong(get(PREDICTION_USER_UNCLEAR_COUNT), null);
    }

    /**
     * <p>Sets number of times users have thought that this prediction blah was unclear.</p>
     * @param count The count
     */
    public void setPredictionUnclearCount(Long count) {
        put(PREDICTION_USER_UNCLEAR_COUNT, count);
    }

    /**
     * <p>Returns number of times users have indicated that this prediction was correct</p>
     * @return  the count
     */
    public Long getPredictionResultCorrectCount() {
        return CommonUtilities.getValueAsLong(get(PREDICTION_RESULT_CORRECT_COUNT), null);
    }

    /**
     * <p>Sets number of times users have indicated that this prediction was correct.</p>
     * @param count The count
     */
    public void setPredictionResultCorrectCount(Long count) {
        put(PREDICTION_RESULT_CORRECT_COUNT, count);
    }

    /**
     * <p>Returns number of times users have indicated that this prediction blah was incorrect</p>
     * @return  the count
     */
    public Long getPredictionResultIncorrectCount() {
        return CommonUtilities.getValueAsLong(get(PREDICTION_RESULT_INCORRECT_COUNT), null);
    }

    /**
     * <p>Sets number of times users have indicated that this prediction was incorrect.</p>
     * @param count The count
     */
    public void setPredictionResultIncorrectCount(Long count) {
        put(PREDICTION_RESULT_INCORRECT_COUNT, count);
    }

    /**
     * <p>Returns number of times users have indicated that this prediction's result was unclear</p>
     * @return  the count
     */
    public Long getPredictionResultUnclearCount() {
        return CommonUtilities.getValueAsLong(get(PREDICTION_RESULT_UNCLEAR_COUNT), null);
    }

    /**
     * <p>Sets number of times users have indicated that this prediction's result was unclear.</p>
     * @param count The count
     */
    public void setPredictionResultUnclearCount(Long count) {
        put(PREDICTION_RESULT_UNCLEAR_COUNT, count);
    }

    /**
     * <p>Returns the blah's current overall strength.</p>
     * @return  The blah's overall strength
     */
    public Double getStrength() {
        return (Double) get(BLAH_STRENGTH);
    }

    /**
     * <p>Sets the blah's overall strength.</p>
     * @param strength  The blah's overall strength.
     */
    public void setStrength(Double strength) {
        put(BLAH_STRENGTH, strength);
    }

    /**
     * <p>Returns the blah's recent strength.</p>
     * @return  The blah's recent strength.
     */
    public Double getRecentStrength() {
        return (Double) get(RECENT_BLAH_STRENGTH);
    }

    /**
     * <p>Sets the blah's recent strength.</p>
     * @param strength  The blah's recent strength.
     */
    public void setRecentStrength(Double strength) {
        put(RECENT_BLAH_STRENGTH, strength);
    }

    /**
     * <p>Returns the blah's statistics.</p>
     * @return  The blah's statistics
     */
    public List<BlahTrackerPayload> getStats() {
        return (List<BlahTrackerPayload>) get(STATS);
    }

    /**
     * <p>Sets the blah's statistics</p>
     * @param stats The blah's statistics.
     */
    public void setStats(List<BlahTrackerPayload> stats) {
        put(STATS, stats);
    }
}
