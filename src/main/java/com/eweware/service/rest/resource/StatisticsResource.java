package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.mgr.StatisticsManager;
import main.java.com.eweware.service.mgr.SystemManager;
import main.java.com.eweware.service.rest.RestUtilities;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author rk@post.harvard.edu
 *         Date: 10/1/12 Time: 11:20 AM
 */
@Path("/stats")
public class StatisticsResource {

    private static final String GET_COMMENT_DEMOGRAPHICS_OPERATION = "getCommentDemographics";
    private static final String GET_BLAH_DEMOGRAPHICS_OPERATION = "getBlahDemographics";
    private static final String GET_GROUP_DEMOGRAPHICS_OPERATION = "getGroupDemographics";

    @GET
    @Path("/demo/group/blahs/{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupDemographics(@PathParam("groupId") String groupId) {
        try {
            final long start = System.currentTimeMillis();
            final Response response = RestUtilities.makeOkResponse(StatisticsManager.getInstance().getGroupDemographics(groupId));
            SystemManager.getInstance().setResponseTime(GET_GROUP_DEMOGRAPHICS_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestException(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    @GET
    @Path("/demo/blahs")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlahDemographics(@QueryParam("type") boolean type) {
        try {
            final long start = System.currentTimeMillis();
            final Response response = RestUtilities.makeOkResponse(StatisticsManager.getInstance().getBlahDemographics(type));
            SystemManager.getInstance().setResponseTime(GET_BLAH_DEMOGRAPHICS_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    @GET
    @Path("/demo/comments")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCommentDemographics() {
        try {
            final long start = System.currentTimeMillis();
            final Response response = RestUtilities.makeOkResponse(StatisticsManager.getInstance().getCommentDemographics());
            SystemManager.getInstance().setResponseTime(GET_COMMENT_DEMOGRAPHICS_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }
}
