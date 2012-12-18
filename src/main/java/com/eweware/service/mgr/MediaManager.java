package main.java.com.eweware.service.mgr;

import com.mongodb.gridfs.GridFS;
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

	public static MediaManager getInstance() throws SystemErrorException {
		if (MediaManager.singleton == null) {
			throw new SystemErrorException("MediaManager not initialized");
		}
		return singleton;
	}
	
	private ManagerState state;
	private GridFS gridFS;
	
	public MediaManager() {
		MediaManager.singleton = this;
        this.state = ManagerState.INITIALIZED;
        System.out.println("*** MediaManager initialized ***");
	}
	
	public ManagerState getState() {
		return state;
	}
	
	public void start() {
//        try {
//            this.gridFS = new GridFS(((MongoStoreManager) MongoStoreManager.getInstance()).getMediaDb());
//        } catch (SystemErrorException e) {
//            throw new WebServiceException(e);
//        }
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
