package main.java.com.eweware.service.base.store.dao.type;

/**
 * <p>User account types used in UserAccountDAO.</p>
 * @author rk@post.harvard.edu
 *         Date: 3/3/13 Time: 5:06 PM
 */
public enum UserAccountType {

    STANDARD("s"), /** standard user account */
    ADMIN("a"); /** admin user account has all privileges */

    public String getCode() {
        return code;
    }

    private final String code;

    UserAccountType(String code) {
        this.code = code;
    }
}
