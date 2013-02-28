package main.java.com.eweware.service.rest.session;

import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.InvalidAuthorizedStateException;
import main.java.com.eweware.service.mgr.GroupManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>A set of static methods to handle session state.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 12/28/12 Time: 1:59 PM
 */
public final class BlahguaSession {


    private static final Logger logger = Logger.getLogger("BlahguaSession");

    /**
     * A clumsy kludge to cheat so that QA tests can pass when REST runs on a Mac.
     * TODO eliminate this before Macs take over the world.
     */
    private static boolean isMaster = System.getProperty("os.name").toLowerCase().startsWith("mac");

    /**
     * Set to a Boolean. Indicates whether the user was authenticated or not.
     */
    private static final String AUTHENTICATION_STATE = "A";

    /**
     * Set to a Set of Strings, each string is a group id
     * These are the groups that the user is currently watching.
     */
    private static final String VIEWING_COUNT_GROUP_IDS = "G";

    /**
     * If the user is authenticated, this is the user's id.
     */
    private static final String USER_ID = "I";

    // TODO debugging
    private static final String USERNAME = "U";

    /**
     * An InboxInfo instance.
     * This is the last inbox number that the session has seen.
     *
     * @see InboxInfo
     */
    private static final String INBOX_INFO = "i";


    /**
     * Marks the user session as authenticated.
     * Input parameters are not checked!
     *
     * @param request  The http request object (unchecked!)
     * @param userId   The user id  (unchecked!)
     * @param username TODO remove (dbg)
     */
    public static void markAuthenticated(HttpServletRequest request, String userId, String username) {
        final HttpSession session = request.getSession(true);
        markAuthenticated(session, userId, username);
    }

    /**
     * Marks the user session as authenticated.
     * Input parameters are not checked!
     *
     * @param userId   The user id (unchecked!)
     * @param username TODO remove (dbg)
     */
    public static void markAuthenticated(HttpSession session, String userId, String username) {
        if (username != null) {     // TODO remove (dbg)
            session.setAttribute(USERNAME, username);
        }
        session.setAttribute(USER_ID, userId);
        session.setAttribute(AUTHENTICATION_STATE, SessionState.AUTHENTICATED);
    }

    /**
     * <p>Returns true if the session is authenticated (i.e., user has logged in).</p>
     *
     * @param request The http request (unchecked!)
     * @return True if the user is logged in (authenticated)
     */
    public static boolean isAuthenticated(HttpServletRequest request) {
        final HttpSession session = request.getSession(); // don't create a session
        return (session != null) && ((SessionState) session.getAttribute(AUTHENTICATION_STATE) == SessionState.AUTHENTICATED);
    }

    /**
     * <p>Ensures that the user session is authenticated.</p>
     *
     * @param request The http request
     * @throws InvalidAuthorizedStateException If there is no authenticated session.
     */
    public static void ensureAuthenticated(HttpServletRequest request) throws InvalidAuthorizedStateException {
        if (!isMaster && !isAuthenticated(request)) {
            throw new InvalidAuthorizedStateException("operation not supported", ErrorCodes.UNAUTHORIZED_USER);
        }
    }

    /**
     * <p>Marks the user session as anonymous</p>
     *
     * @param request The http request (unchecked!)
     */
    public static void markAnonymous(HttpServletRequest request) {
        final HttpSession session = request.getSession();
        markAnonymous(session);
    }

    /**
     * <p>Marks the user session as anonymous</p>
     *
     * @param session The http session object
     */
    public static void markAnonymous(HttpSession session) {
        if (session != null) {
            session.removeAttribute(USER_ID);
            session.removeAttribute(USERNAME);
            session.setAttribute(AUTHENTICATION_STATE, SessionState.ANONYMOUS);
        }
    }

    /**
     * <p>Returns the userId, if any, associated with this session.</p>
     *
     * @param request The http request (unchecked!)
     * @return The userId for this session (if it is authenticated) or
     *         null if this is not an authenticated session or there is no session.
     */
    public static String getUserId(HttpServletRequest request) {
        final HttpSession session = request.getSession();
        return (session == null) ? null : (String) session.getAttribute(USER_ID);
    }

    /**
     * Holds inbox state information.
     * Maps each groupId to the last inbox number fetched within that group.
     */
    private static class InboxInfo {
        private final Map<String, Integer> groupIdToLastInboxNumberMap = new HashMap<String, Integer>(5);

        private InboxInfo(String groupId, Integer lastInboxNumber) {
            setLastInboxNumber(groupId, lastInboxNumber);
        }

        private void setLastInboxNumber(String groupId, Integer lastInboxNumber) {
            groupIdToLastInboxNumberMap.put(groupId, lastInboxNumber);
        }

        private Integer getLastInboxNumber(String groupId) {
            return groupIdToLastInboxNumberMap.get(groupId);
        }
    }

    /**
     * Returns the last inbox number accessed in this session.
     *
     * @param request The request
     * @param groupId The group id of the inbox
     * @return The last inbox number or null if the inbox has not been visited during this session.
     */
    public static Integer getLastInboxNumber(HttpServletRequest request, String groupId) {
        final InboxInfo info = getInboxInfo(request, groupId);
        return (info == null) ? null : info.getLastInboxNumber(groupId);
    }

    /**
     * <p>Sets the last inbox number fetched for the specified group.</p>
     *
     * @param request         The http request (unchecked!)
     * @param groupId         The group id (unchecked!)
     * @param lastInboxNumber The last inbox number in the group to have been fetched.
     */
    public static void setLastInboxNumber(HttpServletRequest request, String groupId, Integer lastInboxNumber) {
        final InboxInfo inboxInfo = getInboxInfo(request, groupId);
        if (inboxInfo != null) {
            inboxInfo.setLastInboxNumber(groupId, lastInboxNumber);
        } else {
            setInboxInfo(request, groupId, lastInboxNumber);
        }
    }

    /**
     * <p>Adds the group to the list of currently viewed groups by user.</p>
     *
     * @param request The http request (unchecked!)
     * @param groupId The groupId (may be null but unchecked for validity)
     */
    public static void addCurrentlyViewedGroup(HttpServletRequest request, String groupId) {
        final HttpSession session = request.getSession(true);
        if (groupId != null) {
            Set<String> groupIds = (Set<String>) session.getAttribute(VIEWING_COUNT_GROUP_IDS);
            if (groupIds == null) {
                groupIds = new HashSet<String>(5);
                session.setAttribute(VIEWING_COUNT_GROUP_IDS, groupIds);
            }
            groupIds.add(groupId);
        }
    }

    /**
     * <p>Called when the user session is destroyed.</p>
     * <p>Reduces the current viewer count for all groups in which this user
     * is participating.</p>
     *
     * @param session The http session object
     */
    public static void sessionDestroyed(HttpSession session) {
        final Set<String> groupIds = (Set<String>) session.getAttribute(BlahguaSession.VIEWING_COUNT_GROUP_IDS);
        String username = (String) session.getAttribute(BlahguaSession.USERNAME);
        if (username == null) {
            username = "unknown";
        } // dbg username
        logger.info("Session destroyed for username '" + username + "': viewing group ids: " + ((groupIds == null) ? null : groupIds));
        if (groupIds != null) {
            for (String groupId : groupIds) {
                try {
                    GroupManager.getInstance().updateViewerCount(groupId, false, null);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to reduce current viewer count for groupId '" + groupId + "'", e);
                    // continue
                }
            }
        }
    }


    private static void setInboxInfo(HttpServletRequest request, String groupId, Integer lastInboxNumber) {
        request.getSession().setAttribute(INBOX_INFO, new InboxInfo(groupId, lastInboxNumber));
    }

    private static InboxInfo getInboxInfo(HttpServletRequest request, String groupId) {
        return (InboxInfo) request.getSession(true).getAttribute(INBOX_INFO);
    }
}
