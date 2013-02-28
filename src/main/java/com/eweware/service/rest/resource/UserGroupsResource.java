package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.payload.AuthorizedState;
import main.java.com.eweware.service.base.payload.UserGroupPayload;
import main.java.com.eweware.service.mgr.SystemManager;
import main.java.com.eweware.service.mgr.UserManager;
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
 * @author rk@post.harvard.edu
 */

@Path("/userGroups")
public class UserGroupsResource {

    private static final String GET_USER_GROUPS_OPERATION = "getUserGroups";
    private static final String GET_USER_GROUP_OPERATION = "getUserGroup";
    private static final String REMOVE_USER_FROM_GROUP_OPERATION = "removeUserFromGroup";
    private static final String REGISTER_USER_IN_GROUP_OPERATION = "registerUserInGroup";

    /**
     * <p>Use this method to register a user in a group (to join a group).</p>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> userGroups</div>
     *
     * @param payload Required content entity: A JSON object containing the
     *                user id of the user and the group id of the group to join.
     *                If necessary, a valdation email address should be included.
     * @param uri     Internal: a uri for building the location header.
     * @return If successful, returns an http status 201 (CREATED) and returns
     *         an entity containing the user group payload.
     *         If there is an error with the request, returns status 400.
     *         If a resource is not found, returns status 404.
     *         If the user is not authorized to access this method, returns status 401.
     *         If there is a state conflict in satisfying this request, returns status 409.
     *         On error, a JSON entity detailing the error will be returned.
     * @see UserGroupPayload
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUserInGroup(
            UserGroupPayload payload,
            @Context UriInfo uri,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            final UserGroupPayload userGroup = UserManager.getInstance().registerUserInGroup(LocaleId.en_us, payload.getUserId(), payload.getValidationEmailAddress(), payload.getGroupId());
            final Response response = RestUtilities.make201CreatedResourceResponse(userGroup, new URI(uri.getAbsolutePath() + userGroup.getUserId() + "/" + userGroup.getGroupId()));
            SystemManager.getInstance().setResponseTime(REGISTER_USER_IN_GROUP_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(e);
        }  catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (InvalidUserValidationKey e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Use this method to remove a user from a group (unjoin it).</p>
     * <div><b>METHOD:</b> DELETE</div>
     * <div><b>URL:</b> userGroups/{userId}/{groupId}</div>
     *
     * @param userId  The user's id
     * @param groupId The groups's id
     * @return If successful, returns an http status 204 (NO CONTENT).
     *         If there's an error in the request, returns status 400.
     *         If there's a state conflict in the update, returns status 409.
     *         If the user or group is not found, returns status 404.
     *         On error, details are provided in a JSON entity.
     */
    @DELETE
    @Path("/{userId}/{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeUserFromGroup(
            @PathParam("userId") String userId,
            @PathParam("groupId") String groupId,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            UserManager.getInstance().updateUserStatus(LocaleId.en_us, userId, groupId, AuthorizedState.D.toString(), null);
            final Response response = RestUtilities.make204OKNoContentResponse();
            SystemManager.getInstance().setResponseTime(REMOVE_USER_FROM_GROUP_OPERATION, (System.currentTimeMillis() - start));
            return response;
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
     * <p>User this method to get information about a user's relationship to a group, if any.</p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> userGroups/{userId}/{groupId}</div>
     *
     * @param userId  The user's id
     * @param groupId The group's id
     * @return An http status of 200 with a user group payload if the method succeeds.
     *         If the request is invalid, returns status 400.
     *         If the user or group is not found, returns status 404.
     *         On error, details are provided in a JSON entity.
     */
    @GET
    @Path("/{userId}/{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserGroup(
            @PathParam("userId") String userId,
            @PathParam("groupId") String groupId,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            final Response response = RestUtilities.make200OkResponse(UserManager.getInstance().getUserGroup(LocaleId.en_us, userId, groupId));
            SystemManager.getInstance().setResponseTime(GET_USER_GROUP_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
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
     * <p>Use this to obtain user-to-group relations for this user by accessing via its id.</p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> userGroups/{userId}</div>
     *
     * @param userId        The user id
     * @param state         <b>Optional query parameter:</b> The user-to-group state (see AuthorizedState)
     * @param start         <b>Optional query parameter:</b> The start index for the returned items
     * @param count         <b>Optional query parameter:</b> The number of items to return in a page.
     * @param sortFieldName <b>Optional query parameter:</b> A field to against. <b>Don't use sort: not very useful and should remove.</b>
     * @return If successful, returns an http status of 200 and the returned
     *         entity is an array of user payload objects.
     *         If there is an error with the request, returns status 400.
     *         If the user is not authorized to access this method, returns status 401.
     *         On error, a JSON entity detailing the error will be returned.
     * @see UserGroupsResource
     * @see AuthorizedState
     */
    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserGroups(
            @PathParam("userId") String userId,
            @QueryParam("state") String state,
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count,
            @QueryParam("sort") String sortFieldName,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            final Response response = RestUtilities.make200OkResponse(UserManager.getInstance().getUserGroups(LocaleId.en_us, userId, state, start, count, sortFieldName));
            SystemManager.getInstance().setResponseTime(GET_USER_GROUPS_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }
}
