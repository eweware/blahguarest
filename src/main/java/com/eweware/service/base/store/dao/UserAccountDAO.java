package main.java.com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 2/26/13 Time: 4:27 PM
 */
public interface UserAccountDAO extends BaseDAO, UserAccountDAOConstants {

    public String getCanonicalUsername();

    public void setCanonicalUsername(String canonicalUsername);

    public String getDigest();

    public void setDigest(String digest);

    public String getSalt();

    public void setSalt(String salt);
}
