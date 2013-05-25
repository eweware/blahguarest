package main.java.com.eweware.service.base.payload;

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
        return (Long) get(BLAH_VOTE);
    }

    public void setBlahVote(Long vote) {
        put(BLAH_VOTE, vote);
    }

    public Long getCommentVotes() {
        return (Long) get(COMMENT_VOTES);
    }

    public void setCommentVotes(Long votes) {
        put(COMMENT_VOTES, votes);
    }

    public Long getCommentUpVotes() {
        return (Long) get(COMMENT_UP_VOTES);
    }

    public void setCommentUpVotes(Long votes) {
        put(COMMENT_UP_VOTES, votes);
    }

    public Long getCommentDownVotes() {
        return (Long) get(COMMENT_DOWN_VOTES);
    }

    public void setCommentDownVotes(Long votes) {
        put(COMMENT_DOWN_VOTES, votes);
    }

    public Long getViews() {
       return (Long) get(VIEWS);
    }

    public void setViews(Long views) {
        put(VIEWS, views);
    }

    public Long getOpens() {
        return (Long) get(OPENS);
    }

    public void setOpens(Long opens) {
        put(OPENS, opens);
    }

    public Double getStrength() {
        return (Double) get(COMMENT_STRENGTH);
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
        return (Long) get(USER_VOTE);
    }

    public void setUserVote(Long vote) {
        put(USER_VOTE, vote);
    }

    public Long getUserViews() {
        return (Long) get(USER_VIEWS);
    }

    public void setUserViews(Long views) {
        put(USER_VIEWS, views);
    }

    public Long getUserOpens() {
        return (Long) get(USER_OPENS);
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
}