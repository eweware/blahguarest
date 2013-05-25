package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.CommonUtilities;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.store.dao.InboxBlahDAOConstants;
import main.java.com.eweware.service.base.store.dao.schema.BaseSchema;
import main.java.com.eweware.service.base.store.dao.schema.InboxBlahSchema;

import java.util.Map;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/9/12 Time: 4:17 PM
 */
public class InboxBlahPayload extends BasePayload implements InboxBlahDAOConstants {

    protected static final BaseSchema getSchema() {
        return InboxBlahSchema.getSchema(LocaleId.en_us);
    }

    public InboxBlahPayload() {
        super();
    }

    public InboxBlahPayload(String id) {
        super(id);
    }

    public InboxBlahPayload(Map<String, Object> map) {
        super(map);
    }

    public String getBlahId() {
        return (String) get(BLAH_ID);
    }

    public void setBlahId(String blahId) {
        put(BLAH_ID, blahId);
    }

    public String getAuthorId() {
        return (String) get(AUTHOR_ID);
    }

    public void setAuthorId(String authorId) {
        put(AUTHOR_ID, authorId);
    }

    public String getBlahText() {
        return (String) get(BLAH_TEXT);
    }

    public void setBlahText(String text) {
        put(BLAH_TEXT, text);
    }

    public String getGroupId() {
        return (String) get(GROUP_ID);
    }

    public void setGroupId(String text) {
        put(GROUP_ID, text);
    }

    public Integer getInboxNumber() {
        return (Integer) get(INBOX_NUMBER);
    }

    public void setInboxNumber(Integer inboxNumber) {
        put(INBOX_NUMBER, inboxNumber);
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

    public Long getUpVotes() {
        return CommonUtilities.getValueAsLong(get(UP_VOTES), null);
    }

    public void setUpVotes(Long upVotes) {
        put(UP_VOTES, upVotes);
    }

    public Long getDownVotes() {
        return CommonUtilities.getValueAsLong(get(DOWN_VOTES), null);
    }

    public void setDownVotes(Long downVotes) {
        put(DOWN_VOTES, downVotes);
    }

    public Long getOpens() {
        return CommonUtilities.getValueAsLong(get(OPENS), null);
    }

    public void setOpens(Long opens) {
        put(OPENS, opens);
    }

    public Long getViews() {
        return CommonUtilities.getValueAsLong(get(VIEWS), null);
    }

    public void setViews(Long views) {
        put(VIEWS, views);
    }

    public String getBadgeIndicator() {
        return (String) get(BADGE_INDICATOR);
    }

    public void setBadgeIndicator(String indicator) {
        put(BADGE_INDICATOR, indicator);
    }

    public String getAuthorNickname() {
        return (String) get(AUTHOR_NICKNAME);
    }

    public void setAuthorNickname(String nickname) {
        put(AUTHOR_NICKNAME, nickname);
    }
}
