package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.mgr.BlahManager;
import main.java.com.eweware.service.mgr.SystemManager;
import main.java.com.eweware.service.rest.RestUtilities;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author rk@post.harvard.edu
 */

@Path("/sys")
public class SystemResource {
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response sayBaaah() {
		try {
			return RestUtilities.makeOkResponse("No sheep here.\n");
		} catch (RuntimeException e) {
			return RestUtilities.makeAndLogSystemErrorResponse(e);
		}
	}
	
	@GET
	@Path("/status")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus() {
		try {
			return RestUtilities.makeOkResponse(BlahManager.getInstance().getState());
		} catch (SystemErrorException e) {
			return RestUtilities.makeAndLogSystemErrorResponse(e);
		} catch (RuntimeException e) {
			return RestUtilities.makeAndLogSystemErrorResponse(e);
		}
	}

    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStats(@QueryParam("reset") boolean reset) {
        try {
            return RestUtilities.makeOkResponse(SystemManager.getInstance().getStats(reset));
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    @POST
    @Path("/refresh")
    public Response refreshCaches() {
        try {
            BlahManager.getInstance().refreshCaches();
            return RestUtilities.makeAcceptedResponse();
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }
}
