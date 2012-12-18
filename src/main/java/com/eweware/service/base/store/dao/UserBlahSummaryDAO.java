package main.java.com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/12/12 Time: 2:40 PM
 */
public interface UserBlahSummaryDAO extends BaseDAO, UserBlahSummaryDAOConstants {

    /**
     * @return Integer Returns the number of unique persons who viewed this blah.
     */
    public Integer getPersonViewCount();

    /**
     * Sets the number of unique persons who have viewed this blah.
     * @param count  Number of unique persons who have viewed this blah.
     */
    public void setPersonViewCount(Integer count);

    /**
     * @return Integer Returns the number of unique persons who
     * have opened this blah.
     */
    public Integer getPersonOpenCount();

    /**
     * Sets the number of unique persons who have opened this blah.
     * @param count  Number of unique persons who have opened this blah
     */
    public void setPersonOpenCount(Integer count);

    /**
     * @return Integer  Returns the number of unique persons
     * who have commented on this blah.
     */
    public Integer getPersonCommented();

    /**
     * Sets the number of unique persons who have commented on this blah.
     * @param comments Number of persons who have commented on this blah.
     */
    public void setPersonCommented(Integer comments);

}
