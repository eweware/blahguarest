package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.ResourceNotFoundException;
import main.java.com.eweware.service.base.error.StateConflictException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.payload.GroupPayload;
import main.java.com.eweware.service.mgr.GroupManager;
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

@Path("/groups")
public class GroupsResource {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createGroup(GroupPayload group,
                                @Context UriInfo uri) {
		try {
            GroupPayload g = GroupManager.getInstance().createGroup(LocaleId.en_us, group);
            return RestUtilities.makeCreatedResourceResponse(g, new URI(uri.getAbsolutePath() + g.getId()));
		} catch (InvalidRequestException e) {
			return RestUtilities.makeInvalidRequestException(e);
		} catch (URISyntaxException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
		} catch (SystemErrorException e) {
			return RestUtilities.makeAndLogSystemErrorResponse(e);
		} catch (RuntimeException e) {
			return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }
	
	@PUT
    @Path("/{groupId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateGroup(GroupPayload group, @PathParam("groupId") String groupId) {
		try {
            GroupManager.getInstance().updateGroup(LocaleId.en_us, groupId, group);
			return RestUtilities.makeOKNoContentResponse();
		} catch (InvalidRequestException e) {
			return RestUtilities.makeInvalidRequestException(e);
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
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGroups(
            @QueryParam("type") String groupTypeId,
            @QueryParam("name") String name,
            @QueryParam("state") String state,
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count,
            @QueryParam("sort") String sortFieldName) {
		try {
			return RestUtilities.makeOkResponse(GroupManager.getInstance().getGroups(LocaleId.en_us, groupTypeId, name, state, start, count, sortFieldName));
		} catch (InvalidRequestException e) {
			return RestUtilities.makeInvalidRequestException(e);
		} catch (SystemErrorException e) {
			return RestUtilities.makeAndLogSystemErrorResponse(e);
		} catch (RuntimeException e) {
			return RestUtilities.makeAndLogSystemErrorResponse(e);
		}
	}

	
	@GET
	@Path("/{groupId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGroupById(@PathParam("groupId") String groupId) {
		try {
			return RestUtilities.makeOkResponse(GroupManager.getInstance().getGroupById(LocaleId.en_us, groupId));
		} catch (InvalidRequestException e) {
			return RestUtilities.makeInvalidRequestException(e);
		} catch (ResourceNotFoundException e) {
			return RestUtilities.makeResourceNotFoundResponse(e);
		} catch (SystemErrorException e) {
			return RestUtilities.makeAndLogSystemErrorResponse(e);
		} catch (RuntimeException e) {
			return RestUtilities.makeAndLogSystemErrorResponse(e);
		}
	}
}
