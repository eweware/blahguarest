package com.eweware.service.base.store.dao;

import com.eweware.service.base.error.SystemErrorException;

/**
 * Created by Dave on 1/25/14.
 */
public interface WhatsNewDAO extends BaseDAO, WhatsNewDAOConstants {

    public String getMessage();
    public void setMessage(String theMessage);

    public String getTargetUser();
    public void setTargetUser(String userId);

    public Integer getNewComments();
    public void setNewComments(Integer newCount);

    public Integer getNewOpens();
    public void setNewOpens(Integer newCount);

    public Integer getNewUpVotes();
    public void setNewUpVotes(Integer newCount);

    public Integer getNewDownVotes();
    public void setNewDownVotes(Integer newCount);

    public Integer getNewMessages();
    public void setNewMessages(Integer newCount);

    public WhatsNewDAO _findNewestInfoByTargetID(String userId) throws SystemErrorException;


}
