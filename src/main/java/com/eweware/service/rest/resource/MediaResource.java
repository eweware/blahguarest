package com.eweware.service.rest.resource;

import com.eweware.service.base.error.ResourceNotFoundException;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.mgr.MediaManager;
import com.eweware.service.rest.RestUtilities;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * <p>Media-specific API methods.</p>
 * <p><b>Not in use.</b></p>
 * @author rk@post.harvard.edu
 */

@Path("/media")
public class MediaResource {

    /**
     * <p><b>Not implemented.</b></p>
     * @param filename
     * @return
     */
	@GET
	@Path("/{filename}")
	@Produces("image/*,audio/*")
	public Response streamFromMedia(@PathParam("filename") String filename,
                                    @Context HttpServletRequest request) {
		try {
			return MediaManager.getInstance().getImage(LocaleId.en_us, filename);
		} catch (ResourceNotFoundException e) {
			return RestUtilities.make404ResourceNotFoundResponse(request, e);
		} catch (SystemErrorException e) {
			return RestUtilities.make500AndLogSystemErrorResponse(request, e);
		} catch (Exception e) {
			return RestUtilities.make500AndLogSystemErrorResponse(request, e);
		}
	}
}
