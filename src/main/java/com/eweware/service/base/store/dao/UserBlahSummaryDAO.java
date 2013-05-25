package main.java.com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/12/12 Time: 2:40 PM
 */
public interface UserBlahSummaryDAO extends BaseDAO, UserBlahSummaryDAOConstants {

    /**
     * @return Long Returns the number of unique persons who viewed this blah.
     */
    public Long getPersonViewCount();

    /**
     * Sets the number of unique persons who have viewed this blah.
     * @param count  Number of unique persons who have viewed this blah.
     */
    public void setPersonViewCount(Long count);

    /**
     * @return Long Returns the number of unique persons who
     * have opened this blah.
     */
    public Long getPersonOpenCount();

    /**
     * Sets the number of unique persons who have opened this blah.
     * @param count  Number of unique persons who have opened this blah
     */
    public void setPersonOpenCount(Long count);

    /**
     * @return Long  Returns the number of unique persons
     * who have commented on this blah.
     */
    public Long getPersonCommented();

    /**
     * Sets the number of unique persons who have commented on this blah.
     * @param comments Number of persons who have commented on this blah.
     */
    public void setPersonCommented(Long comments);

}
