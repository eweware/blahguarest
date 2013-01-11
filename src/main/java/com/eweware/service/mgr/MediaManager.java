package main.java.com.eweware.service.mgr;

import com.mongodb.gridfs.GridFS;
import main.java.com.eweware.service.base.error.ResourceNotFoundException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.mgr.ManagerInterface;
import main.java.com.eweware.service.base.mgr.ManagerState;

import javax.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 *
 * TODO discontinued. Left this code in case we need it again.
 */
public final class MediaManager implements ManagerInterface {

	private static MediaManager singleton;

    private final String imagePathname;
    private final String bucketImageDir;
    private final String bucketOriginalDir;
    private String imageBucketName;

	public static MediaManager getInstance() throws SystemErrorException {
		if (MediaManager.singleton == null) {
			throw new SystemErrorException("MediaManager not initialized");
		}
		return singleton;
	}
	
	private ManagerState state;
	private GridFS gridFS;
	
	public MediaManager(
            String imagePathname,
            String imageBucketName,
            String imagesDir,
            String originalsDir) {
        MediaManager.singleton = this;
        this.imagePathname = imagePathname;
        this.bucketImageDir = imagesDir;
        this.bucketOriginalDir = originalsDir;
        this.imageBucketName = imageBucketName;
        this.state = ManagerState.INITIALIZED;
        System.out.println("*** MediaManager initialized ***");
    }

    public ManagerState getState() {
		return state;
	}

    /**
     * @return String   The pathname to the images directory.
     * // TODO we'll need to shard these eventually
     */
    public String getImagePathname() {
        return imagePathname;
    }

    /**
     * @return String   The bucket name directory for images.
     */
    public String getBucketImageDir() {
        return bucketImageDir;
    }

    /**
     * @return String   The bucket directory for originally uploaded files
     */
    public String getBucketOriginalDir() {
        return bucketOriginalDir;
    }

    /**
     * @return String The name of the bucket where images are held
     * // TODO we'll need to shard these eventually
     */
    public String getImageBucketName() {
        return imageBucketName;
    }

    public void start() {
//        try {
//            this.gridFS = new GridFS(((MongoStoreManager) MongoStoreManager.getInstance()).getMediaDb());
//        } catch (SystemErrorException e) {
//            throw new WebServiceException(e);
//        }
//        System.out.println("*** MediaManager starting ***");
        System.out.println("Images Bucket=" + getImageBucketName());
        System.out.println("Images Pathname=" + getImagePathname());
        this.state = ManagerState.STARTED;
        System.out.println("*** MediaManager started ***");
    }

    public void shutdown() {
        this.state = ManagerState.SHUTDOWN;
        System.out.println("*** MediaManager shut down  ***");
    }

	/** TODO add if-modified support */
	public Response getImage(LocaleId localeId, String filename) throws ResourceNotFoundException {
//        final GridFSDBFile file = gridFS.findOne(new BasicDBObject("filename", filename));
//		if (file == null) {
//			throw new ResourceNotFoundException("Image '"+filename+"' not found", filename, ErrorCodes.MEDIA_NOT_FOUND);
//		}
//        final InputStream in = file.getInputStream();
//		return Response.ok(in).lastModified(file.getUploadDate()).build();
        return Response.noContent().build();
    }
}
