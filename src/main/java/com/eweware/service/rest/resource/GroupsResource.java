package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.payload.GroupPayload;
import main.java.com.eweware.service.mgr.GroupManager;
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

@Path("/groups")
public class GroupsResource {


    /**
     * <p><Use this method to create a group.  <i>User must be logged in to access this method.</i>/p>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> groups</div>
     *
     * @param entity A JSON entity requiring the group type's id, the group's display name,
     *               a group descriptor, and a validation method.
     * @see main.java.com.eweware.service.user.validation.DefaultUserValidationMethod
     * @see main.java.com.eweware.service.user.validation.DefaultUserValidationMethod
     * @see main.java.com.eweware.service.base.store.dao.GroupDAOConstants.GroupDescriptor
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGroup(GroupPayload entity,
                                @Context UriInfo uri,
                                @Context HttpServletRequest request) {
        try {
            BlahguaSession.ensureAuthenticated(request);
            GroupPayload g = GroupManager.getInstance().createGroup(LocaleId.en_us, entity);
            return RestUtilities.make201CreatedResourceResponse(g, new URI(uri.getAbsolutePath() + g.getId()));
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

    /**
     * <p>Use this method to update a group. <i>User must be logged in to access this method.</i></p>
     * <div><b>METHOD:</b> PUT</div>
     * <div><b>URL:</b> groups/{groupId}</div>
     *
     * @param entity  A JSON entity providing the fields to update.
     * @param groupId The group's id
     * @return If successful, returns http status 204 (NO CONTENT)
     *         without any entity. If there is an error in the request,
     *         returns status 400. If the group resource can't be found,
     *         returns 404. If there is a state conflict implied by
     *         the change, returns status 409.
     *         On errors, a detailed error entity will be returned.
     */
    @PUT
    @Path("/{groupId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateGroup(
            GroupPayload entity,
            @PathParam("groupId") String groupId,
            @Context HttpServletRequest request) {
        try {
            BlahguaSession.ensureAuthenticated(request);
            GroupManager.getInstance().updateGroup(LocaleId.en_us, groupId, entity);
            return RestUtilities.make204OKNoContentResponse();
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
     * <p>Use this method to increment or decrement the number of
     * viewers on a specific group.</p>
     * <div><b>METHOD:</b> PUT</div>
     * <div><b>URL:</b> groups/{groupId}/viewerCount/{added}</div>
     *
     * @param groupId The group's id
     * @param added   If true, increment the current viewer count
     *                for the group; else, decrement it.
     * @return If successful, returns http status of 204 (NO CONTENT)
     *         without an entity. Returns status of 401 if the user
     *         is not authorized to make this change.
     */
    @PUT
    @Path("/{groupId}/viewerCount/{added}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateViewerCount(@PathParam("groupId") String groupId,
                                      @PathParam("added") Boolean added,
                                      @Context HttpServletRequest request) {
        try {
            BlahguaSession.ensureAuthenticated(request);
            GroupManager.getInstance().updateViewerCount(groupId, added, request);
            return RestUtilities.make204OKNoContentResponse();
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Use this method to get the current viewer count for a group.</p>
     * <div><b>METHOD:</b> GET </div>
     * <div><b>URL:</b> groups/{groupId}/viewerCount</div>
     *
     * @param groupId The group's id
     * @return If successful, returns http status 200 with a JSON entity
     *         containing the group id and the current viewer count for the group.
     */
    @GET
    @Path("/{groupId}/viewerCount")
    @Produces(MediaType.APPLICATION_JSON)
    public Response viewerAdded(@PathParam("groupId") String groupId) {
        try {
            return RestUtilities.make200OkResponse(GroupManager.getInstance().getViewerCount(groupId));
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Returns features groups. Currently, the only featured groups
     * are open groups (those that can be accessed by anonymous users).</p>
     *
     * @param start <b>Query Parameter: </b>(Optional:) The start index or null if starting at the base.
     * @param count <b>Query Parameter: </b>(Optional:) The number of items to return.
     * @return If successful, returns a possibly empty array of group ids with http status 200. These
     *         are the open group ids.
     */
    @GET
    @Path("/featured")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFeaturedGroups(
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count) {
        try {
            return RestUtilities.make200OkResponse(GroupManager.getInstance().getOpenGroups(LocaleId.en_us, start, count));
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Use this method to obtain a list of all groups of a given type.</p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> groups</div>
     * TODO See https://eweware.atlassian.net/browse/WRS-181
     *
     * @param groupTypeId   The group type id
     * @param name          <b>Query Parameter: </b>(Optional:) The group's display name
     * @param state         <b>Query Parameter: </b>(Optional:) The group's state
     * @param start         <b>Query Parameter: </b>(Optional:) The start index for the returned group items
     * @param count         <b>Query Parameter: </b>(Optional:) The number of items to return
     * @param sortFieldName <b>Query Parameter: </b>(Optional:) The name of a field by which to sort the results.
     *                      <b>TODO get rid of this!</b>
     * @return If successful, returns a JSON entity with http status 200 with the group entities that match the query parameters.
     *         If the request has an error, returns http status 400.
     * @see GroupPayload
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroups(
            @QueryParam("type") String groupTypeId,
            @QueryParam("name") String name,
            @QueryParam("state") String state,
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count,
            @QueryParam("sort") String sortFieldName,
            @Context HttpServletRequest request) {
        try {
            BlahguaSession.ensureAuthenticated(request);
            return RestUtilities.make200OkResponse(GroupManager.getInstance().getGroups(LocaleId.en_us, groupTypeId, name, state, start, count, sortFieldName));
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

    /**
     * <p>Use this method to get details about a group id.</p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> groups/{groupId}</div>
     *
     * @param groupId The group's id
     * @return If successful, returns http status 200 with a JSON entity
     *         containing the group's info.
     * @see GroupPayload
     */
    @GET
    @Path("/{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupById(@PathParam("groupId") String groupId) {
        try {
            return RestUtilities.make200OkResponse(GroupManager.getInstance().getGroupById(LocaleId.en_us, groupId));
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }
}
