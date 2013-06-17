package main.java.com.eweware.service.servlet;

import main.java.com.eweware.service.base.AWSUtilities;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.mgr.SystemManager;
import main.java.com.eweware.service.base.type.RunMode;
import main.java.com.eweware.service.rest.RestUtilities;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>This servlet receives from clients requests for CSS files in the /css directory.</p>
 * <p>If the run mode is PROD, the servlet passes through the contents of the
 * CSS file from the S3 bucket. Else, it will munge the CSS file to change any
 * hard-coded URLs to the appropriate ones for QA or DEV run modes.</p>
 * <p>We do this because CSS variables don't exist, so it is impossible
 * to implement this in the CSS files themselves.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 6/15/13 Time: 9:54 AM
 */
public class WebClientCSSServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(WebClientCSSServlet.class.getName());

    private static SystemManager sysMgr;
    private static RunMode runMode;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final StringBuffer url = req.getRequestURL();
        try {
            final int slash = url.lastIndexOf("/css/");
            if (slash != -1) {
                final String key = url.substring(slash+1);
                writeCSS(key, resp);
            } else {
                logger.severe("Failed to get css filename. URL: " + url);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to pass CSS to web client. URL: " + url + "\nHEADERS:\n" + RestUtilities.getRequestInfo(req), e);
            resp.sendError(500, "");
        }
    }

    /**
     * <p>If in PROD mode, get file from S3 bucket and pass it through, as-is.</p>
     * <p>If in QA or DEV mode, munge the file to correct hard-coded URLs to either
     * the S3 bucket or to the REST server.</p>
     *
     * @param s3BucketKey
     * @param resp
     */
    private void writeCSS(String s3BucketKey, HttpServletResponse resp) throws SystemErrorException, IOException {
        final String contents = AWSUtilities.getContentsFromS3(s3BucketKey);

        resp.setHeader("Content-Type", "text/css");

        if (getRunMode() == RunMode.PROD) {
            resp.getWriter().write(contents);
        } else {
//            logger.info(">>> " + getRunMode() + " mode: transforming contents of " + s3BucketKey);
            resp.getWriter().write(transform(contents));
        }
    }

    /**
     * <p>Change references from PROD S3 bucket to appropriate (QA or DEV) bucket.</p>
     * @param contents
     * @return
     * @throws SystemErrorException
     */
    private String transform(String contents) throws SystemErrorException {
        final String prodS3Bucket = getSystemManager().getS3BaseUrl() + "/" + getSystemManager().getS3WebsiteProdBucket();
        Pattern p = Pattern.compile(prodS3Bucket, Pattern.MULTILINE);
        final Matcher s3BucketRefMatcher = p.matcher(contents);
        final String s3matcher = getSystemManager().getS3BaseUrl() + "/" + getSystemManager().getWebsiteBucket();
        contents = s3BucketRefMatcher.replaceAll(s3matcher);
        return contents;
    }

    private static final SystemManager getSystemManager() throws SystemErrorException {
        if (sysMgr == null) {
            sysMgr = SystemManager.getInstance();
        }
        return sysMgr;
    }

    private static final RunMode getRunMode() throws SystemErrorException {
        if (runMode == null) {
            runMode = getSystemManager().getRunMode();
        }
        return runMode;
    }
}
