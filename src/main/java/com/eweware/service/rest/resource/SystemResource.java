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

    /**
     *
     * <p></p>
     * <div><b>METHOD:</b> </div>
     * <div><b>URL:</b> </div>
     * @return
     */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public Response sayBaaah() {
		try {
			return RestUtilities.make200OkResponse("No sheep here.\n");
		} catch (Exception e) {
			return RestUtilities.make500AndLogSystemErrorResponse(e);
		}
	}

    /**
     *
     * <p></p>
     * <div><b>METHOD:</b> </div>
     * <div><b>URL:</b> </div>
     * @return
     */
	@GET
	@Path("/status")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getStatus() {
		try {
			return RestUtilities.make200OkResponse(BlahManager.getInstance().getState());
		} catch (SystemErrorException e) {
			return RestUtilities.make500AndLogSystemErrorResponse(e);
		} catch (Exception e) {
			return RestUtilities.make500AndLogSystemErrorResponse(e);
		}
	}

    /**
     *
     * <p></p>
     * <div><b>METHOD:</b> </div>
     * <div><b>URL:</b> </div>
     * @param reset
     * @return
     */
    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStats(@QueryParam("reset") boolean reset) {
        try {
            return RestUtilities.make200OkResponse(SystemManager.getInstance().getStats(reset));
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     *
     * <p></p>
     * <div><b>METHOD:</b> </div>
     * <div><b>URL:</b> </div>
     * @return
     */
    @POST
    @Path("/refresh")
    public Response refreshCaches() {
        try {
            BlahManager.getInstance().refreshCaches();
            return RestUtilities.make202AcceptedResponse();
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }
}
