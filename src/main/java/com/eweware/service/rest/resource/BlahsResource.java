package main.java.com.eweware.service.rest.resource;

import com.amazonaws.services.ec2.model.UserIdGroupPair;
import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.payload.BlahInfoPayload;
import main.java.com.eweware.service.base.payload.BlahPayload;
import main.java.com.eweware.service.mgr.BlahManager;
import main.java.com.eweware.service.mgr.SystemManager;
import main.java.com.eweware.service.rest.RestUtilities;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author rk@post.harvard.edu
 */

@Path("/blahs")
public class BlahsResource {

    private static final String CREATE_BLAH_OPERATION = "createBlah";
    private static final String UPDATE_BLAH_OPERATION = "updateBlah";
    private static final String DELETE_BLAH_OPERATION = "deleteBlah";
    private static final String GET_BLAH_TYPES_OPERATION = "getBlahTypes";
    private static final String GET_BLAH_BY_ID_OPERATION = "getBlahById";
    private static final String GET_BLAHS_OPERATION = "getBlahs";

    /**
     * Creates a request.
     *
     * @param request The request payload
     * @param uri  URI to use to build the http Location header
     * @return BlahPayload The created request with the new _id
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBlah(BlahPayload request, @Context UriInfo uri) {
        try {
            final long start = System.currentTimeMillis();
            request = BlahManager.getInstance().createBlah(LocaleId.en_us, request);
            final Response response = RestUtilities.makeCreatedResourceResponse(request, new URI(uri.getAbsolutePath() + request.getId()));
            SystemManager.getInstance().setResponseTime(CREATE_BLAH_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.makeResourceNotFoundResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.makeStateConflictResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (URISyntaxException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    @PUT
    @Path("/{blahId}/pollVote/{userId}/{pollOptionIndex}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response pollVote(@PathParam("blahId") String blahId,
                             @PathParam("userId") String userId,
                             @PathParam("pollOptionIndex") Integer index) {
        try {
            BlahManager.getInstance().pollVote(LocaleId.en_us, blahId, userId, index);
            return RestUtilities.makeOKNoContentResponse();
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.makeStateConflictResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.makeResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    @GET
    @Path("/{blahId}/pollVote/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPollVoteInfo(@PathParam("blahId") String blahId,
                                    @PathParam("userId") String userId) {
        try {
            final BlahInfoPayload info = BlahManager.getInstance().getPollVoteInfo(LocaleId.en_us, blahId, userId);
            return RestUtilities.makeOkResponse(info);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    /**
     * Updates a blah's view, open, and or vote counts.
     * Any other update requests in the payload are ignored.
     * @param blah   The blah payload with the fields to update.
     * @param blahId The blah's id
     * @return An update response without content.
     */
    @PUT
    @Path("/{blahId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateBlahVoteViewOrOpens(
            BlahPayload blah,
            @PathParam("blahId") String blahId,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
//            blahId = BlahguaSession.getInternalBlahId(blahId, request.getSession(true));
            blah.setId(blahId);
            BlahManager.getInstance().updateBlahVoteViewOrOpens(LocaleId.en_us, blah);
            final Response response = RestUtilities.makeOKNoContentResponse();
            SystemManager.getInstance().setResponseTime(UPDATE_BLAH_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.makeResourceNotFoundResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.makeStateConflictResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    /**
     * Returns an array of blah type docs.
     *
     * @return List<BlahTypePayload> Blah types
     */
    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlahTypes() {
        try {
            final long start = System.currentTimeMillis();
            final Response response = RestUtilities.makeOkResponse(BlahManager.getInstance().getBlahTypes(LocaleId.en_us));
            SystemManager.getInstance().setResponseTime(GET_BLAH_TYPES_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } finally {

        }
    }

    /**
     * Returns information about a blah.
     * If a userId is provided, the blah's data will include blah stats
     * for the specified user instead of stats for the blah itself.
     *
     * @param blahId         The blah's id
     * @param userId         (Optional): a userId
     * @param stats          (Optional): if true, return statistics with blah
     * @param statsStartDate If stats=true, return statistics starting with this date
     * @param statsEndDate   If stats=true, return statistics ending with this date
     * @return List<BlahPayload> An array of blah docs.
     */
    @GET
    @Path("/{blahId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlahById(@PathParam("blahId") String blahId,
                                @QueryParam("userId") final String userId,
                                @QueryParam("stats") final boolean stats,
                                @QueryParam("s") final String statsStartDate, // format is yymmdd (e.g., August 27, 2012 is 120827)
                                @QueryParam("e") final String statsEndDate,   // format is yymmdd (e.g., August 27, 2012 is 120827)
                                @QueryParam("sc") final boolean saveContext,
                                @Context HttpServletRequest req) {
        try {
            final long start = System.currentTimeMillis();
//            blahId = BlahguaSession.getInternalBlahId(blahId, req.getSession(true));
            final Response response = RestUtilities.makeOkResponse(BlahManager.getInstance().getBlahById(LocaleId.en_us, blahId, userId, stats, statsStartDate, statsEndDate));
            SystemManager.getInstance().setResponseTime(GET_BLAH_BY_ID_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (ResourceNotFoundException e) {
            return RestUtilities.makeResourceNotFoundResponse(e);
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    /**
     * Returns information about the blahs.
     * If a userId is provided, the blah's data will include blah stats
     * for the specified user instead of stats for the blah itself.
     * If an authorId is provided, only blah's data for the author
     * will be returned.
     *
     * @param start         (Optional): The starting index to fetch when paging
     * @param count         (Optional): The max number of blahs to fetch
     * @param sortFieldName (Optional): name of the field to sort on
     * @param authorId      (Optional): The blah author's userId
     * @param userId        (Optional): The id of the user for whom blah stats will be returned
     * @param typeId        (Optional): The blah's type id. If not given, all types will be returned.
     * @return List<BlahPayload> An array of blahs
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlahs(@QueryParam("start") Integer start,
                             @QueryParam("count") Integer count,
                             @QueryParam("sort") String sortFieldName,
                             @QueryParam("authorId") String authorId,
                             @QueryParam("userId") String userId,
                             @QueryParam("typeid") String typeId) {
        // TODO XXX must translate every
        try {
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.makeOkResponse(BlahManager.getInstance().getBlahs(LocaleId.en_us, userId, authorId, typeId, start, count, sortFieldName));
            SystemManager.getInstance().setResponseTime(GET_BLAHS_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }
}



/**
 * Deletes the blah.
 *
 * @param blahId The blah's id
 * @return The response without content.
 */
//    @DELETE
//    @Path("/{blahId}")
//    public Response deleteBlah(@PathParam("blahId") String blahId,
//                               @Context HttpServletRequest request) {
//        try {
//            final long start = System.currentTimeMillis();
//            blahId = BlahguaSession.getInternalBlahId(blahId, request.getSession(true));
//            BlahManager.getInstance().deleteBlah(LocaleId.en_us, blahId);
//            final Response response = RestUtilities.makeOKNoContentResponse();
//            SystemManager.getInstance().setResponseTime(DELETE_BLAH_OPERATION, (System.currentTimeMillis() - start));
//            return response;
//        } catch (InvalidRequestException e) {
//            return RestUtilities.makeInvalidRequestResponse(e);
//        } catch (SystemErrorException e) {
//            return RestUtilities.makeAndLogSystemErrorResponse(e);
//        } catch (RuntimeException e) {
//            return RestUtilities.makeAndLogSystemErrorResponse(e);
//        }
//    }