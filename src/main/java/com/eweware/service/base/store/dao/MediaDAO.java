package main.java.com.eweware.service.base.store.dao;


/**
 * @author rk@post.harvard.edu
 *         Date: 12/22/12 Time: 7:25 PM
 */
public interface MediaDAO extends BaseDAO, MediaDAOConstants {

    /**
     * @return String   Returns the type of media (e.g., jpg, mov)
     */
    public String getType();

    /**
     * Sets the media type
     * @param type Media type (a valid file extension)
     */
    public void setType(String type);
}
