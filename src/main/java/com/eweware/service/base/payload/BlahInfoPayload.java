package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.store.dao.UserBlahInfoDAOConstants;

import java.util.Date;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 2/18/13 Time: 4:52 PM
 *         <p/>
 *         <p>This payload is client-facing only</p>
 */
public class BlahInfoPayload extends BasePayload implements UserBlahInfoDAOConstants {


    public BlahInfoPayload() {
        super();
    }

    public BlahInfoPayload(String id) {
        super(id);
    }

    public BlahInfoPayload(Map<String, Object> map) {
        super(map);
        ensureDateTime(POLL_VOTE_TIMESTAMP);
    }

    public BlahInfoPayload(String userId, String blahId) {
        setUserId(userId);
        setBlahId(blahId);
    }

    public String getBlahId() {
        return (String) get(BLAH_ID);
    }

    public void setBlahId(String blahId) {
        put(BLAH_ID, blahId);
    }

    public String getUserId() {
        return (String) get(USER_ID);
    }

    public void setUserId(String userId) {
        put(USER_ID, userId);
    }

    public Integer getPollVoteIndex() {
        return (Integer) get(POLL_VOTE_INDEX);
    }

    public void setPollVoteIndex(Integer pollVoteIndex) {
        put(POLL_VOTE_INDEX, pollVoteIndex);
    }

    public Date getPollVoteTimestamp() {
        return (Date) get(POLL_VOTE_TIMESTAMP);
    }

    public void setPollVoteTimestamp(Date pollVoteTimestamp) {
        put(POLL_VOTE_TIMESTAMP, pollVoteTimestamp);
    }
}
