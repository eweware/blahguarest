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
     * <p>User account type.</p>
     * @see main.java.com.eweware.service.base.store.dao.type.UserAccountType
     */
    static String USER_ACCOUNT_TYPE = "t";

    /**
     * <p> The canonical version of the username. A string.</p>
     * @see main.java.com.eweware.service.user.validation.Login#makeCanonicalUsername(String)
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

    /**
     * <p>Optional user email address</p>
     */
    static String EMAIL_ADDRESS = "e";

    /**
     * <p>Permissions associated with the email address field.</p>
     * @see main.java.com.eweware.service.base.store.dao.schema.type.UserProfilePermissions
     */
    static final String EMAIL_ADDRESS_PERMISSIONS = "ep";

    /**
     * <p>Account recovery method.</p>
     * @see main.java.com.eweware.service.base.store.dao.type.RecoveryMethodType
     */
    static final String ACCOUNT_RECOVERY_METHOD = "m";

    /**
     * <p>String representation of recovery code structured as follows:</p>
     * <div>cipher-part-base64 | iv-algorithm-parameter-part-base64 | secret-key-part-base64</div>
     * @see main.java.com.eweware.service.user.validation.RecoveryCode
     * @see main.java.com.eweware.service.user.validation.RecoveryCodeComponents
     */
    static String RECOVERY_CODE_STRING = "r";


    /**
     * <p>The date after which the currently set recovery code expires. A Date.</p>
     */
    static final String RECOVERY_CODE_EXPIRATION_DATE = "x";

    /**
     * <p>A challenge security answer from the user. We just have one for now. A string.</p>
     */
    static final String CHALLENGE_ANSWER_1 = "a";
}


//    /**
//     * <p>Recovery cipher encrypting user id and other data.</p>
//     * <p>Used in account recovery. A Base64 string.</p>
//     */
//    static String RECOVERY_CIPHER_BASE64 = "c";
//
//    /**
//     * <p>Recovery cipher IV algorithm parameter. A Base64 string.</p>
//     */
//    static String RECOVERY_IV_PARAM_BASE64 = "i";
//
//    /**
//     * <p>Recovery cipher secret key. A Base64 string.</p>
//     */
//    static String RECOVERY_SECRET_KEY_BASE64 = "s";