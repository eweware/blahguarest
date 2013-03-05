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

/**
 * <p>Blah-specific API methods.</p>
 * <div>Note that some methods require authentication (previous login) to be accessed.</div>
 *
 * @author rk@post.harvard.edu
 *         goodbye
 */
@Path("/blahs")
public class BlahsResource {

    private static final String CREATE_BLAH_OPERATION = "createBlah";
    private static final String UPDATE_BLAH_OPERATION = "updateBlah";
    private static final String GET_BLAH_TYPES_OPERATION = "getBlahTypes";
    private static final String GET_BLAH_BY_ID_OPERATION = "getBlahById";
    private static final String GET_BLAHS_OPERATION = "getBlahs";

    /**
     * <p>Creates a blah.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> blahs</div>
     *
     * @param entity The request entity. Requires a JSON entity (a BlahPayload) with an
     *               author id, a groupId, a blah type id, and the blah's tagline. Body text
     *               may optionally be supplied.
     * @return BlahPayload The created request with the new blah id
     *         If there is an error in the request, returns status 400.
     *         If the referenced blah or author can't be found, returns status 404.
     *         If a conflict would arise from satisfying the request, returns status 409.
     * @see main.java.com.eweware.service.base.store.dao.BlahDAOConstants
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
            BlahguaSession.ensureAuthenticated(request, true);
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
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Use this method to register a vote for one of the poll options or a blah.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> PUT</div>
     * <div><b>URL:</b> blahs/{blahId}/pollVote/{userId}/{pollOptionIndex}</div>
     * <p/>
     * METHOD: PUT
     * URL: blahs/{blahId}/pollVote/{userId}/{pollOptionIndex}
     *
     * @param blahId <i>Path Parameter</i>. The poll blah id
     * @param userId <i>Path Parameter</i>. The user id
     * @param index  <i>Path Parameter</i>. The poll option index.
     * @return If successful, returns status 204 (OK NO CONTENTS) without
     *         a content entity.
     *         If the user is not authorized to vote, returns status 401.
     *         If there is an error in the request, returns status 400.
     *         If the referenced blah or author can't be found, returns status 404.
     *         If a conflict would arise from satisfying the request, returns status 409.
     * @see main.java.com.eweware.service.base.store.dao.BlahDAOConstants
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
            BlahguaSession.ensureAuthenticated(request, true);
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
     * of the vote.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> blahs/{blahId}/pollVote/{userId}</div>
     *
     * @param blahId <i>Path Parameter</i>. The poll blah id
     * @param userId <i>Path Parameter</i>. The user id
     * @return An http status 200 response with the poll option index on which the
     *         user has voted and the time of the vote. Returns empty object
     *         if the user has not voted on this poll.
     *         If the user is not authorized to vote, returns status 401.
     * @see main.java.com.eweware.service.base.store.dao.UserBlahInfoDAOConstants
     */
    @GET
    @Path("/{blahId}/pollVote/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPollVoteInfo(@PathParam("blahId") String blahId,
                                    @PathParam("userId") String userId,
                                    @Context HttpServletRequest request) {
        try {
            BlahguaSession.ensureAuthenticated(request, true);
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
     * Any other update requests in the payload are ignored.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> PUT</div>
     * <div><b>URL:</b> blahs/{blahId}</div>
     *
     * @param entity A JSON entity (a BlahPayload) with one or more of the following
     *               fields to update: vote, views, opens. (Other fields are ignored
     *               and might result in a status 400 response.)
     * @param blahId <i>Path Parameter</i>. The blah's id
     * @return An update response without content.
     *         If the user is not authorized to vote, returns status 401.
     *         If there is an error in the request, returns status 400.
     *         If the referenced blah or author can't be found, returns status 404.
     *         If a conflict would arise from satisfying the request, returns status 409.
     * @see main.java.com.eweware.service.base.store.dao.BlahDAOConstants
     */
    @PUT
    @Path("/{blahId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateBlahVoteViewOrOpens(
            BlahPayload entity,
            @PathParam("blahId") String blahId,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request, true);
            entity.setId(blahId);
            BlahManager.getInstance().updateBlahVoteViewOrOpens(LocaleId.en_us, entity);
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
     * <p>This method does not require any parameters.</p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> blahs/types</div>
     *
     * @return An http status of 200 with a JSON entity that is a list
     *         of BlahTypePayload entities.
     * @see main.java.com.eweware.service.base.store.dao.BlahTypeDAOConstants
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
     * <p>Returns information about a blah.</p>
     * <p>If a userId is provided, the blah's data will include blah stats
     * for the specified user instead of stats for the blah itself.</p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> blahs/{blahId}</div>
     *
     * @param blahId         <i>Path Parameter</i>. The blah's id
     * @param userId         <i>Query Parameter:</i> Optional. a userId
     * @param stats          <i>Query Parameter:</i> Optional. if true, return statistics with blah
     * @param statsStartDate <i>Query Parameter:</i> Optional. If stats=true, return statistics starting with this date
     * @param statsEndDate   <i>Query Parameter:</i> Optional. If stats=true, return statistics ending with this date
     * @return Returns an http status of 200 and a JSON entity containing the blah information.
     *         If the blah doesn't exist, returns status 404.
     *         If there is an error in the request, returns status 400.
     *         On error, a JSON entity will be returned containing detailed error information.
     * @see main.java.com.eweware.service.base.store.dao.BlahDAOConstants
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

    /**
     * Returns information about the blahs.
     * If a userId is provided, the blah's data will include blah stats
     * for the specified user instead of stats for the blah itself.
     * If an authorId is provided, only blah's data for the author
     * will be returned.
     * <p/>
     * <p></p>
     * <div><b>METHOD:</b> </div>
     * <div><b>URL:</b> </div>
     *
     * @param start         (Optional): The starting index to fetch when paging
     * @param count         (Optional): The max number of blahs to fetch
     * @param sortFieldName (Optional): name of the field to sort on
     * @param authorId      (Optional): The blah author's userId
     * @param userId        (Optional): The id of the user for whom blah stats will be returned
     * @param typeId        (Optional): The blah's type id. If not given, all types will be returned.
     * @return List<BlahPayload> An array of blahs
     * @deprecated
     */
    @Deprecated
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlahs(@QueryParam("start") Integer start,
                             @QueryParam("count") Integer count,
                             @QueryParam("sort") String sortFieldName,
                             @QueryParam("authorId") String authorId,
                             @QueryParam("userId") String userId,
                             @QueryParam("typeid") String typeId,
                             @Context HttpServletRequest req) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(req, true);
            final Response response = RestUtilities.make200OkResponse(BlahManager.getInstance().getBlahs(LocaleId.en_us, userId, authorId, typeId, start, count, sortFieldName));
            SystemManager.getInstance().setResponseTime(GET_BLAHS_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
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