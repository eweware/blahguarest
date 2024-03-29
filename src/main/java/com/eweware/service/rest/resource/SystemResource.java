package com.eweware.service.rest.resource;

import com.eweware.service.base.error.ResourceNotFoundException;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.mgr.SystemManager;
import com.eweware.service.mgr.BlahManager;
import com.eweware.service.mgr.GroupManager;
import com.eweware.service.rest.RestUtilities;
import com.eweware.service.rest.session.BlahguaSession;
import org.codehaus.jackson.annotate.JsonSetter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

/**
 * <p>System-level API.</p>
 * <p>Some methods are for development only and will be removed from
 * public access once we launch.</p>
 * <p><b>DANGER, Will Robinson. DANGER!!!</b></p>
 *
 * @author rk@post.harvard.edu
 */

@Path("/sys")
public class SystemResource {

    private static final Logger logger = Logger.getLogger(SystemResource.class.getName());

    /**
     * <p>Sheep stuff</p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> sys</div>
     *
     * @return Tells you how many sheep we have in our bank.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response sayBaaah(@Context HttpServletRequest request) {
        try {
            return RestUtilities.make200OkResponse("No sheep here.\n");
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
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
            return RestUtilities.make404ResourceNotFoundResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
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
            return RestUtilities.make404ResourceNotFoundResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
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
            BlahguaSession.ensureAdmin(request);
            return Response.ok(BlahguaSession.getSessionInfo(request)).build();
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    /**
     * <p>Use this method to refresh the local cache.</p>
     * <b>This method is for development only and will be removed from
     * public access once we launch.</b>
     * <p/>
     * <div><b>METHOD:</b> GET </div>
     * <div><b>URL:</b> sys/refresh/{pass}</div>
     *
     * @return Returns http code 200 (ACCEPTED).
     */
    @POST
    @Path("/refresh/{pass}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response refreshCaches(@PathParam("pass") String pass, @Context HttpServletRequest request) {
        // TODO dangerous (remove as soon as feasible) -- used by DB reset sequence during initial development
        try {
            if (!pass.equals("kwfew303bf3sss")) {
                Response.status(Response.Status.FORBIDDEN).build();
            }
            BlahManager.getInstance().refreshCaches();
            GroupManager.getInstance().maybeRefreshGroupCache(true);
            logger.info("Refreshed blah manager and group manager local caches");
            return RestUtilities.make202AcceptedResponse();
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

//    @GET
//    @Path("/memcached/{enable}/{pass}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response setMemcachedEnable(@PathParam("enable") boolean enable, @PathParam("pass") String pass, @Context HttpServletRequest request) {
//        try {
//            if (!pass.equals("kwfew303bf3sss")) {
//                Response.status(Response.Status.FORBIDDEN).build();
//            }
////            SystemManager.getInstance().setMemcachedEnable(enable);
//            final Map<String, Object> map = new HashMap<String, Object>(1);
//            map.put("newState", enable);
//            return RestUtilities.make200OkResponse(map);
////        } catch (SystemErrorException e) {
////            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
//        } catch (Exception e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
//        }
//    }
}
