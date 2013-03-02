package main.java.com.eweware.service.base.store.dao.schema.type;

/**
 * <p>Specifies a permission on a ProfileDAO datum (e.g., date of birth).</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 2/28/13 Time: 10:56 PM
 */
public enum UserProfilePermissions {

    /**
     * Only user can see this profile datum
     */
    PRIVATE(0),

    /**
     * Only users who are logged in can see this profile datum
     */
    MEMBERS(1),

    /**
     * Anyone (including anonymous users) can see this profile datum
     */
    PUBLIC(2);

    private final int code;

    UserProfilePermissions(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
