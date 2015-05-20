package com.eweware.service.rest.resource;

import com.eweware.service.base.error.InvalidAuthorizedStateException;
import com.eweware.service.base.error.InvalidRequestException;
import com.eweware.service.base.error.ResourceNotFoundException;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.mgr.SystemManager;
import com.eweware.service.base.payload.ChannelImportPayload;
import com.eweware.service.base.payload.GroupPayload;
import com.eweware.service.mgr.GroupManager;
import com.eweware.service.rest.RestUtilities;
import com.eweware.service.rest.session.BlahguaSession;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * <p>Group-specific API methods.</p>
 * <div>Note that some methods require authentication (previous login) to be accessed.</div>
 * @author rk@post.harvard.edu
 */

@Path("/groups")
public class GroupsResource {

    private static final String GET_VIEWER_COUNT_OPERATION = "getViewerCount";
    private static final String GET_OPEN_GROUPS_OPERATION = "getOpenGroups";
    private static final String GET_GROUPS_OPERATION = "getGroups";
    private static final String GET_GROUP_BY_ID_OPERATION = "getGroupById";
    private static final String GET_GROUP_PERMISSION_BY_ID_OPERATION = "getGroupPermissionById";
    private static final String GET_GROUP_IMPORTERS = "getGroupImporters";
    private static final String ADD_GROUP_IMPORTER = "addGroupImporter";
    private static final String UPDATE_GROUP_IMPORTER = "updateGroupImporter";
    private static final String DELETE_GROUP_IMPORTER = "deleteGroupImporter";
    private static final String DELETE_GROUP = "deleteGroup";

    private static GroupManager groupManager;
    private static SystemManager systemManager;


    /**
     * <p>Use this method to get the current viewer count for a group.</p>
     * <p/>
     * <div><b>METHOD:</b> GET </div>
     * <div><b>URL:</b> groups/{groupId}/viewerCount</div>
     *
     * @param groupId <i>Path Parameter</i>: The group's id
     * @return If successful, returns http status 200 with a JSON entity (a GroupPayload)
     *         containing the group id and the current viewer count for the group.
     * @see com.eweware.service.base.store.dao.GroupDAOConstants
     */
    @GET
    @Path("/{groupId}/viewerCount")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getViewerCount(@PathParam("groupId") String groupId, @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final Response response = RestUtilities.make200OkResponse(getGroupManager().getViewerCount(groupId));
            getSystemManager().setResponseTime(GET_VIEWER_COUNT_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    /**
     * <p>Returns features groups. Currently, the only featured groups
     * are <b>open groups</b> (those that can be accessed by anonymous users).</p>
     * <p/>
     * <div><b>METHOD:</b> GET </div>
     * <div><b>URL:</b> groups/featured</div>
     *
     * @param start<i>Query Parameter:</i>(Optional:) The start index or null if starting at the base.
     * @param count<i>Query Parameter:</i>(Optional:) The number of items to return.
     * @return If successful, returns a possibly empty array of group ids with http status 200. These
     *         are the open group ids.
     * @see com.eweware.service.base.store.dao.GroupDAOConstants
     */
    @GET
    @Path("/featured")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFeaturedGroups(
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.make200OkResponse(getGroupManager().getOpenGroups(LocaleId.en_us, start, count));
            getSystemManager().setResponseTime(GET_OPEN_GROUPS_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    /**
     * <p>Use this method to obtain a list of all groups of a given type.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <p>TODO See https://eweware.atlassian.net/browse/WRS-181</p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> groups</div>
     *
     * @param groupTypeId           <i>Path Parameter:</i> The group type id
     * @param name                  <i>Query Parameter:</i> Optional. The group's display name
     * @param state                 <i>Query Parameter:</i> Optional. The group's state
     * @param start                 <i>Query Parameter:</i> Optional. The start index for the returned group items
     * @param count                 <i>Query Parameter:</i> Optional. The number of items to return
     * @param sortFieldName<i>Query Parameter:</i> Optional. The name of a field by which to sort the results.
     *                              <b>TODO get rid of this!</b>
     * @return If successful, returns a JSON entity (a GroupPayload) with http status 200
     *         with the group entities that match the query parameters.
     *         If the request has an error, returns http status 400.
     * @see com.eweware.service.base.store.dao.GroupDAOConstants
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
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            final Response response = RestUtilities.make200OkResponse(getGroupManager().getGroups(LocaleId.en_us, groupTypeId, name, state, start, count, sortFieldName));
            getSystemManager().setResponseTime(GET_GROUPS_OPERATION, (System.currentTimeMillis() - s));
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

    /**
     * <p>Use this method to get details about a group id.</p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> groups/{groupId}</div>
     *
     * @param groupId <i>Path Parameter:</i> The group's id
     * @return If successful, returns http status 200 with a JSON entity
     *         (a GroupPayload) containing the group's info.
     * @see com.eweware.service.base.store.dao.GroupDAOConstants
     */
    @GET
    @Path("/{groupId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupById(@PathParam("groupId") String groupId, @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.make200OkResponse(getGroupManager().getGroupById(LocaleId.en_us, groupId));
            getSystemManager().setResponseTime(GET_GROUP_BY_ID_OPERATION, (System.currentTimeMillis() - s));
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


    /**
     * <p>Use this method to get permission details about a group id.</p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> groups/{groupId}</div>
     *
     * @param groupId <i>Path Parameter:</i> The group's id
     * @return If successful, returns http status 200 with a JSON entity
     *         (a GroupPayload) containing the group's info.
     * @see com.eweware.service.base.store.dao.GroupDAOConstants
     */
    @GET
    @Path("/{groupId}/permission")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupPermissionById(@PathParam("groupId") String groupId, @Context HttpServletRequest request) {
        try {
            String userId = null;

            if (BlahguaSession.isAuthenticated(request))
                userId = BlahguaSession.ensureAuthenticated(request, true);
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.make200OkResponse(getGroupManager().getGroupPermissionById(groupId, userId));
            getSystemManager().setResponseTime(GET_GROUP_PERMISSION_BY_ID_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    @GET
    @Path("/{groupId}/importers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupImporters(@PathParam("groupId") String groupId, @Context HttpServletRequest request) {
        try {
            String userId = null;

            if (BlahguaSession.isAuthenticated(request))
                userId = BlahguaSession.ensureAuthenticated(request, true);
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.make200OkResponse(getGroupManager().getImportRecords(userId, groupId));
            getSystemManager().setResponseTime(GET_GROUP_IMPORTERS, (System.currentTimeMillis() - s));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    @POST
    @Path("/{groupId}/importers")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addGroupImporter(ChannelImportPayload importPayload,
                                      @PathParam("groupId") String groupId,
                                      @Context HttpServletRequest request) {
        try {
            String userId = null;

            if (BlahguaSession.isAuthenticated(request))
                userId = BlahguaSession.ensureAuthenticated(request, true);
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.make200OkResponse(getGroupManager().addImportRecord(userId, importPayload));
            getSystemManager().setResponseTime(ADD_GROUP_IMPORTER, (System.currentTimeMillis() - s));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    @PUT
    @Path("/importers/{importerid}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateGroupImporter(ChannelImportPayload importPayload,
                                      @PathParam("importerid") String importerid,
                                      @Context HttpServletRequest request) {
        try {
            String userId = null;

            if (BlahguaSession.isAuthenticated(request))
                userId = BlahguaSession.ensureAuthenticated(request, true);
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.make200OkResponse(getGroupManager().updateImportRecord(userId, importPayload));
            getSystemManager().setResponseTime(UPDATE_GROUP_IMPORTER, (System.currentTimeMillis() - s));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    @DELETE
    @Path("/importers/{importerid}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteGroupImporter(@PathParam("importerid") String importerid,
                                      @Context HttpServletRequest request) {
        try {
            String userId = null;

            if (BlahguaSession.isAuthenticated(request))
                userId = BlahguaSession.ensureAuthenticated(request, true);
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.make200OkResponse(getGroupManager().deleteImportRecord(userId, importerid));
            getSystemManager().setResponseTime(DELETE_GROUP_IMPORTER, (System.currentTimeMillis() - s));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }


    private GroupManager getGroupManager() throws SystemErrorException {
        if (groupManager == null) {
            groupManager = GroupManager.getInstance();
        }
        return groupManager;
    }

    private SystemManager getSystemManager() throws SystemErrorException {
        if (systemManager == null) {
            systemManager = SystemManager.getInstance();
        }
        return systemManager;
    }

//    /**
//     * <p>Use this method to update a group.</p>
//     * <p><i>User must be logged in to use this method.</i></p>
//     * <p/>
//     * <div><b>METHOD:</b> PUT</div>
//     * <div><b>URL:</b> groups/{groupId}</div>
//     *
//     * @param entity  A JSON entity (a GroupPayload) providing the fields to update.
//     *                The following fields may be updated: the groups display name,
//     *                the description, and/or the group's state.
//     *                Note that the descriptor may not be changed usig this API.
//     * @param groupId <i>Path Parameter</i>: The group's id
//     * @return If successful, returns http status 204 (NO CONTENT)
//     *         without any entity. If there is an error in the request,
//     *         returns status 400. If the group resource can't be found,
//     *         returns 404. If there is a state conflict implied by
//     *         the change, returns status 409.
//     *         On errors, a detailed error entity will be returned.
//     * @see com.eweware.service.base.store.dao.GroupDAOConstants
//     */
//    @PUT
//    @Path("/{groupId}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response updateGroup(
//            GroupPayload entity,
//            @PathParam("groupId") String groupId,
//            @Context HttpServletRequest request) {
//        try {
//            BlahguaSession.ensureAuthenticated(request);  // TODO mark who updated group last?
//            final String displayName = entity.getDisplayName();
//            final String description = entity.getDescription();
//            final String state = entity.getState();
//            getGroupManager().updateGroup(LocaleId.en_us, groupId, displayName, description, state);
//            return RestUtilities.make204OKNoContentResponse();
//        } catch (InvalidRequestException e) {
//            return RestUtilities.make400InvalidRequestResponse(e);
//        } catch (ResourceNotFoundException e) {
//            return RestUtilities.make404ResourceNotFoundResponse(e);
//        } catch (StateConflictException e) {
//            return RestUtilities.make409StateConflictResponse(e);
//        } catch (InvalidAuthorizedStateException e) {
//            return RestUtilities.make401UnauthorizedRequestResponse(e);
//        } catch (SystemErrorException e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(e);
//        } catch (Exception e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(e);
//        }
//    }


    /**
     * <p><Use this method to create a group./p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> groups</div>
     *
     * @param entity A JSON entity (a GroupPayload) requiring the group type's id, the group's display name,
     *               a group descriptor, a description, and a validation method.
     * @see com.eweware.service.base.store.dao.GroupDAOConstants
     * @see com.eweware.service.user.validation.DefaultUserValidationMethod
     * @see com.eweware.service.user.validation.DefaultUserValidationMethod
     * @see com.eweware.service.base.store.dao.GroupDAOConstants.GroupDescriptor
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGroup(GroupPayload entity,
                                @Context UriInfo uri,
                                @Context HttpServletRequest request) {
        try {
            final String userId = BlahguaSession.ensureAuthenticated(request, true);

            final long start = System.currentTimeMillis();

            GroupPayload g = getGroupManager().createGroup(LocaleId.en_us, userId, entity);
            return RestUtilities.make201CreatedResourceResponse(g, new URI(uri.getAbsolutePath() + g.getId()));
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


    @DELETE
    @Path("/{groupId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteGroup(@PathParam("groupId") String groupId,
                                @Context HttpServletRequest request) {
        try {
            final String userId = BlahguaSession.ensureAuthenticated(request, true);

            final long start = System.currentTimeMillis();

            Boolean didIt = getGroupManager().deleteGroup(LocaleId.en_us, groupId, userId);
            Response response =  RestUtilities.make200OkResponse(didIt);
            getSystemManager().setResponseTime(DELETE_GROUP, (System.currentTimeMillis() - start));
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
}
