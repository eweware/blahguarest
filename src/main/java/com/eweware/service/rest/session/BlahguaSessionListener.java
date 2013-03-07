package main.java.com.eweware.service.rest.session;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * @author rk@post.harvard.edu
 *         Date: 2/19/13 Time: 2:57 PM
 */
public class BlahguaSessionListener implements HttpSessionListener {

    /**
     * <p>A new session is created in the anonymous user state.</p>
     * @param se The session event
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        BlahguaSession.markAnonymous(se.getSession());
    }

    /**
     * <p>When a session is destroyed</p>
     * @param se
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {

        final HttpSession session = se.getSession();
        BlahguaSession.sessionDestroyed(session);
    }
}
