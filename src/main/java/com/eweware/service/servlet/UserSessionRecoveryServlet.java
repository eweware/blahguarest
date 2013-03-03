package main.java.com.eweware.service.servlet;

import main.java.com.eweware.service.base.error.InvalidAuthorizedStateException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.mgr.UserManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * <p>Handles user session recovery</p>
 * @author rk@post.harvard.edu
 *         Date: 3/2/13 Time: 3:33 PM
 */
public class UserSessionRecoveryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
        final String recoveryCode = (String) req.getAttribute("c");
        final String userCode = (String) req.getAttribute("n");
        try {
            final boolean doRedirect = UserManager.getInstance().recoverUserAndRedirectToMainPage(LocaleId.en_us, req, recoveryCode, userCode);
            if (doRedirect) {
                resp.sendRedirect("http://beta.blahgua.com");
            } else {
                resp.sendError(401, "NOT ALLOWED");
            }
        } catch (Exception e) {
            throw new ServletException("Failed to recover user account", e);
        }
    }
}
