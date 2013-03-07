package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.ResourceNotFoundException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.mgr.BlahManager;
import main.java.com.eweware.service.mgr.SystemManager;
import main.java.com.eweware.service.rest.RestUtilities;
import main.java.com.eweware.service.rest.session.BlahguaSession;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * <p>System-level API.</p>
 * <p>These methods are for development only and will be removed from
 * public access once we launch.</p>
 * <p><b>DANGER Mr. Robinson! DANGER!!!</b></p>
 *
 * @author rk@post.harvard.edu
 */

@Path("/sys")
public class SystemResource {

    /**
     * <p>Sheep stuff</p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> sys</div>
     *
     * @return Tells you how many sheep we have in our bank.
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
     * <p>Use this method to get REST API metrics.</p>
     * <b>This method is for development only and will be removed from
     * public access once we launch.</b>
     * <p><i>User must be authenticated and have an admin account.</i></p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> sys/metrics</div>
     *
     * @return A JSON entity with metrics.
     */
    @GET
    @Path("/metrics")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus(@Context HttpServletRequest request) {
        try {
            BlahguaSession.ensureAdmin(request);
            return RestUtilities.make200OkResponse(SystemManager.getInstance().processMetrics(false));
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Use this method to reset the REST API metrics.</p>
     * <b>This method is for development only and will be removed from
     * public access once we launch.</b>
     * <p><i>User must be authenticated and have an admin account.</i></p>
     * <p/>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> sys/metrics/reset</div>
     *
     * @return Returns http status 202 (ACCEPTED).
     */
    @POST
    @Path("/metrics/reset")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStats(@Context HttpServletRequest request) {
        try {
            BlahguaSession.ensureAdmin(request);
            SystemManager.getInstance().processMetrics(true);
            return RestUtilities.make202AcceptedResponse();
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Use this method to turn security on/off.</p>
     * <b>This method is for development only and will be removed from
     * public access once we launch.</b>
     * <p><i>User must be authenticated and have an admin account.</i></p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> sys/secure/{on}</div>
     *
     * @param action <i>Path Parameter:</i> If "on" turns security on.
     *               If "get" returns security state. Any other value turns security off.
     * @return Http status code 200 with plain text specifying the new security state.
     */
    @POST
    @Path("/security/{action}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response flipSecurity(
            @PathParam("action") String action,
            @Context HttpServletRequest request) {
        try {
//            BlahguaSession.ensureAdmin(request);
            if (action.equals("get")) {
                return Response.ok(BlahguaSession.getSecurity() ? "on" : "off").build();
            }
            final boolean on = action.equals("on");
            BlahguaSession.setSecurity(on);
            return Response.ok("security " + (on ? "ON" : "OFF")).build();
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }


    /**
     * <p>Use this method to get information about the session state.</p>
     * <b>This method is for development only and will be removed from
     * public access once we launch.</b>
     * <p><i>User must be authenticated and have an admin account.</i></p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> sys/session</div>
     *
     * @return Returns plain text (status 200) with the session info.
     */
    @GET
    @Path("/session")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getSessionInfo(@Context HttpServletRequest request) {
        try {
//            BlahguaSession.ensureAdmin(request);
            return Response.ok(BlahguaSession.getSessionInfo(request)).build();
//        } catch (ResourceNotFoundException e) {
//            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Use this method to refresh the local cache.</p>
     * <b>This method is for development only and will be removed from
     * public access once we launch.</b>
     * <p/>
     * <div><b>METHOD:</b> GET </div>
     * <div><b>URL:</b> sys/refresh</div>
     *
     * @return Returns http code 200 (ACCEPTED).
     */
    @POST
    @Path("/refresh")
    @Produces(MediaType.APPLICATION_JSON)
    public Response refreshCaches() { // TODO not using
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
