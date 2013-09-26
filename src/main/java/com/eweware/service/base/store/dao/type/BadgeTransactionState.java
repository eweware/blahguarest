package com.eweware.service.base.store.dao.type;

/**
 * <p>Represents the possible states of a BadgeDAO</p>
 * @see com.eweware.service.base.store.dao.BadgeDAO
 *
 * @author rk@post.harvard.edu
 *         Date: 3/22/13 Time: 3:04 PM
 */
public enum BadgeTransactionState {

    /**
     * The badge is pending verification.
     */
    PENDING("p"),

    /**
     * The badge has been granted by an authority.
     */
    GRANTED("g"),

    /**
     * The badge's fulfillment has been refused by an authority.
     */
    REFUSED("r"),

    /**
     * Did not go through due to an error on the badge authority server.
     */
    SERVER_ERROR("s"),

    /**
     * The badge's fulfillment was cancelled by the user.
     */
    CANCELLED("c"),

    /**
     * The badge was granted by the authority, but the
     * userId in the transaction did not correspond to a user
     * in the blahgua DB. (Highly unlikely, but so.)
     */
    GRANTED_BUT_NO_USER_ID("u");

    private final String code;

    BadgeTransactionState(String code) {
        this.code = code;

    }

    /**
     * <p>Returns the state's code value (e.g., as stored in the DB)</p>
     * @return  The state's code
     */
    public String getCode() {
        return code;
    }
}
