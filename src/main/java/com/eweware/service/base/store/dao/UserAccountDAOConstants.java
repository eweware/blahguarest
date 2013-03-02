package main.java.com.eweware.service.base.store.dao;

/**
 * <p>A user account entity holds sensitive data about a user. This information
 * is segregated from the user entity so that it won't be easily exposed to the REST APIs.</p>
 * <p>The id key of this record is the same as the user's id.</p>
 * <p>This might eventually require further isolation in a separate DB.</p>
 * @author rk@post.harvard.edu
 *         Date: 2/26/13 Time: 4:25 PM
 */
public interface UserAccountDAOConstants {

    /**
     * <p> The canonical version of the username. A string.</p>
     * @see main.java.com.eweware.service.mgr.UserManager#makeCanonicalUsername(String)
     */
    static String CANONICAL_USERNAME = "u";

    /**
     * <p>The password's digest code. A Base64 string.</p>
     */
    static String PASSWORD_DIGEST = "d";

    /**
     *  <p>The password's salt. A Base64 string.</p>
     */
    static String PASSWORD_SALT = "p";
}
