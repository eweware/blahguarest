package main.java.com.eweware.service.servlet;

import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.mgr.SystemManager;
import main.java.com.eweware.service.mgr.UserManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>Handles user session recovery</p>
 * @author rk@post.harvard.edu
 *         Date: 3/2/13 Time: 3:33 PM
 */
public class UserSessionRecoveryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        super.doGet(req, resp);
        final String recoveryCode = (String) req.getParameter("n");
        try { // TODO if any is null, redirect to error page
            final boolean doRedirect = UserManager.getInstance().recoverUserAndRedirectToMainPage(LocaleId.en_us, req, recoveryCode);
            if (doRedirect) {
                resp.sendRedirect("http://" + SystemManager.getInstance().getClientServiceEndpoint());
            } else {
                resp.sendError(401, "NOT ALLOWED");
            }
        } catch (Exception e) {
            throw new ServletException("Failed to recover user account", e);
        }
    }
}
