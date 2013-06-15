package main.java.com.eweware.service.servlet;

import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.mgr.SystemManager;
import main.java.com.eweware.service.mgr.UserManager;
import main.java.com.eweware.service.rest.RestUtilities;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Handles user session recovery</p>
 * <p>This page is accessed through an url sent to a user in an email.
 * We expect the recovery code to be correct: if not, we have either
 * a programming bug or a security breach.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 3/2/13 Time: 3:33 PM
 */
public class UserSessionRecoveryServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(UserSessionRecoveryServlet.class.getName());

    private static String restServiceBaseUrl;

    private static final String getRestServiceBaseUrl() throws SystemErrorException {
        if (restServiceBaseUrl == null) {
            restServiceBaseUrl = SystemManager.getInstance().getRestServiceBaseUrl();
        }
        return restServiceBaseUrl;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final String recoveryCode = req.getParameter("n");
        try {
            if ((recoveryCode != null) && UserManager.getInstance().recoverUserAndRedirectToMainPage(LocaleId.en_us, req, recoveryCode)) {
                resp.sendRedirect(getRestServiceBaseUrl());
            } else {
                // Since this request is directly linked from an email created by us, this is a security breach (or bug)!
                logger.severe("Security Breach: Attempt to recover account using invalid recovery code. REQUEST INFORMATION:\n" + RestUtilities.getRequestInfo(req));
                resp.sendError(401, "NOT ALLOWED"); // TODO https://eweware.atlassian.net/browse/BWC-1224
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to recover user account due to system error. REQUEST INFORMATION: \n" + RestUtilities.getRequestInfo(req), e);
            // TODO https://eweware.atlassian.net/browse/BWC-1224 (need branded page instead of Tomcat default)
            resp.sendError(500, "Sorry, but due to temporary technical difficulties, Blahgua couldn't recover your account. Please try again later. Thanks.");
        }
    }
}
