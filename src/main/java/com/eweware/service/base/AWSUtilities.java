package main.java.com.eweware.service.base;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.SystemErrorException;

import java.io.InputStream;

/**
 * @author rk@post.harvard.edu
 *         Date: 4/8/13 Time: 3:00 PM
 */
public class AWSUtilities {

    private static AmazonS3Client amazonS3Client = null;

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
