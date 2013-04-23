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
    PRIVATE(new Integer(0)),

    /**
     * Only users who are logged in can see this profile datum
     */
    MEMBERS(new Integer(1)),

    /**
     * Anyone (including anonymous users) can see this profile datum
     */
    PUBLIC(new Integer(2));

    private final Integer code;

    UserProfilePermissions(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
