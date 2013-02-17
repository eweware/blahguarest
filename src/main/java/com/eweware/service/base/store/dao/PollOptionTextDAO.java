package main.java.com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 2/15/13 Time: 5:47 PM
 */
public interface PollOptionTextDAO extends BaseDAO, PollOptionDAOConstants {

    /**
     * Returns the tag line for this poll item. This should
     * be a very short piece of text (about 32 chars at most)
     * @return  The tag line
     */
    public String getTagLine();

    /**
     * Set the tagline for this option.
     * @param tagLine   The tagline (preferably less than 32 characters)
     */
    public void setTagLine(String tagLine);

    /**
     * Returns the text of the poll option.
     * @return  The poll option text
     */
    public String getText();

    /**
     * Sets the text of the poll option
     * @param text The text of the poll option
     */
    public void setText(String text);
}
