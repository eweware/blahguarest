package com.eweware.service.rest.resource;

import com.eweware.service.base.error.*;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.mgr.SystemManager;
import com.eweware.service.base.payload.BlahPayload;
import com.eweware.service.base.payload.UserBlahInfoPayload;
import com.eweware.service.base.store.dao.UserDAO;
import com.eweware.service.mgr.BlahManager;
import com.eweware.service.mgr.type.PredictionExpirationType;
import com.eweware.service.mgr.type.PredictionVote;
import com.eweware.service.rest.RestUtilities;
import com.eweware.service.rest.session.BlahguaSession;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
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
    private static final String UPDATE_BLAH_STATS_OPERATION = "updateBlahStats";
    private static final String GET_BLAH_TYPES_OPERATION = "getBlahTypes";
    private static final String GET_BLAH_BY_ID_OPERATION = "getBlahById";
    private static final String GET_BLAHS_OPERATION = "getBlahs";
    private static final String GET_BLAH_AUTHOR_OPERATION = "getBlahAuthor";
    private static final String PREDICTION_VOTE_OPERATION = "predictVote";
    private static final String POLL_VOTE_OPERATION = "pollVote";
    private static final String GET_POLL_VOTE_INFO_OPERATION = "getPollVoteInfo";
    private static final String GET_PREDICTION_VOTE_OPERATION = "getPredictionVote";
    private static final String DELETE_BLAH_OPERATION = "deleteBlah";
    private static final String UPDATE_BLAH_OPERATION = "updateBlah";
    private static final String REPORT_BLAH_OPERATION = "reportBlah";

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
     *               field named 'I' whose value is the blah id.
     * @return Returns an http status 200 with the author's data
     *         If there is an error in the request, returns status 400.
     *         If the referenced blah or author can't be found, returns status 404.
     *         If a conflict would arise from satisfying the request, returns status 409.
     * @see com.eweware.service.base.store.dao.UserDAOConstants
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
            final Response response = RestUtilities.make200OkResponse(getBlahManager().getAuthorFromBlah(LocaleId.en_us, entity.get("I")));
            getSystemManager().setResponseTime(GET_BLAH_AUTHOR_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(request, e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(request, e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
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
     * @see com.eweware.service.base.store.dao.BlahDAOConstants
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

            entity = getBlahManager().createAdminBlah(LocaleId.en_us, authorId, entity);
            final Response response = RestUtilities.make201CreatedResourceResponse(entity, new URI(uri.getAbsolutePath() + entity.getId()));
            getSystemManager().setResponseTime(CREATE_BLAH_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(request, e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(request, e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(request, e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
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
     * @see com.eweware.service.base.store.dao.BlahDAOConstants
     */
    @POST
    @Path("/new")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewBlah(
            BlahPayload entity,
            @Context UriInfo uri,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String authorId = BlahguaSession.ensureAuthenticated(request, true);
            if (!BlahguaSession.isAuthenticatedClient(request))
                throw new InvalidAuthorizedStateException();
            entity = getBlahManager().createBlah(LocaleId.en_us, authorId, entity);
            final Response response = RestUtilities.make201CreatedResourceResponse(entity, new URI(uri.getAbsolutePath() + entity.getId()));
            getSystemManager().setResponseTime(CREATE_BLAH_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(request, e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(request, e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(request, e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
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
     * @see com.eweware.service.base.store.dao.BlahDAOConstants
     */
    @PUT
    @Path("/{blahId}/pollVote/{pollOptionIndex}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response pollVote(@PathParam("blahId") String blahId,
                             @PathParam("pollOptionIndex") Long index,
                             @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            getBlahManager().pollVote(LocaleId.en_us, blahId, userId, index);
            final Response response = RestUtilities.make204OKNoContentResponse();
            getSystemManager().setResponseTime(POLL_VOTE_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(request, e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(request, e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(request, e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
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
     * @see com.eweware.service.base.payload.UserBlahInfoPayload
     */
    @GET
    @Path("/{blahId}/pollVote")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPollVoteInfo(@PathParam("blahId") String blahId,
                                    @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            final UserBlahInfoPayload info = getBlahManager().getPollVoteInfo(LocaleId.en_us, blahId, userId);
            final Response response = RestUtilities.make200OkResponse(info);
            getSystemManager().setResponseTime(GET_POLL_VOTE_INFO_OPERATION, (System.currentTimeMillis() - start));
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
            final Response response = RestUtilities.make204OKNoContentResponse();
            getSystemManager().setResponseTime(PREDICTION_VOTE_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(request, e);
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
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
     * @see com.eweware.service.base.payload.UserBlahInfoPayload
     */
    @GET
    @Path("/{blahId}/predicts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPredictionVote(
            @PathParam("blahId") String blahId,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            final Response response = RestUtilities.make200OkResponse(getBlahManager().getPredictionVoteInfo(userId, blahId));
            getSystemManager().setResponseTime(GET_PREDICTION_VOTE_OPERATION, (System.currentTimeMillis() - start));
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
     * <p>Updates a blah's view, open, and or promotion counts.
     * Any other update requests in the payload are ignored.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> PUT</div>
     * <div><b>URL:</b> blahs/{blahId}</div>
     *
     * @param entity A JSON entity with one or more of the following
     *               fields to update: vote, views, opens. (Other fields are ignored
     *               and might result in a status 400 response.)
     * @param blahId <i>Path Parameter</i>. The blah's id
     * @return An update response without content.
     *         If the user is not authorized to vote, returns status 401.
     *         If there is an error in the request, returns status 400.
     *         If the referenced blah or author can't be found, returns status 404.
     *         If a conflict would arise from satisfying the request, returns status 409.
     * @see com.eweware.service.base.store.dao.BlahDAOConstants
     */
    @PUT
    @Path("/{blahId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateBlah(
            BlahPayload entity,
            @PathParam("blahId") String blahId,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.getUserId(request);
            if (userId != null) { // authenticated user
                entity.setAuthorId(userId);
                getBlahManager().updateBlah(LocaleId.en_us, entity, blahId);
            }
            final Response response = RestUtilities.make204OKNoContentResponse();
            getSystemManager().setResponseTime(UPDATE_BLAH_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(request, e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(request, e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }


    /**
     * <p>Updates a blah's view, open, and or promotion counts.
     * Any other update requests in the payload are ignored.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> PUT</div>
     * <div><b>URL:</b> blahs/{blahId}</div>
     *
     * @param entity A JSON entity with one or more of the following
     *               fields to update: vote, views, opens. (Other fields are ignored
     *               and might result in a status 400 response.)
     * @param blahId <i>Path Parameter</i>. The blah's id
     * @return An update response without content.
     *         If the user is not authorized to vote, returns status 401.
     *         If there is an error in the request, returns status 400.
     *         If the referenced blah or author can't be found, returns status 404.
     *         If a conflict would arise from satisfying the request, returns status 409.
     * @see com.eweware.service.base.store.dao.BlahDAOConstants
     */
    @PUT
    @Path("/{blahId}/stats")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateBlahPromotionViewOrOpens(
            BlahPayload entity,
            @PathParam("blahId") String blahId,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.getUserId(request);
            if (userId != null) { // authenticated user
                entity.setAuthorId(userId);
                getBlahManager().updateBlahPromotionViewOrOpens(LocaleId.en_us, entity, blahId);
            } else { // anonymous user
                getBlahManager().updateBlahViewsOrOpensByAnonymousUser(LocaleId.en_us, entity, blahId);
            }
            final Response response = RestUtilities.make204OKNoContentResponse();
            getSystemManager().setResponseTime(UPDATE_BLAH_STATS_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(request, e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(request, e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }



    /**
     * <p>Updates view and/or open counts for a set of blahs.</p>
     *
     * <div><b>METHOD: </b>PUT</div>
     * <div><b>URL: </b>blahs/counts</div>
     *
     * @param entity A JSON entity containing a map. The following key values
     *               are accepted: 'V' and 'O' (letter O). The value of either
     *               key must be a map from a blah id to an integer with the
     *               corresponding count increment. For example, {'V': {{'someBlahId': 1},
     *               {'someOtherBlahId': 2}}, 'O': {{'aBlahId': 2}}}
     * @return Returns http status 202 (ACCEPTED) on success.
     * Returns http 404 (NOT FOUND) if a user is in session and for some reason the user doesn't exist
     * Returns http 400 if the request is invalid (e.g., there are no view or open updates.
     */
    @PUT
    @Path("/counts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setBlahCounts(
            Map<String, Map<String, Long>> entity,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.getUserId(request);
            final Map<String, Long> viewsArray = entity.get(BlahPayload.VIEWS);
            final Map<String, Long> opensArray = entity.get(BlahPayload.OPENS);
            getBlahManager().updateBlahCounts(LocaleId.en_us, userId, viewsArray, opensArray);
            final Response response = RestUtilities.make202AcceptedResponse();
            getSystemManager().setResponseTime(UPDATE_BLAH_STATS_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(request, e);
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }  catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
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
     * @see com.eweware.service.base.store.dao.BlahTypeDAOConstants
     */
    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlahTypes(@Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final Response response = RestUtilities.make200OkResponse(getBlahManager().getBlahTypes(LocaleId.en_us));
            getSystemManager().setResponseTime(GET_BLAH_TYPES_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
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
     * @param blahId <i>Path Parameter</i>. The blah's id
     * @param stats  <i>Query Parameter:</i> Optional. if true, return statistics with blah
     * @param s      <i>Query Parameter:</i> Optional. If stats=true, return statistics starting with this date. Format is yymmdd (e.g., August 27, 2012 is 120827).
     * @param e      <i>Query Parameter:</i> Optional. If stats=true, return statistics ending with this date. Format is yymmdd (e.g., August 27, 2012 is 120827).
     * @return Returns an http status of 200 and a JSON entity containing the blah information.
     *         If the blah doesn't exist, returns status 404.
     *         If there is an error in the request, returns status 400.
     *         On error, a JSON entity will be returned containing detailed error information.
     * @see com.eweware.service.base.store.dao.BlahDAOConstants
     */
    @GET
    @Path("/{blahId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlahById(@PathParam("blahId") String blahId,
                                @QueryParam("stats") final boolean stats,
                                @QueryParam("s") final String s,
                                @QueryParam("e") final String e,
                                @QueryParam("sc") final boolean saveContext,
                                @Context HttpServletRequest req) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.getUserId(req);
            final Response response = RestUtilities.make200OkResponse(getBlahManager().getBlahById(LocaleId.en_us, blahId, userId, stats, s, e));
            getSystemManager().setResponseTime(GET_BLAH_BY_ID_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (ResourceNotFoundException e1) {
            return RestUtilities.make404ResourceNotFoundResponse(req, e1);
        } catch (InvalidRequestException e1) {
            return RestUtilities.make400InvalidRequestResponse(req, e1);
        } catch (SystemErrorException e1) {
            return RestUtilities.make500AndLogSystemErrorResponse(req, e1);
        } catch (Exception e1) {
            return RestUtilities.make500AndLogSystemErrorResponse(req, e1);
        }
    }

    /**
     * <p>By default, returns blahs authored by the logged-in user.</p>
     * <p>If an author id is provided, it returns the blahs created by the specified author instead
     * of by the user.</p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> blahs</div>
     *
     * @param start (Optional): The starting index to fetch when paging
     * @param count (Optional): The max number of blahs to fetch
     * @return An array of blahs.
     * @see com.eweware.service.base.store.dao.BlahDAOConstants
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlahs(@QueryParam("start") Integer start,
                             @QueryParam("count") Integer count,
                             @Context HttpServletRequest req) {
        try {
            final long s = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(req, true);
            final Response response = RestUtilities.make200OkResponse(getBlahManager().getBlahs(LocaleId.en_us, null, userId, null, start, count, null));
            getSystemManager().setResponseTime(GET_BLAHS_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(req, e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(req, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(req, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(req, e);
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

    /**
     * <p>Posts a complaint about a blah</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> blah/(id)/report</div>
     *
     * @param entity The request entity. Requires a JSON entity with an
     *               field named 'type' whose value is the report type.
     * @return Returns an http status 200 with the author's data
     *         If there is an error in the request, returns status 400.
     *         If the referenced commentr can't be found, returns status 404.
     *         If a conflict would arise from satisfying the request, returns status 409.
     *         If the user is not authorized to make this request, returns status 401.
     * @see com.eweware.service.base.store.dao.UserDAOConstants
     */
    @POST
    @Path("/{blahId}/report")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response reportBlah(
            Map<String, String> entity,
            @PathParam("blahId") String blahId,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            Integer reportType = Integer.parseInt(entity.get("type"));
            getBlahManager().reportBlah(userId, blahId, reportType);
            getSystemManager().setResponseTime(REPORT_BLAH_OPERATION, (System.currentTimeMillis() - start));
            return RestUtilities.make204OKNoContentResponse();
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(request, e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(request, e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }


    /**
     * Deletes the blah.
     *
     * @param blahId The blah's id
     * @return The response without content.
     */
    @DELETE
    @Path("/{blahId}")
    public Response deleteBlah(@PathParam("blahId") String blahId,
                               @Context HttpServletRequest req) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(req, true);
            getBlahManager().deleteBlah(LocaleId.en_us, blahId, userId);
            final Response response = RestUtilities.make204OKNoContentResponse();
            getSystemManager().setResponseTime(DELETE_BLAH_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(req, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(req, e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(req, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(req, e);
        }
    }
}

