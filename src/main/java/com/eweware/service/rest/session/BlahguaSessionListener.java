package main.java.com.eweware.service.rest.session;

import main.java.com.eweware.service.mgr.GroupManager;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 *         Date: 2/19/13 Time: 2:57 PM
 */
public class BlahguaSessionListener implements HttpSessionListener {

    private static final Logger logger = Logger.getLogger("BlahguaSessionListener");

    @Override
    public void sessionCreated(HttpSessionEvent se) {
         se.getSession().setAttribute(BlahguaSession.AUTHENTICATION_STATE, SessionState.ANONYMOUS);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {

        final HttpSession session = se.getSession();
        try {
            final Set<String> groupIds = (Set<String>) session.getAttribute(BlahguaSession.VIEWING_COUNT_GROUP_IDS);
            String username = (String) session.getAttribute(BlahguaSession.USERNAME);
            if (username == null) {username = "unknown";} // dbg username
            logger.info("Session destroyed for username '"+ username +"': viewing group ids: " + ((groupIds == null) ? null : groupIds));
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
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error accessing session group viewer count", e);
        }
    }
}
