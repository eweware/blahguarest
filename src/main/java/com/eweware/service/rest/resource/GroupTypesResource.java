package com.eweware.service.rest.resource;

import com.eweware.service.base.error.InvalidRequestException;
import com.eweware.service.base.error.ResourceNotFoundException;
import com.eweware.service.base.error.SystemErrorException;
import com.eweware.service.base.i18n.LocaleId;
import com.eweware.service.base.mgr.SystemManager;
import com.eweware.service.mgr.GroupManager;
import com.eweware.service.rest.RestUtilities;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * <p>Group type-specific API methods.</p>
 * <div>Note that some methods require authentication (previous login) to be accessed.</div>
 * @author rk@post.harvard.edu
 */

@Path("/groupTypes")
public class GroupTypesResource {

    private static final String GET_GROUP_TYPES_OPERATION = "getGroupTypes";
    private static final String GET_GROUP_TYPE_BY_ID_OPERATION = "getGroupTypeById";
    private GroupManager groupManager;
    private SystemManager systemManager;

    /**
     * <p>Use this method to obtain existing group types.</p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> groupTypes</div>
     *
     * @param start                 <i>Query Parameter:</i>(Optional:) The start index for the array of
     *                              returned items.
     * @param count                 <i>Query Parameter:</i>(Optional:) The number of items to return in the array.
     * @param sortFieldName<i>Query Parameter:</i>(Optional:) The name of a field to sort by. <b>Please don't use this as it will
     *                              be removed in a coming release.</b>
     * @return A list of JSON entities (GroupTypePayload entities) with http status of 200.
     * @see com.eweware.service.base.store.dao.GroupTypeDAOConstants
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupTypes(
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count,
            @QueryParam("sort") String sortFieldName,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.make200OkResponse(getGroupManager().getGroupTypes(LocaleId.en_us, start, count, sortFieldName));
            getSystemManager().setResponseTime(GET_GROUP_TYPES_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    /**
     * <p>User this method to obtain details about the group type.</p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> groupTypes/{groupTypeId}</div>
     *
     * @param groupTypeId <i>Path Parameter:</i> The group type's id
     * @return A JSON entity (a GroupTypePayload) with the group type information.
     *         If there is an error in the request, returns status 400.
     *         If the referenced blah or author can't be found, returns status 404.
     * @see com.eweware.service.base.store.dao.GroupTypeDAOConstants
     */
    @GET
    @Path("/{groupTypeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupTypeById(
            @PathParam("groupTypeId") String groupTypeId,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.make200OkResponse(getGroupManager().getGroupTypeById(LocaleId.en_us, groupTypeId));
            getSystemManager().setResponseTime(GET_GROUP_TYPE_BY_ID_OPERATION, (System.currentTimeMillis() - s));
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
}



//    /**
//     * <p>Use this method to create a group type.</p>
//     * <p><i>User must be logged in to use this method.</i></p>
//     * <p><b>This should be restricted to internal use only.</b></p>
//     * <p/>
//     * <div><b>METHOD:</b> POST</div>
//     * <div><b>URL:</b> groupTypes</div>
//     *
//     * @param entity A JSON payload (a GroupTypePayload) with the display name for the group type.
//     * @return If successful, returns http code 201 (CREATED) without any entity.
//     *         If there is an error in the request, returns code 400.
//     *         If there is a state conflict (typically, the group type with the
//     *         specified name already exists), returns code 409.
//     *         If the user is not authorized to create a group type, returns code 401.
//     * @see com.eweware.service.base.store.dao.GroupTypeDAOConstants
//     */
//    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response createGroupType(GroupTypePayload entity,
//                                    @Context UriInfo uri,
//                                    @Context HttpServletRequest request) {
//        try {
//            BlahguaSession.ensureAuthenticated(request);
//            entity = getGroupManager().createGroupType(LocaleId.en_us, entity.getDisplayName());
//            return RestUtilities.make201CreatedResourceResponse(entity, new URI(uri.getAbsolutePath() + "/" + entity.getId()));
//        } catch (InvalidRequestException e) {
//            return RestUtilities.make400InvalidRequestResponse(e);
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


//    /**
//     * <p>Use this method to update the group type's display name.</p>
//     * <p><i>User must be logged in to use this method.</i></p>
//     * <p/>
//     * <div><b>METHOD:</b> PUT</div>
//     * <div><b>URL:</b> groupTypes/{groupTypeId}</div>
//     *
//     * @param entity      A JSON entity (a GroupTypePayload) containing the new display name.
//     * @param groupTypeId <i>Path Parameter:</i> The type id
//     * @return If successful, returns http status 204 (NO CONTENT).
//     *         If there is an error in the request, returns status 400.
//     *         If the group type doesn't exist, returns status 404.
//     *         If the user isn't authorized to access this method, returns status 401.
//     * @see com.eweware.service.base.store.dao.GroupTypeDAOConstants
//     */
//    @PUT
//    @Path("/{groupTypeId}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response updateGroupType(
//            GroupTypePayload entity,
//            @PathParam("groupTypeId") String groupTypeId,
//            @Context HttpServletRequest request) {
//        try {
//            BlahguaSession.ensureAuthenticated(request);
//            getGroupManager().updateGroupTypeDisplayName(LocaleId.en_us, groupTypeId, entity.getDisplayName());
//            return RestUtilities.make204OKNoContentResponse();
//        } catch (InvalidRequestException e) {
//            return RestUtilities.make400InvalidRequestResponse(e);
//        } catch (ResourceNotFoundException e) {
//            return RestUtilities.make404ResourceNotFoundResponse(e);
//        } catch (InvalidAuthorizedStateException e) {
//            return RestUtilities.make401UnauthorizedRequestResponse(e);
//        } catch (SystemErrorException e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(e);
//        } catch (Exception e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(e);
//        }
//    }