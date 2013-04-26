package main.java.com.eweware.service.rest.session;

import main.java.com.eweware.service.base.error.SystemErrorException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 *         Date: 2/19/13 Time: 2:57 PM
 */
public class BlahguaSessionListener implements HttpSessionListener {

    private static Logger logger = Logger.getLogger("BlahguaSessionListener");

    /**
     * <p>A new session is created in the anonymous user state.</p>
     * @param se The session event
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        final HttpSession session = se.getSession();
        try {
            logger.info("Started anonymous session id '" + session.getId() + "'");
            BlahguaSession.markAnonymous(session);
        } catch (SystemErrorException e) {
            logger.log(Level.SEVERE, "Failed to make a created session id '" + session.getId() + "' anonymous", e);
        }
    }

    /**
     * <p>When a session is destroyed</p>
     * @param se
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {

        final HttpSession session = se.getSession();
        try {
            BlahguaSession.sessionDestroyed(session);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to completely destroy session state for session id '" + session.getId(), e);
        }
    }
}
