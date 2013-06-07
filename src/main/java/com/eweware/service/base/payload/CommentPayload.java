package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.CommonUtilities;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.CommentDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.CommentSchema;

import java.util.List;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 */
public final class CommentPayload extends BasePayload implements CommentDAOConstants {

    protected static final BaseSchema getSchema() {
        return CommentSchema.getSchema(LocaleId.en_us);
    }

    /** Fields not in corresponding DAO **/
    static final String USER_VOTE = "uv";
    static final String USER_VIEWS = "uw";
    static final String USER_OPENS = "uo";
    static final String USER_NICKNAME = "K";

    public CommentPayload() {
        super();
    }

    public CommentPayload(String id) {
        super(id);
    }

    public CommentPayload(Map<String, Object> map) {
        super(map);
    }

    public String getBlahId() {
        return (String) get(BLAH_ID);
    }

    public void setBlahId(String id) {
        put(BLAH_ID, id);
    }

    public String getText() {
        return (String) get(TEXT);
    }

    public void setText(String text) {
        put(TEXT, text);
    }

    public String getAuthorId() {
        return (String) get(AUTHOR_ID);
    }

    public void setAuthorId(String authorId) {
        put(AUTHOR_ID, authorId);
    }

    public Long getBlahVote() {
        return CommonUtilities.getValueAsLong(get(BLAH_VOTE), null);
    }

    public void setBlahVote(Long vote) {
        put(BLAH_VOTE, vote);
    }

    public Long getCommentVotes() {
        return CommonUtilities.getValueAsLong(get(COMMENT_VOTES), null);
    }

    public void setCommentVotes(Long votes) {
        put(COMMENT_VOTES, votes);
    }

    public Long getCommentUpVotes() {
        return CommonUtilities.getValueAsLong(get(COMMENT_UP_VOTES), null);
    }

    public void setCommentUpVotes(Long votes) {
        put(COMMENT_UP_VOTES, votes);
    }

    public Long getCommentDownVotes() {
        return CommonUtilities.getValueAsLong(get(COMMENT_DOWN_VOTES), null);
    }

    public void setCommentDownVotes(Long votes) {
        put(COMMENT_DOWN_VOTES, votes);
    }

    public Long getViews() {
       return CommonUtilities.getValueAsLong(get(VIEWS), null);
    }

    public void setViews(Long views) {
        put(VIEWS, views);
    }

    public Long getOpens() {
        return CommonUtilities.getValueAsLong(get(OPENS), null);
    }

    public void setOpens(Long opens) {
        put(OPENS, opens);
    }

    public Double getStrength() {
        return CommonUtilities.getValueAsDouble(get(COMMENT_STRENGTH), null);
    }

    public void setStrength(Double strength) {
        put(COMMENT_STRENGTH, strength);
    }

    public List<CommentTrackerPayload> getStats() {
        return (List<CommentTrackerPayload>) get(STATS);
    }

    public void setStats(List<CommentTrackerPayload> stats) {
        put(STATS, stats);
    }

    public Long getUserVote() {
        return CommonUtilities.getValueAsLong(get(USER_VOTE), null);
    }

    public void setUserVote(Long vote) {
        put(USER_VOTE, vote);
    }

    public Long getUserViews() {
        return CommonUtilities.getValueAsLong(get(USER_VIEWS), null);
    }

    public void setUserViews(Long views) {
        put(USER_VIEWS, views);
    }

    public Long getUserOpens() {
        return CommonUtilities.getValueAsLong(get(USER_OPENS), null);
    }

    public void setUserOpens(Long opens) {
        put(USER_OPENS, opens);
    }

    public String getUserNickname() {
        return (String) get(USER_NICKNAME);
    }

    public void setUserNickname(String nickname) {
        put(USER_NICKNAME, nickname);
    }

    public List<String> getImageIds() {
        return (List<String>) get(IMAGE_IDS);
    }

    public void setImageIds(List<String> imageIds) {
        put(IMAGE_IDS, imageIds);
    }
}