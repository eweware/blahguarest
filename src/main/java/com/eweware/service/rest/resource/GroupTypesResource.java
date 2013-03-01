package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.payload.GroupTypePayload;
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

/**
 * @author rk@post.harvard.edu
 */

@Path("/groupTypes")
public class GroupTypesResource {

    /**
     * <p>Use this method to create a group type.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p><b>This should be restricted to internal use only.</b></p>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> groupTypes</div>
     *
     * @param entity  A JSON payload with the display name for the group type.
     * @param uri     Internal use only.
     * @param request Internal use only
     * @return If successful, returns http code 201 (CREATED) without any entity.
     *         If there is an error in the request, returns code 400.
     *         If there is a state conflict (typically, the group type with the
     *         specified name already exists), returns code 409.
     *         If the user is not authorized to create a group type, returns code 401.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGroupType(GroupTypePayload entity,
                                    @Context UriInfo uri,
                                    @Context HttpServletRequest request) {
        try {
            BlahguaSession.ensureAuthenticated(request);
            entity = GroupManager.getInstance().createGroupType(LocaleId.en_us, entity.getDisplayName());
            return RestUtilities.make201CreatedResourceResponse(entity, new URI(uri.getAbsolutePath() + "/" + entity.getId()));
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
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
     * <p>Use this method to update the group type's display name.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <div><b>METHOD:</b> PUT</div>
     * <div><b>URL:</b> groupTypes/{groupTypeId}</div>
     *
     * @param entity      A JSON entity containing the new display name.
     * @param groupTypeId The type id
     * @return If successful, returns http status 204 (NO CONTENT).
     *         If there is an error in the request, returns status 400.
     *         If the group type doesn't exist, returns status 404.
     *         If the user isn't authorized to access this method, returns status 401.
     */
    @PUT
    @Path("/{groupTypeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateGroupType(
            GroupTypePayload entity,
            @PathParam("groupTypeId") String groupTypeId,
            @Context HttpServletRequest request) {
        try {
            BlahguaSession.ensureAuthenticated(request);
            GroupManager.getInstance().updateGroupTypeDisplayName(LocaleId.en_us, groupTypeId, entity.getDisplayName());
            return RestUtilities.make204OKNoContentResponse();
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Use this method to obtain existing group types.</p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> groupTypes</div>
     *
     * @param start        <i>Query Parameter:</i>(Optional:) The start index for the array of
     *                      returned items.
     * @param count        <i>Query Parameter:</i>(Optional:) The number of items to return in the array.
     * @param sortFieldName<i>Query Parameter:</i>(Optional:) The name of a field to sort by. <b>Please don't use this as it will
     *                      be removed in a coming release.</b>
     * @return A list of group type entities with http status of 200.
     * @see GroupTypePayload
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupTypes(
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count,
            @QueryParam("sort") String sortFieldName) {
        try {
            return RestUtilities.make200OkResponse(GroupManager.getInstance().getGroupTypes(LocaleId.en_us, start, count, sortFieldName));
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>User this method to obtain details about the group type.</p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> groupTypes/{groupTypeId}</div>
     *
     * @param groupTypeId The group type's id
     * @return A JSON entity with the group type information.
     * If there is an error in the request, returns status 400.
     * If the referenced blah or author can't be found, returns status 404.
     * @see GroupTypePayload
     */
    @GET
    @Path("/{groupTypeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroupTypeById(
            @PathParam("groupTypeId") String groupTypeId) {
        try {
            return RestUtilities.make200OkResponse(GroupManager.getInstance().getGroupTypeById(LocaleId.en_us, groupTypeId));
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
