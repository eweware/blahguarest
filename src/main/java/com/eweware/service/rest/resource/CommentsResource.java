package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.ResourceNotFoundException;
import main.java.com.eweware.service.base.error.StateConflictException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.payload.CommentPayload;
import main.java.com.eweware.service.mgr.BlahManager;
import main.java.com.eweware.service.mgr.SystemManager;
import main.java.com.eweware.service.rest.RestUtilities;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author rk@post.harvard.edu
 *         Date: 6/27/12 Time: 6:00 PM
 */
@Path("/comments")
public class CommentsResource {


    private static final String CREATE_COMMENT_OPERATION = "createComment";
    private static final String DELETE_COMMENT_OPERATION = "deleteComment";
    private static final String GET_COMMENT_BY_ID_OPERATION = "getCommentById";
    private static final String GET_COMMENTS_OPERATION = "getComments";
    private static final String UPDATE_COMMENT_OPERATION = "updateComment";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createComment(
            CommentPayload comment,
            @Context UriInfo uri) {
        try {
            final long start = System.currentTimeMillis();
            comment = BlahManager.getInstance().createComment(LocaleId.en_us, comment);
            final Response response = RestUtilities.makeCreatedResourceResponse(comment, new URI(uri.getAbsolutePath() + comment.getId()));
            SystemManager.getInstance().setResponseTime(CREATE_COMMENT_OPERATION, (System.currentTimeMillis() - start));
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
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    @PUT
    @Path("/{commentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateComment(
            CommentPayload comment,
            @PathParam("commentId") String commentId) {
        try {
            final long start = System.currentTimeMillis();
            comment.setId(commentId); // ensure this
            BlahManager.getInstance().updateComment(LocaleId.en_us, comment);
            final Response response = RestUtilities.makeOKNoContentResponse();
            SystemManager.getInstance().setResponseTime(UPDATE_COMMENT_OPERATION, (System.currentTimeMillis() - start));
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

    @DELETE
    @Path("/{commentId}")
    public Response deleteComment(@PathParam("commentId") String commentId) {
        try {
            final long start = System.currentTimeMillis();
            BlahManager.getInstance().deleteComment(LocaleId.en_us, commentId);
            final Response response = RestUtilities.makeOKNoContentResponse();
            SystemManager.getInstance().setResponseTime(DELETE_COMMENT_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    @GET
    @Path("/{commentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCommentById(
            @PathParam("commentId") String commentId,
            @QueryParam("stats") boolean stats,
            @QueryParam("userId") String userId,
            @QueryParam("s") String statsStartDate, // format is yymmdd (e.g., August 27, 2012 is 120827)
            @QueryParam("e") String statsEndDate) {  // format is yymmdd (e.g., August 27, 2012 is 120827)) {
        try {
            final long start = System.currentTimeMillis();
            final Response response = RestUtilities.makeOkResponse(BlahManager.getInstance().getCommentById(LocaleId.en_us, commentId, userId, stats, statsStartDate, statsEndDate));
            SystemManager.getInstance().setResponseTime(GET_COMMENT_BY_ID_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.makeResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getComments(
            @QueryParam("blahId") String blahId, // the blah whose comments are requested
            @QueryParam("count") Integer count, // how many comments to return
            @QueryParam("authorId") String authorId, // filter the returned comments by the comments authored by this user
            @QueryParam("sort") String sortFieldName, // the name of the field to sort on
            @QueryParam("userId") String userId,
            @QueryParam("start") Integer start) {
        try {
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.makeOkResponse(BlahManager.getInstance().getComments(LocaleId.en_us, blahId, userId, authorId, start, count, sortFieldName));
            SystemManager.getInstance().setResponseTime(GET_COMMENTS_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.makeResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }
}
