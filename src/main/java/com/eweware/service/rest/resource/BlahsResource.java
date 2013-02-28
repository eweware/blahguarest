package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.payload.BlahInfoPayload;
import main.java.com.eweware.service.base.payload.BlahPayload;
import main.java.com.eweware.service.mgr.BlahManager;
import main.java.com.eweware.service.mgr.SystemManager;
import main.java.com.eweware.service.rest.RestUtilities;
import main.java.com.eweware.service.rest.session.BlahguaSession;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Hello there
 * @author rk@post.harvard.edu
 * goodbye
 */
@Path("/blahs")
public class BlahsResource {

    private static final String CREATE_BLAH_OPERATION = "createBlah";
    private static final String UPDATE_BLAH_OPERATION = "updateBlah";
    private static final String GET_BLAH_TYPES_OPERATION = "getBlahTypes";
    private static final String GET_BLAH_BY_ID_OPERATION = "getBlahById";
    private static final String GET_BLAHS_OPERATION = "getBlahs";

    /**
     * <p>Creates a blah. <i>User must be logged in to use this method.</i></p>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> blahs</div>
     * @param entity The request entity. Expects a JSON object with an author id, a groupId, a blah type id,
     *                and the blah's tagline
//     * @param uri  Internal (not supplied by JSON object). URI to use to build the http Location header
     * @return BlahPayload The created request with the new blah id
     * @see BlahPayload
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBlah(
            BlahPayload entity,
            @Context UriInfo uri,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            entity = BlahManager.getInstance().createBlah(LocaleId.en_us, entity);
            final Response response = RestUtilities.make201CreatedResourceResponse(entity, new URI(uri.getAbsolutePath() + entity.getId()));
            SystemManager.getInstance().setResponseTime(CREATE_BLAH_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        }  catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Use this method to register a vote for one of the poll options or a blah.
     *  <i>User must be logged in to use this method.</i></p>
     * <div><b>METHOD:</b> PUT</div>
     * <div><b>URL:</b> blahs/{blahId}/pollVote/{userId}/{pollOptionIndex}</div>
     *
     * METHOD: PUT
     * URL: blahs/{blahId}/pollVote/{userId}/{pollOptionIndex}
     *
     * @param blahId    The poll blah id
     * @param userId    The user id
     * @param index The poll option index.
     * @return  If successful, returns status 204 (OK NO CONTENTS) without
     * a content entity. If the user is not authorized to vote, returns
     * status 401. If the request has an error, returned status 400.
     * If the blah or a poll option doesn't exist, returns status 404.
     * On error, an entity is returned with detailed error information.
     */
    @PUT
    @Path("/{blahId}/pollVote/{userId}/{pollOptionIndex}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response pollVote(@PathParam("blahId") String blahId,
                             @PathParam("userId") String userId,
                             @PathParam("pollOptionIndex") Integer index,
                             @Context HttpServletRequest request) {
        try {
            BlahguaSession.ensureAuthenticated(request);
            BlahManager.getInstance().pollVote(LocaleId.en_us, blahId, userId, index);
            return RestUtilities.make204OKNoContentResponse();
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Returns the poll option for which the user has voted and the time
     * of the vote. <i>User must be logged in to use this method.</i></p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> blahs/{blahId}/pollVote/{userId}</div>
     *
     * @param blahId    The poll blah id
     * @param userId    The user id
     * @return  An http status 200 response with the poll option index on which the
     * user has voted and the time of the vote. Returns empty object
     * if the user has not voted on this poll.
     * @see BlahInfoPayload
     */
    @GET
    @Path("/{blahId}/pollVote/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPollVoteInfo(@PathParam("blahId") String blahId,
                                    @PathParam("userId") String userId,
                                    @Context HttpServletRequest request) {
        try {
            BlahguaSession.ensureAuthenticated(request);
            final BlahInfoPayload info = BlahManager.getInstance().getPollVoteInfo(LocaleId.en_us, blahId, userId);
            return RestUtilities.make200OkResponse(info);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Updates a blah's view, open, and or vote counts.
     * Any other update requests in the payload are ignored. <i>User must be logged in to use this method.</i></p>
     * <div><b>METHOD:</b> </div>
     * <div><b>URL:</b> </div>
     *
     * METHOD: PUT
     * URL: blahs/{blahId}
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
            BlahguaSession.ensureAuthenticated(request);
            blah.setId(blahId);
            BlahManager.getInstance().updateBlahVoteViewOrOpens(LocaleId.en_us, blah);
            final Response response = RestUtilities.make204OKNoContentResponse();
            SystemManager.getInstance().setResponseTime(UPDATE_BLAH_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Returns an array of blah type docs: all the blah types available.</p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> blahs/types</div>
     * @return An http status of 200 with a JSON entity that is a list
     * of blah type payload objects.
     * @see main.java.com.eweware.service.base.payload.BlahTypePayload
     */
    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlahTypes() {
        try {
            final long start = System.currentTimeMillis();
            final Response response = RestUtilities.make200OkResponse(BlahManager.getInstance().getBlahTypes(LocaleId.en_us));
            SystemManager.getInstance().setResponseTime(GET_BLAH_TYPES_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } finally {

        }
    }

    /**
     * <p>Returns information about a blah.
     * If a userId is provided, the blah's data will include blah stats
     * for the specified user instead of stats for the blah itself.</p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> blahs/{blahId}</div>
     *
     * @param blahId         The blah's id
     * @param userId         <b>Query Parameter: </b>(Optional): a userId
     * @param stats           <b>Query Parameter: </b>(Optional): if true, return statistics with blah
     * @param statsStartDate  <b>Query Parameter: </b>If stats=true, return statistics starting with this date
     * @param statsEndDate    <b>Query Parameter: </b>f stats=true, return statistics ending with this date
     * @return Returns an http status of 200 and a JSON entity containing the blah information.
     * If the blah doesn't exist, returns status 404.
     * If there is an error in the request, returns status 400.
     * On error, a JSON entity will be returned containing detailed error information.
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
            final Response response = RestUtilities.make200OkResponse(BlahManager.getInstance().getBlahById(LocaleId.en_us, blahId, userId, stats, statsStartDate, statsEndDate));
            SystemManager.getInstance().setResponseTime(GET_BLAH_BY_ID_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

//    /**
//     * Returns information about the blahs.
//     * If a userId is provided, the blah's data will include blah stats
//     * for the specified user instead of stats for the blah itself.
//     * If an authorId is provided, only blah's data for the author
//     * will be returned.
//     *
//     * <p></p>
//     * <div><b>METHOD:</b> </div>
//     * <div><b>URL:</b> </div>
//     * @param start         (Optional): The starting index to fetch when paging
//     * @param count         (Optional): The max number of blahs to fetch
//     * @param sortFieldName (Optional): name of the field to sort on
//     * @param authorId      (Optional): The blah author's userId
//     * @param userId        (Optional): The id of the user for whom blah stats will be returned
//     * @param typeId        (Optional): The blah's type id. If not given, all types will be returned.
//     * @return List<BlahPayload> An array of blahs
//     * @deprecated
//     */
//    @Deprecated
//    @GET
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getBlahs(@QueryParam("start") Integer start,
//                             @QueryParam("count") Integer count,
//                             @QueryParam("sort") String sortFieldName,
//                             @QueryParam("authorId") String authorId,
//                             @QueryParam("userId") String userId,
//                             @QueryParam("typeid") String typeId) {
//        try {
//            final long s = System.currentTimeMillis();
//            final Response response = RestUtilities.make200OkResponse(BlahManager.getInstance().getBlahs(LocaleId.en_us, userId, authorId, typeId, start, count, sortFieldName));
//            SystemManager.getInstance().setResponseTime(GET_BLAHS_OPERATION, (System.currentTimeMillis() - s));
//            return response;
//        } catch (InvalidRequestException e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(e);
//        } catch (SystemErrorException e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(e);
//        } catch (Exception e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(e);
//        }
//    }
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
//            final Response response = RestUtilities.make204OKNoContentResponse();
//            SystemManager.getInstance().setResponseTime(DELETE_BLAH_OPERATION, (System.currentTimeMillis() - start));
//            return response;
//        } catch (InvalidRequestException e) {
//            return RestUtilities.make400InvalidRequestResponse(e);
//        } catch (SystemErrorException e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(e);
//        } catch (Exception e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(e);
//        }
//    }