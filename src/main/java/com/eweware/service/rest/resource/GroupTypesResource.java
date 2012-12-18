package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.ResourceNotFoundException;
import main.java.com.eweware.service.base.error.StateConflictException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.payload.GroupTypePayload;
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

@Path("/groupTypes")
public class GroupTypesResource {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createGroupType(GroupTypePayload type, @Context UriInfo uri) {
		try {
			type = GroupManager.getInstance().createGroupType(LocaleId.en_us, type);
			return RestUtilities.makeCreatedResourceResponse(type, new URI(uri.getAbsolutePath() + "/" + type.getId()));
		} catch (InvalidRequestException e) {
			return RestUtilities.makeInvalidRequestException(e);
		} catch (StateConflictException e) {
			return RestUtilities.makeStateConflictResponse(e);
		} catch (URISyntaxException e) {
			return RestUtilities.makeAndLogSystemErrorResponse(e);
		} catch (SystemErrorException e) {
			return RestUtilities.makeAndLogSystemErrorResponse(e);
		} catch (RuntimeException e) {
			return RestUtilities.makeAndLogSystemErrorResponse(e);
		}
	}
	
	@PUT
    @Path("/{groupTypeId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateGroupType(GroupTypePayload payloadWithUpdates, @PathParam("groupTypeId") String groupTypeId) {
		try {
            GroupManager.getInstance().updateGroupType(LocaleId.en_us, groupTypeId, payloadWithUpdates);
			return RestUtilities.makeOKNoContentResponse();
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
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGroupTypes(
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count,
            @QueryParam("sort") String sortFieldName) {
		try {
			return RestUtilities.makeOkResponse(GroupManager.getInstance().getGroupTypes(LocaleId.en_us, start, count, sortFieldName));
		} catch (SystemErrorException e) {
			return RestUtilities.makeAndLogSystemErrorResponse(e);
		} catch (RuntimeException e) {
			return RestUtilities.makeAndLogSystemErrorResponse(e);
		}
	}
	
	@GET
	@Path("/{groupTypeId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getGroupTypeById(@PathParam("groupTypeId") String groupTypeId,
			@QueryParam("s") String state) {
		try {
			return RestUtilities.makeOkResponse(GroupManager.getInstance().getGroupTypeById(LocaleId.en_us, groupTypeId));
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
