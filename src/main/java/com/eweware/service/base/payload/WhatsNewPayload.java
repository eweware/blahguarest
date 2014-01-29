package com.eweware.service.base.payload;

/**
 * Created by Dave on 1/27/14.
 */

import com.eweware.service.base.CommonUtilities;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.store.dao.WhatsNewDAO;
import com.eweware.service.base.store.dao.WhatsNewDAOConstants;
import com.eweware.service.base.store.dao.schema.BaseSchema;
import com.eweware.service.base.store.dao.schema.WhatsNewSchema;

import java.util.Map;


public final class WhatsNewPayload extends BasePayload implements WhatsNewDAOConstants {

    protected static final BaseSchema getSchema() {
        return WhatsNewSchema.getSchema(LocaleId.en_us);
    }

    public WhatsNewPayload() {
        super();
    }

    public WhatsNewPayload(String id) {
        super(id);
    }

    public WhatsNewPayload(Map<String, Object> map) {
        super(map);
    }

    public String getMessage() {
        return (String) get(MESSAGE);
    }

    public void setMessage(String theMessage) {
        put(MESSAGE, theMessage);
    }

    public String getTargetUser() {
        return (String) get(TARGET_USER);
    }

    public void setTargetUser(String theMessage) {
        put(TARGET_USER, theMessage);
    }

    public Integer getNewComments() {
        return (Integer) get(NEW_COMMENTS);
    }

    public void setNewComments(Integer theMessage) {
        put(NEW_COMMENTS, theMessage);
    }

    public Integer getNewOpens() {
        return (Integer) get(NEW_OPENS);
    }

    public void setNewOpens(Integer theMessage) {
        put(NEW_OPENS, theMessage);
    }

    public Integer getNewUpVotes() {
        return (Integer) get(NEW_UP_VOTES);
    }

    public void setNewUpVotes(Integer theMessage) {
        put(NEW_UP_VOTES, theMessage);
    }

    public Integer getNewDownVotes() {
        return (Integer) get(NEW_DOWN_VOTES);
    }

    public void setNewDownVotes(Integer theMessage) {
        put(NEW_DOWN_VOTES, theMessage);
    }

    public Integer getNewMessages() {
        return (Integer) get(NEW_MESSAGES);
    }

    public void setNewMessages(Integer theMessage) {
        put(NEW_MESSAGES, theMessage);
    }

}
