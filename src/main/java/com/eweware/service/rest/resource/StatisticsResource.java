package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.InvalidAuthorizedStateException;
import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.mgr.StatisticsManager;
import main.java.com.eweware.service.base.mgr.SystemManager;
import main.java.com.eweware.service.rest.RestUtilities;
import main.java.com.eweware.service.rest.session.BlahguaSession;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * <p>Statistics-specific API methods.</p>
 * <div>Note that some methods require authentication (previous login) to be accessed.</div>
 *
 * @author rk@post.harvard.edu
 *         Date: 10/1/12 Time: 11:20 AM
 */
@Path("/stats")
public class StatisticsResource {

    private static final String GET_COMMENT_DEMOGRAPHICS_OPERATION = "getCommentDemographics";
    private static final String GET_BLAH_DEMOGRAPHICS_OPERATION = "getBlahDemographics";
    private static final String GET_GROUP_DEMOGRAPHICS_OPERATION = "getGroupDemographics";

    private SystemManager systemManager;

    private SystemManager getSystemManager() throws SystemErrorException {
        if (systemManager == null) {
            systemManager = getSystemManager();
        }
        return systemManager;
    }

    /**
     * <p>Use this method to obtain demographics for a group.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p><b>TODO: this API needs to be better documented.</b></p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL: stats/demo/group/blahs/{groupId}</b> </div>
     *
     * @param groupId <i>Path Parameter:</i> The group's id.
     * @return A JSON entity with the group demographics.
     */
    @GET
    @Path("/demo/group/blahs/{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupDemographics(
            @PathParam("groupId") String groupId,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            final Response response = RestUtilities.make200OkResponse(StatisticsManager.getInstance().getGroupDemographics(groupId));
            getSystemManager().setResponseTime(GET_GROUP_DEMOGRAPHICS_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(request, e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    /**
     * <p>Use this method to obtain demographics for blahs in general.</p>
     * <p><b>TODO: this API needs to be better documented.</b></p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> stats/demo/blahs</div>
     *
     * @param type <i>Query Parameter:</i> Optional. Filters by blah type id.
     * @return Returns a JSON entity with the blah's demographics.
     */
    @GET
    @Path("/demo/blahs")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlahDemographics(
            @QueryParam("type") boolean type,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            final Response response = RestUtilities.make200OkResponse(StatisticsManager.getInstance().getBlahDemographics(type));
            getSystemManager().setResponseTime(GET_BLAH_DEMOGRAPHICS_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    /**
     * <p>Use this method to get demographics for comments in general.</p>
     * <p><b>TODO: this API needs to be better documented.</b></p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> stats/demo/comments</div>
     *
     * @return Returns a JSON entity with the demographics for the comment.
     */
    @GET
    @Path("/demo/comments")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCommentDemographics(@Context HttpServletRequest request) {
        try {
            BlahguaSession.ensureAuthenticated(request);
            final long start = System.currentTimeMillis();
            final Response response = RestUtilities.make200OkResponse(StatisticsManager.getInstance().getCommentDemographics());
            getSystemManager().setResponseTime(GET_COMMENT_DEMOGRAPHICS_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }
}
