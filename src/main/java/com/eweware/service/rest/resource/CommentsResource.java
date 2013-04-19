package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.payload.CommentPayload;
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
import java.util.Map;

/**
 * <p>Comment-specific API methods.</p>
 * <div>Note that some methods require authentication (previous login) to be accessed.</div>
 * @author rk@post.harvard.edu
 *         Date: 6/27/12 Time: 6:00 PM
 */
@Path("/comments")
public class CommentsResource {

    private static final String CREATE_COMMENT_OPERATION = "createComment";
    //    private static final String DELETE_COMMENT_OPERATION = "deleteComment";
    private static final String GET_COMMENT_BY_ID_OPERATION = "getCommentById";
    private static final String GET_COMMENTS_OPERATION = "getComments";
    private static final String UPDATE_COMMENT_OPERATION = "updateComment";
    private static final String GET_COMMENT_AUTHOR_OPERATION = "getCommentAuthor";

    private static BlahManager blahManager;
    private static SystemManager systemManager;



    /**
     * <p>Returns user information about the comment's author</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> comments/author</div>
     *
     * @param entity The request entity. Requires a JSON entity with an
     *               field named 'I' whose value is the comment id.
     * @return Returns an http status 200 with the author's data
     *         If there is an error in the request, returns status 400.
     *         If the referenced blah or author can't be found, returns status 404.
     *         If a conflict would arise from satisfying the request, returns status 409.
     *         If the user is not authorized to make this request, returns status 401.
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
            final Response response = RestUtilities.make200OkResponse(getBlahManager().getAuthorFromComment(LocaleId.en_us, entity.get("I")));
            getSystemManager().setResponseTime(GET_COMMENT_AUTHOR_OPERATION, (System.currentTimeMillis() - start));
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
     * <p>Use this method to create a comment for a blah.></p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> comments</div>
     *
     * @param entity A JSON entity (a CommentPayload). This must contain the following
     *               CommentPayload fields: the blah id to which this is a comment and the comment's text.
     * @return Returns an http status of 201 (CREATED) without a JSON entity.
     *         If there is an error in the request, returns status 400.
     *         If the referenced blah or author can't be found, returns status 404.
     *         If a conflict would arise from satisfying the request, returns status 409.
     *         If the user is not authorized to make this request, returns status 401.
     * @see main.java.com.eweware.service.base.store.dao.CommentDAOConstants
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createComment(
            CommentPayload entity,
            @Context UriInfo uri,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String authorId = BlahguaSession.ensureAuthenticated(request, true);
            entity = getBlahManager().createComment(LocaleId.en_us, authorId, entity);
            final Response response = RestUtilities.make201CreatedResourceResponse(entity, new URI(uri.getAbsolutePath() + entity.getId()));
            getSystemManager().setResponseTime(CREATE_COMMENT_OPERATION, (System.currentTimeMillis() - start));
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
     * <p>Use this method to update a comment.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> PUT</div>
     * <div><b>URL:</b> comments/{commentId}</div>
     *
     * @param entity    A JSON entity (a CommentPayload) with the following CommentPayload fields:
     *                  The id of the user updating the comment is required.
     *                  Optional are a vote for the comment, a view count, and/or an open count for the comment.
     * @param commentId The comment id
     * @return A response with http status 204 (NO CONTENT) without any JSON entity.
     *         If there is an error in the request, returns status 400.
     *         If the referenced blah or author can't be found, returns status 404.
     *         If a conflict would arise from satisfying the request, returns status 409.
     *         If the user is not authorized to make this request, returns status 401.
     * @see main.java.com.eweware.service.base.store.dao.CommentDAOConstants
     */
    @PUT
    @Path("/{commentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateComment(
            CommentPayload entity,
            @PathParam("commentId") String commentId,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            getBlahManager().updateComment(LocaleId.en_us, entity, userId, commentId);
            final Response response = RestUtilities.make204OKNoContentResponse();
            getSystemManager().setResponseTime(UPDATE_COMMENT_OPERATION, (System.currentTimeMillis() - start));
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
     * <p>Use this method to obtain a comment by its id.</p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> comments/{commentId}</div>
     *
     * @param commentId      <i>Path Parameter:</i> The comment's id
     * @param stats          <i>Query Parameter:</i> A boolean. If true, comment statistics will
     *                       be included with the returned entity. <b>NOTE: this will be moved
     *                       to a separate method in the future.</b>
     * @param userId         <i>Query Parameter:</i> REQUIRED. The id of the user
     * @param s <i>Query Parameter:</i> OPTIONAL: If stats is true, this is a start date with which to filter
     *                       the returned stats. Format is yymmdd (e.g., August 27, 2012 is 120827).
     * @param e   <i>Query Parameter:</i> OPTIONAL: If stats is true and there is a start date,
     *                       this is an end date with which to filter
     *                       the returned stats. An end date without a start date will be rejected.
     *                       Format is yymmdd (e.g., August 27, 2012 is 120827).
     * @return A response with http status 200 (OK) with a JSON entity with the comment data.
     *         If there is an error in the request, returns status 400.
     *         If the referenced blah or author can't be found, returns status 404.
     *         If a conflict would arise from satisfying the request, returns status 409.
     * @see main.java.com.eweware.service.base.store.dao.CommentDAOConstants
     */
    @GET
    @Path("/{commentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCommentById(
            @PathParam("commentId") String commentId,
            @QueryParam("stats") boolean stats,
            @QueryParam("userId") String userId,
            @QueryParam("s") String s,
            @QueryParam("e") String e,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final Response response = RestUtilities.make200OkResponse(getBlahManager().getCommentById(LocaleId.en_us, BlahguaSession.isAuthenticated(request), commentId, userId, stats, s, e));
            getSystemManager().setResponseTime(GET_COMMENT_BY_ID_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e1) {
            return RestUtilities.make400InvalidRequestResponse(request, e1);
        } catch (ResourceNotFoundException e1) {
            return RestUtilities.make404ResourceNotFoundResponse(request, e1);
        } catch (SystemErrorException e1) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e1);
        } catch (Exception e1) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e1);
        }
    }

    /**
     * <p>Use this method to get the comments for a blah.</p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> comments</div>
     *
     * @param blahId        <i>Path Parameter:</i> The blah id
     * @param userId        <i>Query Parameter:</i> Optional. If provided, it will return view/open/vote information
     *                      on this comment for the specified user id. TODO make it implicit
     * @param authorId      <i>Query Parameter:</i> Optional. If provided, the returned comments will be
     *                      filtered by the specified author's id.
     * @param start         <i>Query Parameter:</i> Optional.   The start index of the comments to return.
     * @param count         <i>Query Parameter:</i> Optional. The number of comments to return. Default: returns all.
     * @param sortFieldName <i>Query Parameter:</i> Optional. A field with which to sort the results.  TODO remove this?
     * @return Returns http status 200 with a list of comment entities.
     *         If there is an error in the request, returns status 400.
     *         If the referenced blah or author can't be found, returns status 404.
     *         If a conflict would arise from satisfying the request, returns status 409.
     * @see main.java.com.eweware.service.base.store.dao.CommentDAOConstants
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCommentsForBlah(
            @QueryParam("blahId") String blahId,
            @QueryParam("userId") String userId,
            @QueryParam("authorId") String authorId,
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count,
            @QueryParam("sort") String sortFieldName,
            @Context HttpServletRequest request
    ) {
        try {
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.make200OkResponse(getBlahManager().getComments(LocaleId.en_us, BlahguaSession.isAuthenticated(request), blahId, userId, authorId, start, count, sortFieldName));
            getSystemManager().setResponseTime(GET_COMMENTS_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(request, e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
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


//    /**
//     *
//     * <p></p>
//     * <div><b>METHOD:</b> </div>
//     * <div><b>URL:</b> </div>
//     * @param commentId
//     * @return
//     */
//    @DELETE
//    @Path("/{commentId}")
//    public Response deleteComment(@PathParam("commentId") String commentId) {
//        try {
//            final long start = System.currentTimeMillis();
//            getBlahManager().deleteComment(LocaleId.en_us, commentId);
//            final Response response = RestUtilities.make204OKNoContentResponse();
//            getSystemManager().setResponseTime(DELETE_COMMENT_OPERATION, (System.currentTimeMillis() - start));
//            return response;
//        } catch (InvalidRequestException e) {
//            return RestUtilities.make400InvalidRequestResponse(e);
//        } catch (SystemErrorException e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(e);
//        } catch (Exception e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(e);
//        }
//    }