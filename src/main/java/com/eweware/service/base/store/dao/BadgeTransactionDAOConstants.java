package main.java.com.eweware.service.base.store.dao;

/**
 * @author rk@post.harvard.edu
 *         Date: 3/22/13 Time: 4:41 PM
 */
public interface BadgeTransactionDAOConstants {

    /**
     * Uniquely identifies
     * the transaction between this server and
     * the badging authority. It consists of
     * the authority id suffixed by the transaction token
     * received from the authority. The authority id prevents
     * a namespace clash in ids.
     */
    static final String ID = "_id";

    /**
     * The id of the authority granting the badge
     * in this transaction.
     */
    static final String AUTHORITY_ID = "A";

    /**
     * <p>The display name of the authority</p>
     */
    static final String AUTHORITY_DISPLAY_NAME = "D";

    /**
     * The transaction's current state.
     * @see main.java.com.eweware.service.base.store.dao.type.BadgeTransactionState
     */
    static final String STATE = "S";

    /**
     * The id of the user attempting to obtain the badge.
     */
    static final String USER_ID = "U";

    /**
     * Time transaction started.
     */
    static final String CREATED = "C";

}
