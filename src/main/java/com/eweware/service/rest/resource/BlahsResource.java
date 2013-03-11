package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.payload.UserBlahInfoPayload;
import main.java.com.eweware.service.base.payload.BlahPayload;
import main.java.com.eweware.service.mgr.BlahManager;
import main.java.com.eweware.service.mgr.SystemManager;
import main.java.com.eweware.service.mgr.type.PredictionExpirationType;
import main.java.com.eweware.service.mgr.type.PredictionVote;
import main.java.com.eweware.service.rest.RestUtilities;
import main.java.com.eweware.service.rest.session.BlahguaSession;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Map;

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
    private static final String GET_BLAH_AUTHOR_OPERATION = "getBlahAuthor";
    private static final String PREDICTION_VOTE_OPERATION = "predictVote";
    private static final String POLL_VOTE_OPERATION = "pollVote";

    private static BlahManager blahManager;
    private static SystemManager systemManager;


    /**
     * <p>Returns user information about the blah author</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> blahs/author</div>
     *
     * @param entity The request entity. Requires a JSON entity with an
     *               field named 'i' whose value is the blah id.
     * @return Returns an http status 200 with the author's data
     *         If there is an error in the request, returns status 400.
     *         If the referenced blah or author can't be found, returns status 404.
     *         If a conflict would arise from satisfying the request, returns status 409.
     * @see main.java.com.eweware.service.base.store.dao.UserDAOConstants
     */
    @POST
    @Path("/author")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlahAuthor(
            Map<String, String> entity,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            final Response response = RestUtilities.make200OkResponse(getBlahManager().getAuthorFromBlah(LocaleId.en_us, entity.get("i")));
            getSystemManager().setResponseTime(GET_BLAH_AUTHOR_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

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
            final String authorId = BlahguaSession.ensureAuthenticated(request, true);
            entity = getBlahManager().createBlah(LocaleId.en_us, authorId, entity);
            final Response response = RestUtilities.make201CreatedResourceResponse(entity, new URI(uri.getAbsolutePath() + entity.getId()));
            getSystemManager().setResponseTime(CREATE_BLAH_OPERATION, (System.currentTimeMillis() - start));
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
     * <div><b>URL:</b> blahs/{blahId}/pollVote/{pollOptionIndex}</div>
     * <p/>
     *
     * @param blahId <i>Path Parameter</i>. The poll blah id
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
    @Path("/{blahId}/pollVote/{pollOptionIndex}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response pollVote(@PathParam("blahId") String blahId,
                             @PathParam("pollOptionIndex") Integer index,
                             @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            getBlahManager().pollVote(LocaleId.en_us, blahId, userId, index);
            getSystemManager().setResponseTime(POLL_VOTE_OPERATION, (System.currentTimeMillis() - start));
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
     * <div><b>URL:</b> blahs/{blahId}/pollVote</div>
     *
     * @param blahId <i>Path Parameter</i>. The poll blah id
     * @return An http status 200 response with the poll option index on which the
     *         user has voted and the time of the vote. Returns empty object
     *         if the user has not voted on this poll.
     *         If the user is not authorized to vote, returns status 401.
     * @see main.java.com.eweware.service.base.payload.UserBlahInfoPayload
     */
    @GET
    @Path("/{blahId}/pollVote")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPollVoteInfo(@PathParam("blahId") String blahId,
                                    @Context HttpServletRequest request) {
        try {
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            final UserBlahInfoPayload info = getBlahManager().getPollVoteInfo(LocaleId.en_us, blahId, userId);
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
     * <p>Use this method to set a user's vote into a prediction blah.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p>This supports the pre-expiration vote and the post-expiration vote.</p>
     * <p/>
     * <div><b>METHOD:</b> PUT</div>
     * <div><b>URL:</b> blahs/{blahId}/predicts</div>
     * <p/>
     *
     * @param entity An entity containing the voting parameters. Two fields
     *               named 't' and 'v' are expected:
     *               't' := this field specifies whether the vote is a pre-expiration
     *               vote (i.e., the user is agreeing, disagreeing, or saying that
     *               the prediction is unclear) or post-expiration (i.e., the user
     *               is asserting that the prediction is correct, incorrect, or unclear.
     *               The value of the 't' field may be 'pre' (for pre-expiration) or
     *               'post' (for post expiration).
     *               'v' := specifies the user's vote: possible values are
     *               'y' (depending on the value of 't', user either agrees with or asserts that the prediction is correct), or
     *               'n' (depending on the value of 't', user either disagrees with or asserts that the prediction is incorrect), or
     *               'u' (depending on the value of 't', user asserts that either the prediction is unclear or that the resolution is unclear).
     * @param blahId The blah's id
     * @return If successful, returns status 204 (OK NO CONTENTS) without a content entity.
     *         If the user is not authorized to vote, returns status 401.
     *         If there is an error in the request, returns status 400.
     *         If the referenced blah or author can't be found, returns status 404.
     *         If a conflict (e.g., the prediction expired and the client is trying to
     *         make a pre-expiration vote) would arise from satisfying the request, returns status 409
     * @see PredictionExpirationType
     * @see PredictionVote
     */
    @PUT
    @Path("/{blahId}/predicts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response userPredictionVote(
            Map<String, Object> entity,
            @PathParam("blahId") String blahId,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            final String preOrPostExpiration = (String) entity.get(PredictionExpirationType.PREDICTION_VOTE_TYPE_FIELD_NAME);
            final String vote = (String) entity.get(PredictionVote.PREDICTION_VOTE_FIELD_NAME);
            getBlahManager().predictionVote(userId, blahId, preOrPostExpiration, vote);
            getSystemManager().setResponseTime(PREDICTION_VOTE_OPERATION, (System.currentTimeMillis() - start));
            return RestUtilities.make204OKNoContentResponse();
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Returns the prediction votes from the user.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> blahs/{blahId}/predicts</div>
     *
     * @param blahId <i>Path Parameter</i>. The prediction blah id
     * @return An http status 200 response with a JSON entity containing
     *         the user's votes, if any, for the prediction. The appropriate fields of
     *         this entity are specified in UserBlahInfoPayload.
     *         If the user is not authorized to vote, returns status 401.
     * @see main.java.com.eweware.service.base.payload.UserBlahInfoPayload
     */
    @GET
    @Path("/{blahId}/predicts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPredictionVote(
            @PathParam("blahId") String blahId,
            @Context HttpServletRequest request) {
        try {
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            return RestUtilities.make200OkResponse(getBlahManager().getPredictionVoteInfo(userId, blahId));
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (SystemErrorException e) {
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
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            entity.setAuthorId(userId);
            entity.setId(blahId);
            getBlahManager().updateBlahVoteViewOrOpens(LocaleId.en_us, entity);
            final Response response = RestUtilities.make204OKNoContentResponse();
            getSystemManager().setResponseTime(UPDATE_BLAH_OPERATION, (System.currentTimeMillis() - start));
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
            final Response response = RestUtilities.make200OkResponse(getBlahManager().getBlahTypes(LocaleId.en_us));
            getSystemManager().setResponseTime(GET_BLAH_TYPES_OPERATION, (System.currentTimeMillis() - start));
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
     * <p>If there's a user session in progress, the blah's data will include blah stats
     * for the user.</p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> blahs/{blahId}</div>
     *
     * @param blahId         <i>Path Parameter</i>. The blah's id
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
                                @QueryParam("stats") final boolean stats,
                                @QueryParam("s") final String statsStartDate, // format is yymmdd (e.g., August 27, 2012 is 120827)
                                @QueryParam("e") final String statsEndDate,   // format is yymmdd (e.g., August 27, 2012 is 120827)
                                @QueryParam("sc") final boolean saveContext,
                                @Context HttpServletRequest req) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.getUserId(req);
            final Response response = RestUtilities.make200OkResponse(getBlahManager().getBlahById(LocaleId.en_us, blahId, userId, stats, statsStartDate, statsEndDate));
            getSystemManager().setResponseTime(GET_BLAH_BY_ID_OPERATION, (System.currentTimeMillis() - start));
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
     * <p>By default, returns blahs for the logged-in  user.</p>
     * <p>If an author id is provided, it returns the blahs created by the specified author instead
     * of by the user.</p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> blahs</div>
     *
     * @param start         (Optional): The starting index to fetch when paging
     * @param count         (Optional): The max number of blahs to fetch
     * @param sortFieldName (Optional): name of the field to sort on
     * @param authorId      (Optional): The blah author's userId
     * @param typeId        (Optional): The blah's type id. If not given, all types will be returned.
     * @return An array of blahs.
     * @see main.java.com.eweware.service.base.store.dao.BlahDAOConstants
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlahs(@QueryParam("start") Integer start,
                             @QueryParam("count") Integer count,
                             @QueryParam("sort") String sortFieldName,
                             @QueryParam("authorId") String authorId,
                             @QueryParam("typeid") String typeId,
                             @Context HttpServletRequest req) {
        try {
            final long s = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(req, true);
            final Response response = RestUtilities.make200OkResponse(getBlahManager().getBlahs(LocaleId.en_us, userId, authorId, typeId, start, count, sortFieldName));
            getSystemManager().setResponseTime(GET_BLAHS_OPERATION, (System.currentTimeMillis() - s));
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

    private BlahManager getBlahManager() throws SystemErrorException {
        if (blahManager == null) {
            blahManager = BlahManager.getInstance();
        }
        return blahManager;
    }

    private SystemManager getSystemManager() throws SystemErrorException {
        if (systemManager == null) {
            systemManager = SystemManager.getInstance();
        }
        return systemManager;
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
//            getBlahManager(.deleteBlah(LocaleId.en_us, blahId);
//            final Response response = RestUtilities.make204OKNoContentResponse();
//            getSystemManager().setResponseTime(DELETE_BLAH_OPERATION, (System.currentTimeMillis() - start));
//            return response;
//        } catch (InvalidRequestException e) {
//            return RestUtilities.make400InvalidRequestResponse(e);
//        } catch (SystemErrorException e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(e);
//        } catch (Exception e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(e);
//        }
//    }