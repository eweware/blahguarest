package main.java.com.eweware.service.base;

import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.mgr.SystemManager;
import main.java.com.eweware.service.base.type.RunMode;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author rk@post.harvard.edu
 *         Date: 4/8/13 Time: 3:00 PM
 */
public class AWSUtilities {

    private static final Logger logger = Logger.getLogger(AWSUtilities.class.getName());

    private static AmazonS3Client amazonS3Client = null;
    private static AmazonS3Client anonymousAmazonS3Client = null;

    private static long FIVE_MINUTES =  1000 * 60 * 5;
    private static String defaultHtml;
    private static volatile boolean defaultHtmlCacheValid;
    private static long lastTimeDefaultHtmlCached = System.currentTimeMillis();
    private static ReentrantReadWriteLock defaultHtmlLock = new ReentrantReadWriteLock();
    private static SystemManager sysMgr;
    private static String websiteBucket;
    private static String websiteProdBucket;
    private static String restServiceBaseUrl;
    private static String s3BaseUrl;
    private static String imagesBucketUrl;
    private static String prodBaseUrl;
    private static String prodBaseUrlWithVersion;
    private static String prodImagesBucketUrl;
    private static String baseUrlWithVersion;
    private static RunMode runMode;
    private static Pattern prodCssPattern;
    private static Pattern prodBucketPattern;
    private static Pattern prodRestPattern;
    private static Pattern prodImagesBucketPattern;
    private static Pattern prodBaseUrlWithVersionPattern;

    /**
     * <p>Returns the cached default HTML page from S3.</p>
     * <p>The cache is refreshed every few minutes from S3.</p>
     *
     * @return <p>Returns the cached default HTML page from S3.</p>
     */
    public static String getDefaultHtmlFromS3() {
        defaultHtmlLock.readLock().lock();
        if (System.currentTimeMillis() > (lastTimeDefaultHtmlCached + FIVE_MINUTES) || !defaultHtmlCacheValid) {
            // Must release read lock before acquiring write lock
            defaultHtmlLock.readLock().unlock();
            defaultHtmlLock.writeLock().lock();
            // Recheck state because another thread might have acquired
            //   write lock and changed state before we did.
            defaultHtml = doGetDefaultHtmlFromS3();
            defaultHtmlCacheValid = true;
            // Downgrade by acquiring read lock before releasing write lock
            defaultHtmlLock.readLock().lock();
            defaultHtmlLock.writeLock().unlock(); // Unlock write, still hold read
        }
        final String page = defaultHtml;
        defaultHtmlLock.readLock().unlock();
        return page;
    }

    /**
     * <p>Transforms content if this is not a PROD environment.</p>
     * @return A possibly transformed content
     */
    private static String doGetDefaultHtmlFromS3() {
        try {
            String contents = getContentsFromS3("default.html");
            if (getRunMode() != RunMode.PROD) {
                contents = transformDefaultHtmlFile(contents);
            }
            return contents;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to deliver default.html from s3", e);
            return ""; // TODO should have a default fallback file
        }
    }

    /**
     * <p>Takes contents of PROD version of default.html and converts it into a structure that can
     * point to the QA server</p>
     * <p>Returns the contents appropriate for QA or DEV environments.</p>
     *
     * @return The contents of the file, adjusted for the current environment.
     */
    private static String transformDefaultHtmlFile(String contents) throws SystemErrorException {

        /* Replace the reference to blahgua.css with links to all files imported by it */
        contents = maybeReplaceAll(getProdCssPattern(), contents, getCssLinks());

        /* Point S3 bucket references to QA or DEV bucket */
        contents = maybeReplaceAll(getProdBucketPattern(), contents, getS3BaseUrl() + "/" + getWebsiteBucket());

        /** Replace PROD REST version references to blahgua REST or QA */
        contents = maybeReplaceAll(getProdBaseUrlWithVersionPattern(), contents, getBaseUrlWithVersion());

        /** Replace PROD REST references to blahgua REST or QA */
        contents = maybeReplaceAll(getProdRestPattern(), contents, getRestServiceBaseUrl());

        return maybeReplaceAll(getProdImagesBucketPattern(), contents, getImagesBucketUrl());
    }

    private static String maybeReplaceAll(Pattern regex, String string, String replacement) {
        final Matcher matcher = regex.matcher(string);
        if (matcher.find()) {
            return matcher.replaceAll(replacement);
        } else {
            return string;
        }
    }

    private static Pattern getProdImagesBucketPattern() throws SystemErrorException {
        if (prodImagesBucketPattern == null) {
            prodImagesBucketPattern = Pattern.compile(getProdImagesBucketUrl(), Pattern.MULTILINE);
        }
        return prodImagesBucketPattern;
    }

    private static String  getProdImagesBucketUrl() throws SystemErrorException {
        if (prodImagesBucketUrl == null) {
            prodImagesBucketUrl = getSystemManager().getProdImagesBucketUrl();
        }
        return prodImagesBucketUrl;
    }

    private static String getImagesBucketUrl() throws SystemErrorException {
        if (imagesBucketUrl == null) {
            imagesBucketUrl = getSystemManager().getImagesBucketUrl();
        }
        return imagesBucketUrl;
    }

    private static Pattern getProdRestPattern() throws SystemErrorException {
        if (prodRestPattern == null) {
            prodRestPattern = Pattern.compile(getProdBaseUrl(), Pattern.MULTILINE);
        }
        return prodRestPattern;
    }

    private static Pattern getProdBucketPattern() throws SystemErrorException {
        if (prodBucketPattern == null) {
            prodBucketPattern = Pattern.compile(getS3BaseUrl() + "/" + getS3WebsiteProdBucket(), Pattern.MULTILINE);
        }
        return prodBucketPattern;
    }

    private static Pattern getProdCssPattern() throws SystemErrorException {
        if (prodCssPattern == null) {
            prodCssPattern = Pattern.compile("(<link rel=\"stylesheet\" href=\"" + getS3BaseUrl() + "/" + getS3WebsiteProdBucket() + "/css/blahgua.css\"" + " />)");
        }
        return prodCssPattern;
    }

    private static Pattern getProdBaseUrlWithVersionPattern() throws SystemErrorException {
        if (prodBaseUrlWithVersionPattern == null) {
            prodBaseUrlWithVersionPattern = Pattern.compile(getProdBaseUrlWithVersion(), Pattern.MULTILINE);
        }
        return prodBaseUrlWithVersionPattern;
    }

    // Don't need to cache this because access is under default.html lock and cache timing
    private static String getCssLinks() throws SystemErrorException {
        final String css = getContentsFromS3("css/blahgua.css");
        final String[] lines = css.split("\n");
        final List<String> imports = new ArrayList<String>(lines.length);
        Pattern f = Pattern.compile("\\\"(\\S+)\\\"", Pattern.MULTILINE | Pattern.DOTALL);
        for (String imp : lines) {
            final Matcher matcher = f.matcher(imp);
            if (matcher.find()) {
                imports.add(matcher.group(1));
            }
        }
        final StringBuilder links = new StringBuilder();
        for (String filename : imports) {
            links.append("<link rel=\"stylesheet\" href=\"");
            links.append(getRestServiceBaseUrl());
            links.append("/css/");
            links.append(filename);
            links.append("\" />\n");
        }
        return links.toString();
    }

    /**
     * <p>Returns the contents of the specified key in the default run mode bucket.</p>
     *
     * @param key The bucket key (e.g., /css/foo.css)
     * @return Returns the contents as a String
     */
    public static String getContentsFromS3(String key) {
        com.amazonaws.services.s3.model.S3ObjectInputStream in = null;
        String bucket = null;
        try {
            if ((anonymousAmazonS3Client == null)) {
                anonymousAmazonS3Client = new AmazonS3Client(new AnonymousAWSCredentials());
            }
            bucket = getWebsiteBucket();
            final S3Object obj = anonymousAmazonS3Client.getObject(bucket, key);
            in = obj.getObjectContent();
            return IOUtils.toString(in, "UTF-8");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to deliver '" + key + "' from s3 bucket '" + bucket + "'", e);
            return ""; // TODO should have a default fallback file
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Failed to deliver '" + key + "' from s3 bucket '" + bucket + "'", e);
                    return "";  // TODO should have a default fallback file
                }
            }
        }
    }

    private static SystemManager getSystemManager() throws SystemErrorException {
        if (sysMgr == null) {
            sysMgr = SystemManager.getInstance();
        }
        return sysMgr;
    }

    private static RunMode getRunMode() throws SystemErrorException {
        if (runMode == null) {
            runMode = getSystemManager().getRunMode();
        }
        return runMode;
    }

    private static String getWebsiteBucket() throws SystemErrorException {
        if (websiteBucket == null) {
            websiteBucket = getSystemManager().getWebsiteBucket();
        }
        return websiteBucket;
    }

    private static String getS3WebsiteProdBucket() throws SystemErrorException {
        if (websiteProdBucket == null) {
            websiteProdBucket = getSystemManager().getS3WebsiteProdBucket();
        }
        return websiteProdBucket;
    }

    private static String getRestServiceBaseUrl() throws SystemErrorException {
        if (restServiceBaseUrl == null) {
            restServiceBaseUrl = getSystemManager().getRestServiceBaseUrl();
        }
        return restServiceBaseUrl;
    }

    private static String getS3BaseUrl() throws SystemErrorException {
        if (s3BaseUrl == null) {
            s3BaseUrl = getSystemManager().getS3BaseUrl();
        }
        return s3BaseUrl;
    }

    private static String getProdBaseUrl() throws SystemErrorException {
        if (prodBaseUrl == null) {
            prodBaseUrl = getSystemManager().getProdBaseUrl();
        }
        return prodBaseUrl;
    }

    private static String getProdBaseUrlWithVersion() throws SystemErrorException {
        if (prodBaseUrlWithVersion == null) {
            prodBaseUrlWithVersion = getSystemManager().getProdBaseUrlWithVersion();
        }
        return prodBaseUrlWithVersion;
    }

    private static String getBaseUrlWithVersion() throws SystemErrorException {
        if (baseUrlWithVersion == null) {
            baseUrlWithVersion = getSystemManager().getRestServiceBaseUrlWithVersion();
        }
        return baseUrlWithVersion;
    }

    public static AmazonS3 getAmazonS3() throws SystemErrorException {
        if (amazonS3Client != null) {
            return amazonS3Client;
        }
        try {
            final InputStream resourceAsStream = AWSUtilities.class.getResourceAsStream(AWSConstants.AWS_CONFIGURATION_PROPERTIES_FILENAME);
            if (resourceAsStream == null) {
                throw new SystemErrorException("Severe error. No AWS credentials property file '" + AWSConstants.AWS_CONFIGURATION_PROPERTIES_FILENAME + "'", ErrorCodes.SERVER_SEVERE_ERROR);
            }
            AWSUtilities.amazonS3Client = new AmazonS3Client(new PropertiesCredentials(resourceAsStream));
            return amazonS3Client;
        } catch (Exception e) {
            throw new SystemErrorException("Failed to access AWS credentials property file '" + AWSConstants.AWS_CONFIGURATION_PROPERTIES_FILENAME + "'", e, ErrorCodes.SERVER_SEVERE_ERROR);
        }
    }
}
