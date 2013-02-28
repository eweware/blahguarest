package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.ResourceNotFoundException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.mgr.MediaManager;
import main.java.com.eweware.service.rest.RestUtilities;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * @author rk@post.harvard.edu
 */

@Path("/media")
public class MediaResource {

    /**
     *
     * <p></p>
     * <div><b>METHOD:</b> </div>
     * <div><b>URL:</b> </div>
     * @param filename
     * @return
     */
	@GET
	@Path("/{filename}")
	@Produces("image/*,audio/*")
	public Response streamFromMedia(@PathParam("filename") String filename) {
		try {
			return MediaManager.getInstance().getImage(LocaleId.en_us, filename);
		} catch (ResourceNotFoundException e) {
			return RestUtilities.make404ResourceNotFoundResponse(e);
		} catch (SystemErrorException e) {
			return RestUtilities.make500AndLogSystemErrorResponse(e);
		} catch (Exception e) {
			return RestUtilities.make500AndLogSystemErrorResponse(e);
		}
	}
}
