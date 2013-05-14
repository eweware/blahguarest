package main.java.com.eweware.service.base;

import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.SystemErrorException;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 *         Date: 4/8/13 Time: 3:00 PM
 */
public class AWSUtilities {

    private static final Logger logger = Logger.getLogger(AWSUtilities.class.getName());

    private static AmazonS3Client amazonS3Client = null;
    private static AmazonS3Client anonymousAmazonS3Client = null;

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

    public static String getDefaultHtmlFromS3() {
        com.amazonaws.services.s3.model.S3ObjectInputStream in = null;
        try {
            if ((anonymousAmazonS3Client == null)) {
                anonymousAmazonS3Client = new AmazonS3Client(new AnonymousAWSCredentials());
            }
            final S3Object obj = anonymousAmazonS3Client.getObject("beta.blahgua.com", "default.html");
            in = obj.getObjectContent();
            return IOUtils.toString(in, "UTF-8");
        } catch (java.lang.Exception e) {
            logger.log(Level.SEVERE, "Failed to deliver default.html from s3", e);
            return ""; // TODO should have a fallback file
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (java.lang.Exception e) {
                    logger.log(Level.SEVERE, "Failed to deliver default.html from s3", e);
                    return "";  // TODO should have a fallback file
                }
            }
        }
    }
}
