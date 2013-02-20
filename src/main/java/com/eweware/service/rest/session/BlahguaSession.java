package main.java.com.eweware.service.rest.session;

import org.apache.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;

/**
 * @author rk@post.harvard.edu
 *         Date: 12/28/12 Time: 1:59 PM
 */
public final class BlahguaSession {

    /**
     * Set to a Boolean. Indicates whether the user was authenticated or not.
     */
    public static final String AUTHENTICATION_STATE = "A";
    /**
     * Set to a Set of Strings, each string is a group id
     * These are the groups that the user is currently watching.
     */
    public static final String VIEWING_COUNT_GROUP_IDS = "G";

    // TODO debugging
    public static final String USERNAME = "U";


    /**
     * Marks the user session as authenticated.
     *
     * @param request              The http request object
     * @param passedAuthentication True if it passed authentication
     * @param username    TODO remove (dbg)
     */
    public static void markAuthenticated(HttpServletRequest request, Boolean passedAuthentication, String username) {
        final HttpSession session = request.getSession(true);
        if (username != null) {
            session.setAttribute(USERNAME, username);
        }
        session.setAttribute(AUTHENTICATION_STATE, passedAuthentication ? SessionState.AUTHENTICATED : SessionState.ANONYMOUS);
    }

    public static boolean isAuthenticated(HttpServletRequest request) {
        final HttpSession session = request.getSession();
        if (session == null) { // don't create one
            return false;
        }
        return ((SessionState) session.getAttribute(AUTHENTICATION_STATE) == SessionState.AUTHENTICATED);
    }


    public static void addCurrentlyViewedGroup(String groupId, HttpServletRequest request) {
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
}
