package main.java.com.eweware.service.base.payload;

import main.java.com.eweware.service.base.error.InvalidAuthorizedStateException;

/**
 * @author rk@post.harvard.edu
 *         <p/>
 *         Used for validation only as enums don't automatically (de)serialize.
 *         Keeping it simple by not adding custom serialization apparatus.
 */
public enum AuthorizedState {
    P("joined"), A("activated"), S("suspended"), D("deleted");
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

    public static String checkState(String stateAsString) throws InvalidAuthorizedStateException {
        try {
            AuthorizedState.valueOf(stateAsString);
        } catch (IllegalArgumentException e) {

        }
        try {
            return (AuthorizedState.valueOf(stateAsString) != null) ? stateAsString : null;
        } catch (Exception e) {
            throw new InvalidAuthorizedStateException("invalid state=" + stateAsString, e);
        }
    }
}