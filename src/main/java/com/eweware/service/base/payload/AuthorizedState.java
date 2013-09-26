package com.eweware.service.base.payload;

/**
 * @author rk@post.harvard.edu
 *         <p/>
 *         Used for validation only as enums don't automatically (de)serialize.
 *         Keeping it simple by not adding custom serialization apparatus.
 */
public enum AuthorizedState {

    P("pending"), A("activated"), S("suspended"), D("deleted"),;

    private String description;

    private AuthorizedState(String actionCode) {
        this.description = actionCode;
    }

    public String getDescription() {
        return description;
    }

    public static final boolean validStateP(String state) {
        return (valueOf(state) != null);
    }

    public static AuthorizedState getDefault() {
        return P;
    }

    public static String getDefaultState() {
        return getDefault().toString();
    }

    public static AuthorizedState find(String stateAsString) {
        for (AuthorizedState s : AuthorizedState.values()) {
            if (s.toString().equals(stateAsString)) {
                return s;
            }
        }
        return null;
    }
}