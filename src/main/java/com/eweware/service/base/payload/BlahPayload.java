package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.store.dao.BlahDAOConstants;

import java.util.List;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 */
public final class BlahPayload extends BasePayload implements BlahDAOConstants {

    static final String ORIGINAL_GROUP_ID = "groupId";
    static final String USER_VOTE = "uv";
    static final String USER_VIEWS = "uw";
    static final String USER_OPENS = "uo";


    public BlahPayload() {
        super();
    }

    public BlahPayload(String id) {
        super(id);
    }

    public BlahPayload(Map<String, Object> map) {
        super(map);
    }

    public String getTypeId() {
        return (String) get(TYPE_ID);
    }

    public void setTypeId(String type) {
        put(TYPE_ID, type);
    }

    public String getText() {
        return (String) get(TEXT);
    }

    public void setText(String text) {
        put(TEXT, text);
    }

    public String getBody() {
        return (String) get(BODY);
    }

    public void setBody(String body) {
        put(BODY, body);
    }
    public String getAuthorId() {
        return (String) get(AUTHOR_ID);
    }

    public void setAuthorId(String authorId) {
        put(AUTHOR_ID, authorId);
    }

    public Integer getVotes() {
        return (Integer) get(VOTES);
    }

    public void setVotes(Integer votes) {
        put(VOTES, votes);
    }

    public Integer getUpVotes() {
        return (Integer) get(UP_VOTES);
    }

    public void setUpVotes(Integer votes) {
        put(UP_VOTES, votes);
    }

    public Integer getDownVotes() {
        return (Integer) get(DOWN_VOTES);
    }

    public void setDownVotes(Integer votes) {
        put(DOWN_VOTES, votes);
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

    public Integer getComments() {
        return (Integer) get(COMMENTS);
    }

    public void setComments(Integer comments) {

        put(COMMENTS, comments);
    }

    public String getGroupId() {
        return (String) get(ORIGINAL_GROUP_ID);
    }

    public void setGroupId(String groupId) {
        put(ORIGINAL_GROUP_ID, groupId);
    }
    public Integer getUserVote() {
        return (Integer) get(USER_VOTE);
    }

    public void setUserVote(Integer userVote) {
        put(USER_VOTE, userVote);
    }

    public Integer getUserViews() {
        return (Integer) get(USER_VIEWS);
    }

    public void setUserViews(Integer userViews) {
        put(USER_VIEWS, userViews);
    }

    public Integer getUserOpens() {
        return (Integer) get(USER_OPENS);
    }

    public void setUserOpens(Integer userOpens) {
        put(USER_OPENS, userOpens);
    }

    public Double getStrength() {
        return (Double) get(BLAH_STRENGTH);
    }

    public void setStrength(Double strength) {
        put(BLAH_STRENGTH, strength);
    }

    public Double getRecentStrength() {
        return (Double) get(RECENT_BLAH_STRENGTH);
    }

    public void setRecentStrength(Double strength) {
        put(RECENT_BLAH_STRENGTH, strength);
    }

    public List<BlahTrackerPayload> getStats() {
        return (List<BlahTrackerPayload>) get(STATS);
    }

    public void setStats(List<BlahTrackerPayload> stats) {
        put(STATS, stats);
    }
}
