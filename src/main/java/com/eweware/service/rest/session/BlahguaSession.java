package main.java.com.eweware.service.rest.session;

import main.java.com.eweware.service.base.date.DateUtils;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.InvalidAuthorizedStateException;
import main.java.com.eweware.service.base.error.ResourceNotFoundException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.store.dao.type.UserAccountType;
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
     * <p>Dubious workaround by yours truly to facilitate debugging and testing.</p>
     */
    private static boolean securityOn = true;


    // TODO remove this before rollout: security must always be on
    public static final void setSecurity(boolean onOrOff) {
        securityOn = onOrOff;
    }

    public static final boolean getSecurity() {
        return securityOn;
    }

    /**
     * <p>Marks whether the account is authenticated or not.</p>
     * <p>If set, it's set to Boolean.TRUE</p>
     */
    private static final String ACCOUNT_TYPE_ATTRIBUTE = "T";

    /**
     * Set to a Boolean. Indicates whether the user was authenticated or not.
     */
    private static final String AUTHENTICATION_STATE_ATTRIBUTE = "A";

    /**
     * Set to a Set of Strings, each string is a group id
     * These are the groups that the user is currently watching.
     */
    private static final String VIEWING_GROUP_ID_ATTRIBUTE = "G";

    /**
     * If the user is authenticated, this is the user's id.
     */
    private static final String USER_ID_ATTRIBUTE = "I";

    /**
     * If the user is authenticated, this is the username.
     */
    private static final String USERNAME_ATTRIBUTE = "U";

    /**
     * An InboxInfo instance.
     * This is the last inbox number that the session has seen.
     *
     * @see InboxInfo
     */
    private static final String INBOX_INFO_ATTRIBUTE = "i";

    /**
     * Marks the user session as authenticated.
     * Input parameters are not checked!
     *
     * @param request     The http request object (unchecked!)
     * @param userId      The user id  (unchecked!)
     * @param accountType The account type. Currently is set only if it is an admin account. (unchecked!)
     * @param username    The username (unchecked!)
     */
    public static void markAuthenticated(HttpServletRequest request, String userId, String accountType, String username) {
        final HttpSession session = request.getSession(true);
        markAuthenticated(session, userId, accountType, username);
    }

    /**
     * Marks the user session as authenticated.
     * Input parameters are not checked!
     *
     * @param userId      The user id (unchecked!)
     * @param username    The username (unchecked!)
     * @param accountType The account type. Currently is set only if it is an admin account
     */
    private static void markAuthenticated(HttpSession session, String userId, String accountType, String username) {
        if (username != null) {
            session.setAttribute(USERNAME_ATTRIBUTE, username);
        }
        if (accountType != null && accountType.equals(UserAccountType.ADMIN.getCode())) {
            session.setAttribute(ACCOUNT_TYPE_ATTRIBUTE, Boolean.TRUE);
        }
        session.setAttribute(USER_ID_ATTRIBUTE, userId);
        session.setAttribute(AUTHENTICATION_STATE_ATTRIBUTE, SessionState.AUTHENTICATED);
    }

    /**
     * <p>Returns true if the session is authenticated (i.e., user has logged in).</p>
     *
     * @param request The http request (unchecked!)
     * @return True if the user is logged in (authenticated)
     */
    public static boolean isAuthenticated(HttpServletRequest request) {
        final HttpSession session = request.getSession(); // don't create a session
        return (session != null) && ((SessionState) session.getAttribute(AUTHENTICATION_STATE_ATTRIBUTE) == SessionState.AUTHENTICATED);
    }

    /**
     * <p>Ensures that the user session is authenticated and returns either the session's user id or the username.</p>
     *
     * @param request  The http request
     * @param returnId If true, returns user id, else returns canonical username
     * @return Returns user id or canonical username
     * @throws InvalidAuthorizedStateException
     *          If there is no authenticated session.
     */
    public static String ensureAuthenticated(HttpServletRequest request, boolean returnId) throws InvalidAuthorizedStateException {
        ensureAuthenticated(request);
        return (String) request.getSession().getAttribute(returnId ? USER_ID_ATTRIBUTE : USERNAME_ATTRIBUTE);
    }

    /**
     * <p>Ensures that user is authenticated.</p>
     * @param request   The request
     * @throws InvalidAuthorizedStateException Thrown if the user is not authenticated
     */
    public static void ensureAuthenticated(HttpServletRequest request) throws InvalidAuthorizedStateException {
        if (securityOn && !isAuthenticated(request)) {
            throw new InvalidAuthorizedStateException("operation not supported", ErrorCodes.UNAUTHORIZED_USER);
        }
    }

    /**
     * <p>Updates the username</p>
     * @param request   The request
     * @param username  The username
     */
    public static void setUsername(HttpServletRequest request, String username) {
        final HttpSession session = request.getSession();
        if (session != null) {
            session.setAttribute(USERNAME_ATTRIBUTE, username);
        }
    }

    /**
     * <p>Ensures that the session is authenticated and that the user is an administrator.</p>
     * <p>Check disabled if security is turned off!</p>
     *
     * @param request
     */
    public static void ensureAdmin(HttpServletRequest request) throws ResourceNotFoundException {
        final HttpSession session = request.getSession();
        if (!securityOn) {    // !!
            return;
        }
        if (session != null) {
            final Object attribute = session.getAttribute(ACCOUNT_TYPE_ATTRIBUTE);
            if (attribute != null && attribute.equals(Boolean.TRUE)) {
                return;
            }
        }
        throw new ResourceNotFoundException("Incapable", ErrorCodes.UNAUTHORIZED_USER);
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
            session.removeAttribute(USER_ID_ATTRIBUTE);
            session.removeAttribute(USERNAME_ATTRIBUTE);
            session.setAttribute(AUTHENTICATION_STATE_ATTRIBUTE, SessionState.ANONYMOUS);
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
        return (session == null) ? null : (String) session.getAttribute(USER_ID_ATTRIBUTE);
    }

    /**
     * TODO for debugging only
     */
    public static String getSessionInfo(HttpServletRequest request) {
        final HttpSession s = request.getSession();
        final StringBuilder b = new StringBuilder();
        if (s != null) {
            if (s.getAttribute(AUTHENTICATION_STATE_ATTRIBUTE) == SessionState.AUTHENTICATED) {
                b.append("Authenticated Session");
                final Object userId = s.getAttribute(USER_ID_ATTRIBUTE);
                if (userId != null) {
                    b.append("\nUser Id: ");
                    b.append(userId);
                }
                final String username = (String) s.getAttribute(USERNAME_ATTRIBUTE);
                if (username != null) {
                    b.append("\nUsername: ");
                    b.append(username);
                }
            } else {
                b.append("Anonymous Session");
            }
            final String viewing = (String) s.getAttribute(BlahguaSession.VIEWING_GROUP_ID_ATTRIBUTE);
            if (viewing != null) {
                b.append("\nViewing Channel Id: ");
                b.append(viewing);
            } else {
                b.append("\nNot Viewing Any Channels");
            }
            b.append("\nSession Started: ");
            b.append(DateUtils.formatDateTime(s.getCreationTime()));
            b.append("\nLast Session Activity: ");
            final long lastAccessedTime = s.getLastAccessedTime();
            b.append(DateUtils.formatDateTime(lastAccessedTime));
            b.append("\nWill expire in ");
            long elapsed = (System.currentTimeMillis() - lastAccessedTime) / 1000;
            long timeLeft = s.getMaxInactiveInterval() - elapsed;
            long minutesLeft = (timeLeft / 60);
            long secondsLeft = (timeLeft % 60);
            if (minutesLeft > 0) {
                b.append(minutesLeft);
                b.append(" minutes ");
            }
            if (secondsLeft > 0) {
                b.append(secondsLeft);
                b.append(" seconds");
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
     * <p>Sets the group as the currently viewed group in the session.
     * If another group is currently watched, it decrements the viewer
     * count for that group in the DB.</p>
     *
     * @param request The http request (unchecked!)
     * @param groupId The groupId (may be null but unchecked for validity)
     */
    public static void setCurrentlyViewedGroup(HttpServletRequest request, String groupId) throws SystemErrorException, ResourceNotFoundException {
        if (groupId != null) {
            final HttpSession session = request.getSession(true);
            final String currentlyWatched = (String) session.getAttribute(VIEWING_GROUP_ID_ATTRIBUTE);
            session.setAttribute(VIEWING_GROUP_ID_ATTRIBUTE, groupId);

            // Update DB
            if (currentlyWatched != null) {
                if (!currentlyWatched.equals(groupId)) { // watching a different group
                    // decrement count in DB for current
                    GroupManager.getInstance().updateViewerCount(currentlyWatched, false);
                    // increment count in DB for new
                    GroupManager.getInstance().updateViewerCount(groupId, true);
                } else { // watching same group as before
                    // do nothing
                }
            } else { // increment count in DB
                GroupManager.getInstance().updateViewerCount(groupId, true);
            }
        }
    }

    /**
     * Destroys the current session.
     *
     * @param request The http request object
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
        final String groupId = (String) session.getAttribute(BlahguaSession.VIEWING_GROUP_ID_ATTRIBUTE);
        String username = (String) session.getAttribute(BlahguaSession.USERNAME_ATTRIBUTE);
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
        request.getSession().setAttribute(INBOX_INFO_ATTRIBUTE, new InboxInfo(groupId, lastInboxNumber));
    }

    private static InboxInfo getInboxInfo(HttpServletRequest request) {
        return (InboxInfo) request.getSession(true).getAttribute(INBOX_INFO_ATTRIBUTE);
    }
}
