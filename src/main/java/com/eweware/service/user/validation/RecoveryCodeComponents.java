package com.eweware.service.user.validation;

/**
 * @author rk@post.harvard.edu
 *         Date: 3/3/13 Time: 1:10 PM
 */
public class RecoveryCodeComponents {

    private String userId;
    private String canonicalUsername;

    public RecoveryCodeComponents(String userId, String canonicalUsername) {
        this.userId = userId;
        this.canonicalUsername = canonicalUsername;
    }

    public String getCanonicalUsername() {
        return canonicalUsername;
    }

    public String getUserId() {
        return userId;
    }

}
