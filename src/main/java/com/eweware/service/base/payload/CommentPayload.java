package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.CommentDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.BlahSchema;
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

    public Integer getBlahVote() {
        return (Integer) get(BLAH_VOTE);
    }

    public void setBlahVote(Integer vote) {
        put(BLAH_VOTE, vote);
    }

    public Integer getCommentVotes() {
        return (Integer) get(COMMENT_VOTES);
    }

    public void setCommentVotes(Integer votes) {
        put(COMMENT_VOTES, votes);
    }

    public Integer getCommentUpVotes() {
        return (Integer) get(COMMENT_UP_VOTES);
    }

    public void setCommentUpVotes(Integer votes) {
        put(COMMENT_UP_VOTES, votes);
    }

    public Integer getCommentDownVotes() {
        return (Integer) get(COMMENT_DOWN_VOTES);
    }

    public void setCommentDownVotes(Integer votes) {
        put(COMMENT_DOWN_VOTES, votes);
    }

    public Integer getViews() {
       return (Integer) get(VIEWS);
    }

    public void setViews(Integer views) {
        put(VIEWS, views);
    }

    public Integer getOpens() {
        return (Integer) get(OPENS);
    }

    public void setOpens(Integer opens) {
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

    public Integer getUserVote() {
        return (Integer) get(USER_VOTE);
    }

    public void setUserVote(Integer vote) {
        put(USER_VOTE, vote);
    }

    public Integer getUserViews() {
        return (Integer) get(USER_VIEWS);
    }

    public void setUserViews(Integer views) {
        put(USER_VIEWS, views);
    }

    public Integer getUserOpens() {
        return (Integer) get(USER_OPENS);
    }

    public void setUserOpens(Integer opens) {
        put(USER_OPENS, opens);
    }

    public String getUserNickname() {
        return (String) get(USER_NICKNAME);
    }

    public void setUserNickname(String nickname) {
        put(USER_NICKNAME, nickname);
    }
}