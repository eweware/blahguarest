package main.java.com.eweware.service.base.store.dao;

import java.util.List;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/9/12 Time: 4:08 PM
 */
public interface InboxBlahDAO extends BaseDAO, InboxBlahDAOConstants {

    public String getBlahId();

    public void setBlahId(String blahId);

    public String getAuthorId();

    public void setAuthorId(String authorId);

    public String getType();

    public void setType(String type);

    public String getGroupId();

    public void setGroupId(String groupId);

    public String getBlahText();

    public void setBlahText(String text);

    public List<String> getImageIds();

    public void setImageIds(List<String> imageIds);

    public Integer getInboxNumber();

    public void setInboxNumber(Integer sequenceNumber);

    public Double getStrength();

    public void setStrength(Double strength);

    public Double getRecentStrength();

    public void setRecentStrength(Double strength);

    public Integer getUpVotes();

    public void setUpVotes(Integer upVotes);

    public Integer getDownVotes();

    public void setDownVotes(Integer downVotes);

    public Integer getOpens();

    public void setOpens(Integer opens);

    public Integer getViews();

    public void setViews(Integer views);
}
