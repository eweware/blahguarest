package main.java.com.eweware.service.mgr;

import com.mongodb.gridfs.GridFS;
import main.java.com.eweware.service.base.error.ErrorCodes;
import main.java.com.eweware.service.base.error.ResourceNotFoundException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.mgr.ManagerInterface;
import main.java.com.eweware.service.base.mgr.ManagerState;

import javax.ws.rs.core.Response;

/**
 * @author rk@post.harvard.edu
 *
 * TODO discontinued. Left this code in case we need it again.
 */
public final class MediaManager implements ManagerInterface {

	private static MediaManager singleton;

    private final String _imagePathname;
    private final String _bucketImageDir;
    private final String _bucketOriginalDir;
    private final String _imageBucketName;
    private ManagerState _state = ManagerState.UNKNOWN;


	public static MediaManager getInstance() throws SystemErrorException {
		if (MediaManager.singleton == null) {
			throw new SystemErrorException("MediaManager not initialized");
		}
		return singleton;
	}
//	private GridFS _gridFS;
	
	public MediaManager(
            String imagePathname,
            String imageBucketName,
            String imagesDir,
            String originalsDir) {
        MediaManager.singleton = this;
        _imagePathname = imagePathname;
        _bucketImageDir = imagesDir;
        _bucketOriginalDir = originalsDir;
        _imageBucketName = imageBucketName;
        _state = ManagerState.INITIALIZED;
        System.out.println("*** MediaManager initialized ***");
    }

    public ManagerState getState() {
		return _state;
	}

    /**
     * @return String   The pathname to the images directory.
     * // TODO we'll need to shard these eventually
     */
    public String getImagePathname() throws SystemErrorException {
        ensureReady();
        return _imagePathname;
    }

    /**
     * @return String   The bucket name directory for images.
     */
    public String getBucketImageDir() throws SystemErrorException {
        ensureReady();
        return _bucketImageDir;
    }

    /**
     * @return String   The bucket directory for originally uploaded files
     */
    public String getBucketOriginalDir() throws SystemErrorException {
        ensureReady();
        return _bucketOriginalDir;
    }

    /**
     * @return String The name of the bucket where images are held
     * // TODO we'll need to shard these eventually
     */
    public String getImageBucketName() {
        return _imageBucketName;
    }

    public void start() {
//        try {
//            gridFS = new GridFS(((MongoStoreManager) MongoStoreManager.getInstance()).getMediaDb());
//        } catch (SystemErrorException e) {
//            throw new WebServiceException(e);
//        }
//        System.out.println("*** MediaManager starting ***");
        _state = ManagerState.STARTED;
        System.out.println("Images Bucket=" + _imageBucketName);
        System.out.println("Formatted Images Pathname=" + _imagePathname);
        System.out.println("Original Images Pathname=" + _bucketOriginalDir);
        System.out.println("*** MediaManager started ***");
    }

    public void shutdown() {
        _state = ManagerState.SHUTDOWN;
        System.out.println("*** MediaManager shut down  ***");
    }

	/** TODO add if-modified support */
	public Response getImage(LocaleId localeId, String filename) throws ResourceNotFoundException, SystemErrorException {
//        final GridFSDBFile file = gridFS.findOne(new BasicDBObject("filename", filename));
//		if (file == null) {
//			throw new ResourceNotFoundException("Image '"+filename+"' not found", filename, ErrorCodes.MEDIA_NOT_FOUND);
//		}
//        final InputStream in = file.getInputStream();
//		return Response.ok(in).lastModified(file.getUploadDate()).build();
        ensureReady();
        return Response.noContent().build();
    }

    private void ensureReady() throws SystemErrorException {
        if (_state != ManagerState.STARTED) {
            throw new SystemErrorException("System not ready", ErrorCodes.SERVER_NOT_INITIALIZED);
        }
    }
}
