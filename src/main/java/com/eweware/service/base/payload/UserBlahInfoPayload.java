package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.store.dao.UserBlahInfoDAOConstants;

import java.util.Date;
import java.util.Map;

/**
 * <p>Entity carries poll or prediction payload.</p>
 * <p>This payload is client-facing only.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 2/18/13 Time: 4:52 PM
 */
public class UserBlahInfoPayload extends BasePayload implements UserBlahInfoDAOConstants {


    public UserBlahInfoPayload() {
        super();
    }

    public UserBlahInfoPayload(String id) {
        super(id);
    }

    public UserBlahInfoPayload(Map<String, Object> map) {
        super(map);
        ensureCanonicalDateTime(POLL_VOTE_TIMESTAMP);
    }

    public UserBlahInfoPayload(String userId, String blahId) {
        setUserId(userId);
        setBlahId(blahId);
    }

    /**
     * <p>Returns the related blah id.</p>
     * @return  The blah id
     */
    public String getBlahId() {
        return (String) get(BLAH_ID);
    }

    public void setBlahId(String blahId) {
        put(BLAH_ID, blahId);
    }

    /**
     * <p>Returns the related user id.</p>
     * @return  The user id
     */
    public String getUserId() {
        return (String) get(USER_ID);
    }

    public void setUserId(String userId) {
        put(USER_ID, userId);
    }

    /**
     * <p>Returns the index of the poll vote option that the user
     * has voted upon.</p>
     * @return  The index of the poll vote option or
     * null if the user has not voted on any poll options.
     */
    public Integer getPollVoteIndex() {
        return (Integer) get(POLL_VOTE_INDEX);
    }

    public void setPollVoteIndex(Integer pollVoteIndex) {
        put(POLL_VOTE_INDEX, pollVoteIndex);
    }

    /**
     * <p>Returns the datetime when the user voted for a poll
     * option.</p>
     * @return  the poll vote timestamp or null if the user
     * has not voted on the poll.
     */
    public Date getPollVoteTimestamp() {
        return (Date) get(POLL_VOTE_TIMESTAMP);
    }

    public void setPollVoteTimestamp(Date pollVoteTimestamp) {
        put(POLL_VOTE_TIMESTAMP, pollVoteTimestamp);
    }

    /**
     * <p>This is the user's vote for the prediction <i>before</i> it expired.</p>
     * <p>For possible values, see the codes for PredictionVote.</p>
     * @return  The user's vote for the prediction before it expired, or null
     * if the user has not voted.
     * @see main.java.com.eweware.service.mgr.type.PredictionVote
     */
    public String getPredictionVote() {
        return (String) get(PREDICTION_VOTE);
    }

    /**
     * <p>This is the user's vote for the prediction <i>after</i> it expired.</p>
     * <p>For possible values, see the codes for PredictionVote.</p>
     * @return  The user's vote for the prediction after it expired, or null
     * if the user has not voted on the result.
     * @see main.java.com.eweware.service.mgr.type.PredictionVote
     */
    public String getPredictionResultVote() {
        return (String) get(PREDICTION_RESULT_VOTE);
    }
}
