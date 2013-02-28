package main.java.com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 2/26/13 Time: 4:25 PM
 */
public interface UserAccountDAOConstants {

    /**
     * The _id of this record is the same
     * as the UserDAO's id.
     */

    /**
     * The canonical version of the username
     * @see main.java.com.eweware.service.mgr.UserManager#makeCanonicalUsername(String)
     */
    static String CANONICAL_USERNAME = "u";

    /**
     * Password digest
     */
    static String PASSWORD_DIGEST = "d";

    /**
     * Salt for password
     */
    static String PASSWORD_SALT = "p";
}
