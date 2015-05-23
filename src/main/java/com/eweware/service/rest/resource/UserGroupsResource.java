package com.eweware.service.rest.resource;

import com.eweware.service.base.error.*;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.mgr.SystemManager;
import com.eweware.service.base.payload.AuthorizedState;
import com.eweware.service.base.payload.UserGroupPayload;
import com.eweware.service.mgr.UserManager;
import com.eweware.service.rest.RestUtilities;
import com.eweware.service.rest.session.BlahguaSession;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Map;

/**
 * <p>API methods related to user/group relationships.</p>
 * <div>Note that some methods require authentication (previous login) to be accessed.</div>
 * @author rk@post.harvard.edu
 */

@Path("/userGroups")
public class UserGroupsResource {

    private static final String GET_USER_GROUPS_OPERATION = "getUserGroups";
    private static final String GET_USER_GROUP_OPERATION = "getUserGroup";
    private static final String REMOVE_USER_FROM_GROUP_OPERATION = "removeUserFromGroup";
    private static final String REGISTER_ALL_USERS_IN_GROUP_OPERATION = "registerAllUsersInGroup";

    private static final String REGISTER_USER_IN_GROUP_OPERATION = "registerUserInGroup";
    private UserManager userManager;
    private SystemManager systemManager;

    /**
     * <p>Use this method to register a user in a group (to join a group).</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> userGroups</div>
     *
     * @param entity A JSON entity (a UserGroupPayload) containing the
     *               the group id of the group to join in a field named 'G'.
     * @return If successful, returns an http status 201 (CREATED).
     *         If there is an error with the request, returns status 400.
     *         If a resource is not found, returns status 404.
     *         If the user is not authorized to access this method, returns status 401.
     *         If there is a state conflict in satisfying this request, returns status 409.
     *         On error, a JSON entity detailing the error will be returned.
     * @see com.eweware.service.base.store.dao.UserGroupDAOConstants
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerUserInGroup(
            Map<String, String> entity,
            @Context UriInfo uri,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            final String groupId = entity.get("G");
            final UserGroupPayload userGroup = getUserManager().registerUserInGroup(LocaleId.en_us, userId, null, groupId);
            final Response response = RestUtilities.make201CreatedResourceResponse(userGroup, new URI(uri.getAbsolutePath().toString()));
            getSystemManager().setResponseTime(REGISTER_USER_IN_GROUP_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(request, e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(request, e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(request, e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(request, e);
        } catch (InvalidUserValidationKey e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    @POST
    @Path("/all")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerAllUsersInGroup(
            Map<String, String> entity,
            @Context UriInfo uri,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            final String groupId = entity.get("G");
            final UserGroupPayload userGroup = getUserManager().registerAllUsersInGroup(LocaleId.en_us, userId, null, groupId);
            final Response response = RestUtilities.make201CreatedResourceResponse(userGroup, new URI(uri.getAbsolutePath().toString()));
            getSystemManager().setResponseTime(REGISTER_ALL_USERS_IN_GROUP_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(request, e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(request, e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(request, e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(request, e);
        } catch (InvalidUserValidationKey e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    /**
     * <p>Use this method to remove a user from a group (unjoin it).</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> DELETE</div>
     * <div><b>URL:</b> userGroups</div>
     *
     * @param entity    A JSON entity containing the group id in a
     *                  field named 'G'.
     * @return If successful, returns an http status 204 (NO CONTENT).
     *         If there's an error in the request, returns status 400.
     *         If there's a state conflict in the update, returns status 409.
     *         If the user or group is not found, returns status 404.
     *         On error, details are provided in a JSON entity.
     * @see com.eweware.service.base.store.dao.UserGroupDAOConstants
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeUserFromGroup(
            Map<String, String> entity,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            final String groupId = entity.get("G");
            getUserManager().updateUserStatus(LocaleId.en_us, userId, groupId, AuthorizedState.X.toString());
            final Response response = RestUtilities.make204OKNoContentResponse();
            getSystemManager().setResponseTime(REMOVE_USER_FROM_GROUP_OPERATION, (System.currentTimeMillis() - start));
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
     * <p>User this method to get information about a user's relationship to a group, if any.</p>
     * <p/>
     * <p><i>User must be logged in to use this method.</i></p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> userGroups/{groupId}</div>
     *
     * @param groupId <i>Path Parameter:</i> The group's id
     * @return An http status of 200 with a user group payload if the method succeeds.
     *         If the request is invalid, returns status 400.
     *         If the user or group is not found, returns status 404.
     *         On error, details are provided in a JSON entity.
     * @see com.eweware.service.base.store.dao.UserGroupDAOConstants
     */
    @GET
    @Path("/{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserGroup(
            @PathParam("groupId") String groupId,
            @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            final Response response = RestUtilities.make200OkResponse(getUserManager().getUserGroup(LocaleId.en_us, userId, groupId));
            getSystemManager().setResponseTime(GET_USER_GROUP_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(request, e);
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
     * <p>Use this to obtain user-to-group relations for this user by accessing via its id.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> userGroups</div>
     *
     * @param state         <i>Query Parameter:</i> The user-to-group state (see AuthorizedState)
     * @param start         <i>Query Parameter:</i> The start index for the returned items
     * @param count         <i>Query Parameter:</i> The number of items to return in a page.
     * @param sortFieldName <i>Query Parameter:</i> A field to against. <b>Don't use sort: not very useful and should remove.</b>
     * @return If successful, returns an http status of 200 and the returned
     *         entity is an array of user payload objects.
     *         If there is an error with the request, returns status 400.
     *         If the user is not authorized to access this method, returns status 401.
     *         On error, a JSON entity detailing the error will be returned.
     * @see UserGroupsResource
     * @see AuthorizedState
     * @see com.eweware.service.base.store.dao.UserGroupDAOConstants
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserGroups(
            @QueryParam("state") String state,
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count,
            @QueryParam("sort") String sortFieldName,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            final Response response = RestUtilities.make200OkResponse(getUserManager().getUserGroups(LocaleId.en_us, userId, state, start, count, sortFieldName));
            getSystemManager().setResponseTime(GET_USER_GROUPS_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(request, e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    private UserManager getUserManager() throws SystemErrorException {
        if (userManager == null) {
            userManager = UserManager.getInstance();
        }
        return userManager;
    }
    private SystemManager getSystemManager() throws SystemErrorException {
        if (systemManager == null) {
            systemManager = SystemManager.getInstance();
        }
        return systemManager;
    }
}
