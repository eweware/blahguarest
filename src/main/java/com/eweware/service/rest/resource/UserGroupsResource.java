package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.payload.AuthorizedState;
import main.java.com.eweware.service.base.payload.UserGroupPayload;
import main.java.com.eweware.service.mgr.SystemManager;
import main.java.com.eweware.service.mgr.UserManager;
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
 */

@Path("/userGroups")
public class UserGroupsResource {

    private static final String GET_USER_GROUPS_OPERATION = "getUserGroups";
    private static final String GET_USER_GROUP_OPERATION = "getUserGroup";
    private static final String REMOVE_USER_FROM_GROUP_OPERATION = "removeUserFromGroup";
    private static final String REGISTER_USER_IN_GROUP_OPERATION = "registerUserInGroup";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUserInGroup(UserGroupPayload payload,
                                        @Context UriInfo uri) {
        try {
            final long start = System.currentTimeMillis();
            final UserGroupPayload userGroup = UserManager.getInstance().registerUserInGroup(LocaleId.en_us, payload.getUserId(), payload.getValidationEmailAddress(), payload.getGroupId());
            final Response response = RestUtilities.makeCreatedResourceResponse(userGroup, new URI(uri.getAbsolutePath() + userGroup.getUserId() + "/" + userGroup.getGroupId()));
            SystemManager.getInstance().setResponseTime(REGISTER_USER_IN_GROUP_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.makeResourceNotFoundResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.makeStateConflictResponse(e);
        } catch (URISyntaxException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (InvalidUserValidationKey e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    @DELETE
    @Path("/{userId}/{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeUserFromGroup(
            @PathParam("userId") String userId,
            @PathParam("groupId") String groupId) {
        try {
            final long start = System.currentTimeMillis();
            UserManager.getInstance().updateUserStatus(LocaleId.en_us, userId, groupId, AuthorizedState.D.toString(), null);
            final Response response = RestUtilities.makeOKNoContentResponse();
            SystemManager.getInstance().setResponseTime(REMOVE_USER_FROM_GROUP_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.makeStateConflictResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.makeResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    @GET
    @Path("/{userId}/{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserGroup(
            @PathParam("userId") String userId,
            @PathParam("groupId") String groupId) {
        try {
            final long start = System.currentTimeMillis();
            final Response response = RestUtilities.makeOkResponse(UserManager.getInstance().getUserGroup(LocaleId.en_us, userId, groupId));
            SystemManager.getInstance().setResponseTime(GET_USER_GROUP_OPERATION, (System.currentTimeMillis() - start));
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
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserGroups(
            @PathParam("userId") String userId,
            @QueryParam("state") String state,
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count,
            @QueryParam("sort") String sortFieldName) {
        try {
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.makeOkResponse(UserManager.getInstance().getUserGroups(LocaleId.en_us, userId, state, start, count, sortFieldName));
            SystemManager.getInstance().setResponseTime(GET_USER_GROUPS_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }
}
