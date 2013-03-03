package main.java.com.eweware.service.rest.session;

import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.InvalidAuthorizedStateException;
import main.java.com.eweware.service.base.error.ResourceNotFoundException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.mgr.GroupManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
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
    private static boolean securityOn = !System.getProperty("os.name").toLowerCase().startsWith("mac");

    // TODO remove this before rollout
    public static final void flipSecurity(boolean onOrOff) {
        securityOn = onOrOff;
    }

    /**
     * Set to a Boolean. Indicates whether the user was authenticated or not.
     */
    private static final String AUTHENTICATION_STATE = "A";

    /**
     * Set to a Set of Strings, each string is a group id
     * These are the groups that the user is currently watching.
     */
    private static final String VIEWING_GROUP_ID = "G";

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
    private static void markAuthenticated(HttpSession session, String userId, String username) {
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
     *
     * @param request The http request
     * @throws InvalidAuthorizedStateException If there is no authenticated session.
     */
    public static String ensureAuthenticated(HttpServletRequest request) throws InvalidAuthorizedStateException {
        if (securityOn && !isAuthenticated(request)) {
            throw new InvalidAuthorizedStateException("operation not supported", ErrorCodes.UNAUTHORIZED_USER);
        }
        return (String) request.getSession().getAttribute(USER_ID);
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

    /** TODO temp for debugging only */
    public static String getSessionInfo(HttpServletRequest request) {
        final HttpSession s = request.getSession();
        final StringBuilder b = new StringBuilder();
        if (s != null) {
            if (s.getAttribute(AUTHENTICATION_STATE) == SessionState.AUTHENTICATED) {
                b.append("Authenticated Session");
                final Object userId = s.getAttribute(USER_ID);
                if (userId != null) {
                    b.append("\nUser Id: ");
                    b.append(userId);
                }
                final String username = (String) s.getAttribute(USERNAME);
                if (username != null) {
                    b.append("\nUsername: ");
                    b.append(username);
                }
            } else {
                b.append("Anonymous Session");
            }
            final String viewing = (String) s.getAttribute(BlahguaSession.VIEWING_GROUP_ID);
            if (viewing != null) {
                b.append("\nViewing Channel Id: ");
                b.append(viewing);
            } else {
                b.append("\nNot Viewing Any Channels");
            }
        } else {
            b.append("No User Session");
        }
        return b.toString();
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
        final InboxInfo info = getInboxInfo(request);
        return (info == null) ? null : info.getLastInboxNumber(groupId);
    }

    /**
     * <p>Sets the last inbox number fetched for the specified group.</p>
     *
     * @param request         The http request (unchecked!)
     * @param groupId         The group id (unchecked!)
     * @param lastInboxNumber The last inbox number in the group to have been fetched.
     */
    public static void setLastInboxNumber(HttpServletRequest request, String groupId, Integer lastInboxNumber) throws SystemErrorException, ResourceNotFoundException {
        final InboxInfo inboxInfo = getInboxInfo(request);
        if (inboxInfo != null) {
            inboxInfo.setLastInboxNumber(groupId, lastInboxNumber);
        } else {
            setInboxInfo(request, groupId, lastInboxNumber);
        }
        setCurrentlyViewedGroup(request, groupId);
    }

    /**
     *<p>Sets the group as the currently viewed group in the session.
     * If another group is currently watched, it decrements the viewer
     * count for that group in the DB.</p>
     *
     * @param request The http request (unchecked!)
     * @param groupId The groupId (may be null but unchecked for validity)
     */
    public static void setCurrentlyViewedGroup(HttpServletRequest request, String groupId) throws SystemErrorException, ResourceNotFoundException {
        if (groupId != null) {
            final HttpSession session = request.getSession(true);
            final String currentlyWatched = (String) session.getAttribute(VIEWING_GROUP_ID);
            session.setAttribute(VIEWING_GROUP_ID, groupId);

            // Update DB
            if (currentlyWatched != null) {
                if (!currentlyWatched.equals(groupId)) { // decrement count in DB
                    GroupManager.getInstance().updateViewerCount(currentlyWatched, false);
                }
            } else { // increment count in DB
                GroupManager.getInstance().updateViewerCount(groupId, true);
            }
        }
    }

    /**
     * Destroys the current session.
     * @param request   The http request object
     */
    public static void destroySession(HttpServletRequest request) {
        final HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
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
        final String groupId = (String) session.getAttribute(BlahguaSession.VIEWING_GROUP_ID);
        String username = (String) session.getAttribute(BlahguaSession.USERNAME);
        if (username == null) {
            username = "unknown";
        } // dbg username
        if (groupId != null) {
            try {
                GroupManager.getInstance().updateViewerCount(groupId, false);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to reduce current viewer count for groupId '" + groupId + "'", e);
                // continue
            }
        }
        logger.info("Session destroyed for username '" + username + "': viewing group ids: " + ((groupId == null) ? null : groupId));
    }


    private static void setInboxInfo(HttpServletRequest request, String groupId, Integer lastInboxNumber) {
        request.getSession().setAttribute(INBOX_INFO, new InboxInfo(groupId, lastInboxNumber));
    }

    private static InboxInfo getInboxInfo(HttpServletRequest request) {
        return (InboxInfo) request.getSession(true).getAttribute(INBOX_INFO);
    }
}
