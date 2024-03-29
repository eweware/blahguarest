package com.eweware.service.rest.session;

import com.eweware.service.base.date.DateUtils;
import com.eweware.service.base.error.ErrorCodes;
import com.eweware.service.base.error.InvalidAuthorizedStateException;
import com.eweware.service.base.error.ResourceNotFoundException;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.store.dao.type.UserAccountType;
import com.eweware.service.mgr.GroupManager;
import com.eweware.service.mgr.TrackingManager;
import com.eweware.service.rest.RestUtilities;
import com.eweware.service.user.validation.Login;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Key;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.crypto.provider.AESKeyGenerator;
import com.sun.crypto.provider.DESKeyFactory;
import com.sun.crypto.provider.DESKeyGenerator;
import org.apache.commons.codec.binary.Base64;

/**
 * <p>A set of static methods to handle session state.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 12/28/12 Time: 1:59 PM
 */
public final class BlahguaSession {

    private static final String SECRET_CLIENT_KEY = "ImAGZFi9J9NfcKjPNljZfw==";

    private static final Logger logger = Logger.getLogger(BlahguaSession.class.getName());

    /**
     * <p>Marks whether the account is authenticated or not.</p>
     * <p>If set, it's set to Boolean.TRUE</p>
     */
    private static final String ACCOUNT_TYPE_ATTRIBUTE = "T";

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
     * If the user wants mature content
     */
    private static final String USER_WANTS_MATURE_CONTENT = "XXX";

    private static final String AUTHENTICATED_CLIENT = "AUTH";
    private static final String CLIENT_VERIFY_STRING = "CS";

    /**
     * An InboxInfo instance.
     * This is the last inbox number that the session has seen.
     *
     * @see InboxInfo
     */
    private static final String INBOX_INFO_ATTRIBUTE = "i";

    private static final String[] ATTRIBUTE_NAMES = new String[]{
            ACCOUNT_TYPE_ATTRIBUTE,
//            AUTHENTICATION_STATE_ATTRIBUTE,
            VIEWING_GROUP_ID_ATTRIBUTE,
            USER_ID_ATTRIBUTE,
            USERNAME_ATTRIBUTE,
            USER_WANTS_MATURE_CONTENT
    };

    /**
     * Marks the user session as authenticated.
     * Input parameters are not checked!
     *
     * @param request     The http request object (unchecked!)
     * @param userId      The user id  (unchecked!)
     * @param accountType The account type. Currently is set only if it is an admin account. (unchecked!)
     * @param username    The username (unchecked!)
     */
    public static void markAuthenticated(HttpServletRequest request, String userId, String accountType, String username, Boolean wantsMature) throws SystemErrorException {
        final HttpSession session = request.getSession(true);
        markAuthenticated(session, userId, accountType, username, wantsMature);
        final StringBuilder b = new StringBuilder("Authenticated username '");
        b.append(username);
        b.append("' (canonical name '");
        b.append(Login.makeCanonicalUsername(username));
        b.append("'), user id '");
        b.append(userId);
        b.append(". INFO:\n");
        b.append(RestUtilities.getRequestInfo(request));
        logger.finer(b.toString());
    }

    /**
     * Marks the user session as authenticated.
     * Input parameters are not checked!
     *
     * @param userId      The user id (unchecked!)
     * @param username    The username (unchecked!)
     * @param accountType The account type. Currently is set only if it is an admin account
     */
    private static void markAuthenticated(HttpSession session, String userId, String accountType, String username, Boolean wantsMature) throws SystemErrorException {
        try {
            if (username != null) {
                session.setAttribute(USERNAME_ATTRIBUTE, username);
            }
            if (wantsMature != null)
                session.setAttribute(USER_WANTS_MATURE_CONTENT, wantsMature);
            else
                session.setAttribute(USER_WANTS_MATURE_CONTENT, false);
            if (accountType != null && accountType.equals(UserAccountType.ADMIN.getCode())) {
                session.setAttribute(ACCOUNT_TYPE_ATTRIBUTE, Boolean.TRUE);
            }
            session.setAttribute(USER_ID_ATTRIBUTE, userId);
        } catch (IllegalStateException e) {
            throw new SystemErrorException("Attempted to set username '" + username + "', user id '" + userId + "' to an invalidated session id '" + session.getId() + "'", e, ErrorCodes.INVALID_SESSION_STATE);
        }
    }

    /**
     * <p>Returns true if the session is authenticated (i.e., user has logged in).</p>
     *
     * @param request The http request (unchecked!)
     * @return True if the user is logged in (authenticated)
     */
    public static boolean isAuthenticated(HttpServletRequest request) throws SystemErrorException {
        final HttpSession session = request.getSession(); // don't create a session
        try {
            return (session.getAttribute(USER_ID_ATTRIBUTE) != null);
        } catch (IllegalStateException e) {
            throw new SystemErrorException("Attempted to get attributes from an invalidated session id '" + session.getId() + "'", e, ErrorCodes.INVALID_SESSION_STATE);
        }
    }

    public static boolean isAuthenticatedClient(HttpServletRequest request) throws SystemErrorException {
        final HttpSession session = request.getSession(); // don't create a session
        try {
            return (session.getAttribute(AUTHENTICATED_CLIENT) != null);
        } catch (IllegalStateException e) {
            throw new SystemErrorException("Attempted to get attributes from an invalidated session id '" + session.getId() + "'", e, ErrorCodes.INVALID_SESSION_STATE);
        }
    }

    public static String getClientAuthString(HttpServletRequest request) throws SystemErrorException {
        final HttpSession session = request.getSession(); // don't create a session
        try {
            String authString = (String)session.getAttribute(CLIENT_VERIFY_STRING);
            if (authString == null) {
                authString = UUID.randomUUID().toString();
                session.setAttribute(CLIENT_VERIFY_STRING, authString);
            }
            return authString;
        } catch (IllegalStateException e) {
            throw new SystemErrorException("Attempted to get attributes from an invalidated session id '" + session.getId() + "'", e, ErrorCodes.INVALID_SESSION_STATE);
        }
    }

    public static boolean AuthenticateClient(HttpServletRequest request, String hashString) throws SystemErrorException {
        final HttpSession session = request.getSession(); // don't create a session
        try {
            String storedString =  (String)session.getAttribute(CLIENT_VERIFY_STRING);
            byte[] encodedKey     = Base64.decodeBase64(SECRET_CLIENT_KEY);

            Cipher encrypter = Cipher.getInstance("AES/CBC/PKCS5PADDING");

            byte[] iv = { 1, 2, 3, 4, 5, 6, 6, 5, 4, 3, 2, 1, 7, 7, 7, 7 };
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            Key SecretKey = new SecretKeySpec(encodedKey, "AES");
            encrypter.init(Cipher.ENCRYPT_MODE, SecretKey, ivspec);

            String finalStr = Base64.encodeBase64String(encrypter.doFinal(storedString.getBytes()));

            if (hashString.compareTo(finalStr) == 0) {
                session.setAttribute(AUTHENTICATED_CLIENT, true);
                return true;
            } else
                return false;

        } catch (IllegalStateException e) {
            throw new SystemErrorException("Attempted to set attributes from an invalidated session id '" + session.getId() + "'", e, ErrorCodes.INVALID_SESSION_STATE);
        } catch (Exception exp) {
            return false;
        }
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
    public static String ensureAuthenticated(HttpServletRequest request, boolean returnId) throws InvalidAuthorizedStateException, SystemErrorException {
        ensureAuthenticated(request);
        final HttpSession session = request.getSession();
        try {
            return (String) session.getAttribute(returnId ? USER_ID_ATTRIBUTE : USERNAME_ATTRIBUTE);
        } catch (IllegalStateException e) {
            throw new SystemErrorException("Attempted to get user id/name attributes from an invalidated session id '" + session.getId() + "'", e, ErrorCodes.INVALID_SESSION_STATE);
        }
    }

    /**
     * <p>Ensures that user is authenticated.</p>
     *
     * @param request The request
     * @throws InvalidAuthorizedStateException
     *          Thrown if the user is not authenticated
     */
    public static void ensureAuthenticated(HttpServletRequest request) throws InvalidAuthorizedStateException, SystemErrorException {
        if (!isAuthenticated(request)) {
            throw new InvalidAuthorizedStateException("access not authorized", ErrorCodes.UNAUTHORIZED_USER);
        }
    }

    /**
     * <p>Updates the username</p>
     *
     * @param request  The request
     * @param username The username
     */
    public static void setUsername(HttpServletRequest request, String username) throws SystemErrorException {
        final HttpSession session = request.getSession();
        if (session != null) {
            try {
                session.setAttribute(USERNAME_ATTRIBUTE, username);
            } catch (IllegalStateException e) {
                throw new SystemErrorException("Attempted to set username '" + username + "' attribute to an invalidated session id '" + session.getId() + "'", e, ErrorCodes.INVALID_SESSION_STATE);
            }
        }
    }

    /**
     * <p>Updates the mature flag</p>
     *
     * @param request  The request
     * @param wantsMature true if the user wants mature content
     */
    public static void setWantsMature(HttpServletRequest request, Boolean wantsMature) throws SystemErrorException {
        final HttpSession session = request.getSession();
        if (session != null) {
            try {
                session.setAttribute(USER_WANTS_MATURE_CONTENT, wantsMature);
            } catch (IllegalStateException e) {
                throw new SystemErrorException("Attempted to set mature flag attribute to an invalidated session id '" + session.getId() + "'", e, ErrorCodes.INVALID_SESSION_STATE);
            }
        }
    }

    /**
     * <p>Returns the mature flag</p>
     *
     * @param request  The request
     * @return true if this session has mature content

     */
    public static Boolean getWantsMature(HttpServletRequest request) throws SystemErrorException {
        final HttpSession session = request.getSession();
        if (session != null) {
            try {
                return (Boolean) session.getAttribute(USER_WANTS_MATURE_CONTENT);
            } catch (IllegalStateException e) {
                throw new SystemErrorException("Attempted to get mature flag attribute from an invalidated session id '" + session.getId() + "'", e, ErrorCodes.INVALID_SESSION_STATE);
            }
        }
        throw new SystemErrorException("Attempted to get mature flag attribute from a null session id", null, ErrorCodes.INVALID_SESSION_STATE);
    }



    /**
     * <p>Ensures that the session is authenticated and that the user is an administrator.</p>
     * <p>Check disabled if security is turned off!</p>
     *
     * @param request
     */
    public static void ensureAdmin(HttpServletRequest request) throws ResourceNotFoundException, SystemErrorException {
        final HttpSession session = request.getSession();
        if (session != null) {
            final Object attribute;
            try {
                attribute = session.getAttribute(ACCOUNT_TYPE_ATTRIBUTE);
                if (attribute != null && attribute.equals(Boolean.TRUE)) {
                    return;
                }
            } catch (IllegalStateException e) {
                throw new SystemErrorException("Attempted to get account type attribute from an invalidated session id '" + session.getId() + "'", e, ErrorCodes.INVALID_SESSION_STATE);
            }
        }
        throw new ResourceNotFoundException("Incapable", ErrorCodes.UNAUTHORIZED_USER);
    }

    /**
     * <p>Marks the user session as anonymous</p>
     *
     * @param request The http request (unchecked!)
     */
    public static void markAnonymous(HttpServletRequest request) throws SystemErrorException {
        final HttpSession session = request.getSession();
        markAnonymous(session);
    }

    /**
     * <p>Marks the user session as anonymous</p>
     *
     * @param session The http session object
     */
    public static void markAnonymous(HttpSession session) throws SystemErrorException {
        if (session != null) {
            try {
                session.removeAttribute(USER_ID_ATTRIBUTE);
                session.removeAttribute(USERNAME_ATTRIBUTE);
                session.setAttribute(USER_WANTS_MATURE_CONTENT, false);
            } catch (IllegalStateException e) {
                throw new SystemErrorException("Failed to mark session anonymous because session id '" + session.getId() + "' was invalidated", e, ErrorCodes.INVALID_SESSION_STATE);
            }
        }
    }

    /**
     * <p>Returns the userId, if any, associated with this session.</p>
     *
     * @param request The http request (unchecked!)
     * @return The userId for this session (if it is authenticated) or
     *         null if this is not an authenticated session or there is no session.
     */
    public static String getUserId(HttpServletRequest request) throws SystemErrorException {
        final HttpSession session = request.getSession();
        try {
            return (session == null) ? null : (String) session.getAttribute(USER_ID_ATTRIBUTE);
        } catch (IllegalStateException e) {
            throw new SystemErrorException("Can't get user id because session id '" + session.getId() + " is invalidated", e, ErrorCodes.INVALID_SESSION_STATE);
        }
    }

    /**
     * TODO for debugging only
     */
    public static String getSessionInfo(HttpServletRequest request) {
        final HttpSession s = request.getSession();
        final StringBuilder b = new StringBuilder();
        if (s != null) {
            if (s.getAttribute(USER_ID_ATTRIBUTE) != null) {
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

    public static void setSessionInfo(HttpServletRequest request, Map<String, Object> info) {
        final HttpSession s = request.getSession();
        if (s != null) {
            final long lastAccessedTime = s.getLastAccessedTime();
            long elapsed = (System.currentTimeMillis() - lastAccessedTime) / 1000;
            long timeLeft = s.getMaxInactiveInterval() - elapsed;
            long minutesLeft = (timeLeft / 60);
            long secondsLeft = (timeLeft % 60);
            info.put("M", minutesLeft);
            info.put("S", secondsLeft);
        }
    }


    /**
     * Holds inbox state information.
     * Maps each groupId to the last inbox number fetched within that group.
     */
    private static class InboxInfo {
        private final Map<String, Integer> groupIdToLastInboxNumberMap = new HashMap<String, Integer>(5);
        private final Map<String, Integer> groupIdToLastSafeInboxNumberMap = new HashMap<String, Integer>(5);

        private InboxInfo(String groupId, Integer lastInboxNumber, Integer lastSafeInboxNumber) {
            if (lastInboxNumber != null)
                setLastInboxNumber(groupId, lastInboxNumber);
            if (lastSafeInboxNumber != null)
                setLastSafeInboxNumber(groupId, lastSafeInboxNumber);
        }

        private void setLastInboxNumber(String groupId, Integer lastInboxNumber) {
            groupIdToLastInboxNumberMap.put(groupId, lastInboxNumber);
        }

        private void setLastSafeInboxNumber(String groupId, Integer lastInboxNumber) {
            groupIdToLastSafeInboxNumberMap.put(groupId, lastInboxNumber);
        }

        private Integer getLastInboxNumber(String groupId) {
            return groupIdToLastInboxNumberMap.get(groupId);
        }
        private Integer getLastSafeInboxNumber(String groupId) {
            return groupIdToLastSafeInboxNumberMap.get(groupId);
        }
    }

    /**
     * Returns the last inbox number accessed in this session.
     *
     * @param request The request
     * @param groupId The group id of the inbox
     * @return The last inbox number or null if the inbox has not been visited during this session.
     */
    public static Integer getLastInboxNumber(HttpServletRequest request, String groupId, Boolean safe) throws SystemErrorException {
        final InboxInfo info = getInboxInfo(request);

        if (info == null) {
            return null;
        } else {
            if (safe)
                return info.getLastSafeInboxNumber(groupId);
            else
                return info.getLastInboxNumber(groupId);
        }
    }

    /**
     * <p>Sets the last inbox number fetched for the specified group.</p>
     *
     * @param request         The http request (unchecked!)
     * @param groupId         The group id (unchecked!)
     * @param lastInboxNumber The last inbox number in the group to have been fetched.
     */
    public static void setLastInboxNumber(HttpServletRequest request, String groupId, Integer lastInboxNumber, Boolean safe) throws ResourceNotFoundException, SystemErrorException {
        final InboxInfo inboxInfo = getInboxInfo(request);
        if (inboxInfo != null) {
            if (safe)
                inboxInfo.setLastSafeInboxNumber(groupId, lastInboxNumber);
            else
                inboxInfo.setLastInboxNumber(groupId, lastInboxNumber);
        } else {
            Integer lastMature = null, lastSafe = null;
            if (safe)
                lastSafe = lastInboxNumber;
            else
                lastMature = lastInboxNumber;
            setInboxInfo(request, groupId, lastMature, lastSafe);
        }

    }

    /**
     * <p>Sets the group as the currently viewed group in the session.
     * If another group is currently watched, it decrements the viewer
     * count for that group in the DB.</p>
     *
     * @param request The http request (unchecked!)
     * @param groupId The groupId. It must be verified to exist before calling this method.
     */
    private static void setCurrentlyViewedGroup(HttpServletRequest request, String groupId) throws SystemErrorException, ResourceNotFoundException {
        if (groupId != null) {
            final HttpSession session = request.getSession(true);
            try {
                final String currentlyWatched = (String) session.getAttribute(VIEWING_GROUP_ID_ATTRIBUTE);
                session.setAttribute(VIEWING_GROUP_ID_ATTRIBUTE, groupId);

                // Update DB
                if (currentlyWatched != null) {
                    if (!currentlyWatched.equals(groupId)) { // watching a different group
                        // decrement count in DB for current
                        try {
                            decrementViewerCount(currentlyWatched);
                        } catch (SystemErrorException e) {
                            throw e;
                        } catch (ResourceNotFoundException e) {
                            logger.log(Level.WARNING, "Failed to decrement viewer count for a channel that did not exist", e);
                            // fall through to allow increment
                        }
                        // increment count in DB for new
                        incrementViewerCount(groupId);

                    } else { // watching same group as before
                        // do nothing
                    }
                } else { // increment count in DB
                    incrementViewerCount(groupId);
                }
            } catch (IllegalStateException e) {
                throw new SystemErrorException("Failed to set channel id '" + groupId + "' viewer count (and possibly decrement an old channel) because session id '" + session.getId() + "' was invalidated", e, ErrorCodes.INVALID_SESSION_STATE);
            }
        }
    }

    public static void incrementViewerCount(String groupId) throws SystemErrorException, ResourceNotFoundException {
        GroupManager.getInstance().updateViewerCountInDB(groupId, true);
        logger.finer("Incremented viewer count for channel id '" + groupId + "'");
    }

    public static void decrementViewerCount(String groupId) throws SystemErrorException, ResourceNotFoundException {
        GroupManager.getInstance().updateViewerCountInDB(groupId, false);
        logger.finer("Decremented viewer count for channel id '" + groupId + "'");
    }

    /**
     * Destroys the current session. Called either when logging in or logging out.
     * <p>When logging in, we first destroy the current session and that's when this method is called.
     * We must delete the current session for security: typically, we may go from an http session
     * to an https session at this point and we must make sure that the session id can't be
     * hijacked: we do this by creating a new session (and new session id) that will
     * henceforward be encrypted and therefore safe(r).</p>
     * <p>When logging out, we destroy the session and allow an anonymous session to take
     * over when further operations are performed by the same user agent.</p>
     *
     * @param request The http request object
     * @param login   True if in a login context; false in a logout context.
     */
    public static void destroySession(HttpServletRequest request, boolean login) throws SystemErrorException {
        final HttpSession session = request.getSession();
        if (session != null) {
            String groupId = null;
            try {
                groupId = (String) session.getAttribute(BlahguaSession.VIEWING_GROUP_ID_ATTRIBUTE);

                if (!login) {
                    final String userId = (String) session.getAttribute(BlahguaSession.USER_ID_ATTRIBUTE);
                    final String username = (String) session.getAttribute(BlahguaSession.USERNAME_ATTRIBUTE);
                    if (userId != null || username != null) {
                        final StringBuilder b = new StringBuilder("Logged out");
                        if (username != null) {
                            b.append(" username '");
                            b.append(username);
                            b.append("'");
                        }
                        if (userId != null) {
                            b.append(" user id '");
                            b.append(userId);
                            b.append("'");
                        }
                        b.append(". INFO:\n");
                        b.append(RestUtilities.getRequestInfo(request));
                        logger.finer(b.toString());
                    }

                }

            } catch (IllegalStateException e) {
                throw new SystemErrorException("Failed to decrement current viewer count for a channel because session id '" + session.getId() + "' was invalidated", e, ErrorCodes.SERVER_SEVERE_ERROR);
            } catch (Exception e) {
                throw new SystemErrorException("Failed to decrement current viewer count for channel id '" + groupId + "' because session id '" + session.getId() + "' was invalidated", e, ErrorCodes.SERVER_SEVERE_ERROR);
            } finally {
                try {
                    removeAllAttributes(session);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Invalidating session id '" + session.getId() + "', but attributes could not be removed during a " + (login ? "login" : "logout") + " operation", e);
                }
                try {
                    session.invalidate();
                } catch (IllegalStateException e) {
                    throw new SystemErrorException("Failed to invalidate session id '" + session.getId() + "' during a " + (login ? "login" : "logout") + " operation because session was already invalidated", e, ErrorCodes.INVALID_SESSION_STATE);
                }
            }
        }
    }

    /**
     * <p>Called by the session listener when the user session is destroyed.</p>
     * <p>Reduces the current viewer count for all groups in which this user
     * is participating.</p>
     *
     * @param session The http session object
     */
    public static void sessionDestroyed(HttpSession session) throws SystemErrorException, ResourceNotFoundException {
        String groupId = null;
        String username = null;
        String userId = null;
        try {
            username = (String) session.getAttribute(BlahguaSession.USERNAME_ATTRIBUTE);
            userId = (String) session.getAttribute(BlahguaSession.USER_ID_ATTRIBUTE);
            groupId = (String) session.getAttribute(BlahguaSession.VIEWING_GROUP_ID_ATTRIBUTE);
            if (groupId != null) {
                decrementViewerCount(groupId);
            }
        } catch (IllegalStateException e) {
            throw new SystemErrorException("Failed to decrement current viewer count because session was already invalidated for some groupId", e, ErrorCodes.SERVER_SEVERE_ERROR);
        } catch (ResourceNotFoundException e) {
            logger.log(Level.WARNING, "Did not decrement channel id '" + groupId + "' because it does not exist", e);
            // fall through
        } catch (Exception e) {
            throw new SystemErrorException("Failed to decrement current viewer count for channel id '" + groupId + "'", e, ErrorCodes.SERVER_SEVERE_ERROR);
        } finally {
            try {
                removeAllAttributes(session);
            } catch (Exception e) {
                // just log it and fall through
                logger.log(Level.WARNING, "Failed to remove all attributes for invalidated session id '" + session.getId() + "'. Not crucial.", e);
            }
            final StringBuilder b = new StringBuilder("Destroyed session id '");
            b.append(session.getId());
            if (username == null) {
                b.append("' for anonymous username");
            } else {
                b.append("' for username '");
                b.append(username);
                b.append("'");
            }
            if (userId == null) {
                b.append(" unknown user id");
            } else {
                b.append(" user id '");
                b.append(userId);
                b.append("'");
            }
            if (groupId != null) {
                b.append(" viewing channel id '");
                b.append(groupId);
                b.append("'");
            } else {
                b.append(" not viewing any channels");
            }
            logger.finer(b.toString());
        }
    }

    /**
     * Cleanse session from its attributes. Doing this for sanity's sake.
     *
     * @param session
     * @throws SystemErrorException If and only if the session has been invalidated!
     */
    private static void removeAllAttributes(HttpSession session) throws SystemErrorException {
        try {
            String userId = (String) session.getAttribute(BlahguaSession.USER_ID_ATTRIBUTE);

            if (userId != null) {
                TrackingManager.getInstance().TrackUserLogout(userId);
            }

            for (String attribute : ATTRIBUTE_NAMES) {
                session.removeAttribute(attribute);
            }
        } catch (IllegalStateException e) {
            throw new SystemErrorException("Failed to remove all attributes on invalidated session id '" + session.getId() + "'", e, ErrorCodes.INVALID_SESSION_STATE);
        }
    }


    private static void setInboxInfo(HttpServletRequest request, String groupId, Integer lastInboxNumber, Integer lastSafeInboxNumber) throws SystemErrorException {
        final HttpSession session = request.getSession();
        try {
            session.setAttribute(INBOX_INFO_ATTRIBUTE, new InboxInfo(groupId, lastInboxNumber, lastSafeInboxNumber));
        } catch (IllegalStateException e) {
            throw new SystemErrorException("Failed to set inbox info for channel id '" + groupId + "' because session id '" + session.getId() + "' was already invalidated", e, ErrorCodes.INVALID_SESSION_STATE);
        }
    }

    private static InboxInfo getInboxInfo(HttpServletRequest request) throws SystemErrorException {
        final HttpSession session = request.getSession(true);
        try {
            return (InboxInfo) session.getAttribute(INBOX_INFO_ATTRIBUTE);
        } catch (IllegalStateException e) {
            throw new SystemErrorException("Failed to get inbox info attribute because session id '" + session.getId() + "' was already invalidated", e, ErrorCodes.INVALID_SESSION_STATE);
        }
    }
}
