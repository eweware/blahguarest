package main.java.com.eweware.service.base;

/**
 * @author rk@post.harvard.edu
 *         Date: 4/8/13 Time: 2:50 PM
 */
public interface AWSConstants {

    /**
     * <p>File containing AWS credentials (and possibly other properties)</p>
     */
    public final String AWS_CONFIGURATION_PROPERTIES_FILENAME = "AwsCredentials.properties";

    /**
     * <p>Prefixes for each type of image held in the image buckets.</p>
     */
    public final String[] AWS_S3_IMAGE_SUFFIXES = new String[]{"-A", "-B", "-C", "-D"};
}
