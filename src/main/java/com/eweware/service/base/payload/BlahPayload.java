package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.BlahDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.BlahSchema;
import main.java.com.eweware.service.base.store.dao.schema.GroupSchema;

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
    static final String ORIGINAL_GROUP_ID = "groupId";

    /**
     * <p>Indicates the user promotion/demotion for this blag.</p>
     * <p> This is used between the client and the service, but
     * is not stored in the db.</p>
     * TODO get rid of this https://eweware.atlassian.net/browse/WRS-234
     */
    static final String USER_PROMOTES_OR_DEMOTES = "v";

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

//    /**
//     * <p>Returns the schema for the payload's DAO representation.</p>
//     * <p><i>Called through reflection in BaseDAOImple.</i></p>
//     * @return  the dao-equivalent schema
//     * @see main.java.com.eweware.service.base.store.impl.mongo.dao.BaseDAOImpl#getDAOSchema(main.java.com.eweware.service.base.i18n.LocaleId)
//     */
//    protected static final BaseSchema getSchema() {
//        return BlahSchema.getSchema(LocaleId.en_us);
//    }

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
        put(TEXT, text);
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
        put(BODY, body);
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
     * <p>When the client user id is available (e.g., user is logged in),
     * returns the value indicating whether the user has promoted or demoted
     * this blah.</p>
     * <p>A value of +1 means the user has promoted it. A value of -1
     * means the user has demoted it. If it is null or 0, then the user
     * has neither promoted not demoted this blah.</p>
     * @return  The demotion value or null if the user has not promoted/demoted this blah.
     */
    public Integer getUserPromotesOrDemotes() {
        return (Integer) get(USER_PROMOTES_OR_DEMOTES);
    }

    public void setUserPromotesOrDemotes(Integer promotesOrDemotes) {
        put(USER_PROMOTES_OR_DEMOTES, promotesOrDemotes);
    }

    /**
     * <p>Returns the count of users who have promoted this blah.</p>
     * @return  count of users who have promoted this blah
     */
    public Integer getPromotedCount() {
        return (Integer) get(PROMOTED_COUNT);
    }

    public void setPromotedCount(Integer promotions) {
        put(PROMOTED_COUNT, promotions);
    }

    /**
     * <p>Returns the count of users who have demoted this blah.</p>
     * @return count of users who have demoted this blah
     */
    public Integer getDemotedCount() {
        return (Integer) get(DEMOTED_COUNT);
    }

    public void setDemotedCount(Integer demotions) {
        put(DEMOTED_COUNT, demotions);
    }

    /**
     * <p>Returns the number of views of this blah.</p>
     * @return  number of blah views
     */
    public Integer getViews() {
        return (Integer) get(VIEWS);
    }

    public void setViews(Integer views) {
        put(VIEWS, views);
    }

    /**
     * <p>Returns the number of times users have opened this blah</p>
     * @return  number of blah opens
     */
    public Integer getOpens() {
        return (Integer) get(OPENS);
    }

    public void setOpens(Integer opens) {
        put(OPENS, opens);
    }

    /**
     * <p>Returns the number of comments on this blah.</p>
     * @return  Number of comments
     */
    public Integer getComments() {
        return (Integer) get(COMMENTS);
    }

    public void setComments(Integer comments) {

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
    public Integer getUserPromotion() {
        return (Integer) get(USER_PROMOTION);
    }

    public void setUserPromotion(Integer promotion) {
        put(USER_PROMOTION, promotion);
    }

    /**
     * <p>Returns the number of times the blah has been viewed.</p>
     * @return the number of times the blah has been viewed
     */
    public Integer getUserViews() {
        return (Integer) get(USER_VIEWS);
    }

    public void setUserViews(Integer userViews) {
        put(USER_VIEWS, userViews);
    }

    /**
     * <p>Returns the number of times the blah has been opened.</p>
     * @return the number of times the blah has been opened
     */
    public Integer getUserOpens() {
        return (Integer) get(USER_OPENS);
    }

    public void setUserOpens(Integer userOpens) {
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
     * <p>Returns the number of poll options (number of
     * entries in poll options array.</p>
     * @return  The number of poll options
     */
    public Integer getPollOptionCount() {
        return (Integer) get(POLL_OPTION_COUNT);
    }

    /**
     * <p>Sets the number of poll options</p>
     * @param pollOptionCount   The number of poll options
     */
    public void setPollOptionCount(Integer pollOptionCount) {
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
    public List<Integer> getPollOptionVotes() {
        return (List<Integer>) get(POLL_OPTION_VOTES);
    }

    public void setPollOptionVotes(List<Integer> pollOptionVotes) {
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
    public Integer getPredictionAgreeCount() {
        return (Integer) get(PREDICTION_USER_AGREE_COUNT);
    }

    /**
     * <p>Sets number of times users have agreed with this prediction blah</p>
     * @param count The count
     */
    public void setPredictionAgreeCount(Integer count) {
        put(PREDICTION_USER_AGREE_COUNT, count);
    }

    /**
     * <p>Returns number of times users have disagreed with this prediction blah</p>
     * @return  Number of times users have agreed with this prediction blah
     */
    public Integer getPredictionDisagreeCount() {
        return (Integer) get(PREDICTION_USER_DISAGREE_COUNT);
    }

    /**
     * <p>Sets number of times users have disgreed with this prediction blah</p>
     * @param count The count
     */
    public void setPredictionDisagreeCount(Integer count) {
        put(PREDICTION_USER_DISAGREE_COUNT, count);
    }

    /**
     * <p>Returns number of times users have thought this prediction blah was unclear</p>
     * @return  the count
     */
    public Integer getPredictionUnclearCount() {
        return (Integer) get(PREDICTION_USER_UNCLEAR_COUNT);
    }

    /**
     * <p>Sets number of times users have thought that this prediction blah was unclear.</p>
     * @param count The count
     */
    public void setPredictionUnclearCount(Integer count) {
        put(PREDICTION_USER_UNCLEAR_COUNT, count);
    }

    /**
     * <p>Returns number of times users have indicated that this prediction was correct</p>
     * @return  the count
     */
    public Integer getPredictionResultCorrectCount() {
        return (Integer) get(PREDICTION_RESULT_CORRECT_COUNT);
    }

    /**
     * <p>Sets number of times users have indicated that this prediction was correct.</p>
     * @param count The count
     */
    public void setPredictionResultCorrectCount(Integer count) {
        put(PREDICTION_RESULT_CORRECT_COUNT, count);
    }

    /**
     * <p>Returns number of times users have indicated that this prediction blah was incorrect</p>
     * @return  the count
     */
    public Integer getPredictionResultIncorrectCount() {
        return (Integer) get(PREDICTION_RESULT_INCORRECT_COUNT);
    }

    /**
     * <p>Sets number of times users have indicated that this prediction was incorrect.</p>
     * @param count The count
     */
    public void setPredictionResultIncorrectCount(Integer count) {
        put(PREDICTION_RESULT_INCORRECT_COUNT, count);
    }

    /**
     * <p>Returns number of times users have indicated that this prediction's result was unclear</p>
     * @return  the count
     */
    public Integer getPredictionResultUnclearCount() {
        return (Integer) get(PREDICTION_RESULT_UNCLEAR_COUNT);
    }

    /**
     * <p>Sets number of times users have indicated that this prediction's result was unclear.</p>
     * @param count The count
     */
    public void setPredictionResultUnclearCount(Integer count) {
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
