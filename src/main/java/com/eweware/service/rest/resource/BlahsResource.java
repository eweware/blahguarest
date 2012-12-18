package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.ResourceNotFoundException;
import main.java.com.eweware.service.base.error.StateConflictException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.payload.BlahPayload;
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
 */

@Path("/blahs")
public class BlahsResource {

    private static final String CREATE_BLAH_OPERATION = "createBlah";
    private static final String UPDATE_BLAH_OPERATION = "updateBlah";
    private static final String DELETE_BLAH_OPERATION = "deleteBlah";
    private static final String GET_BLAH_TYPES_OPERATION = "getBlahTypes";
    private static final String GET_BLAH_BY_ID_OPERATION = "getBlahById";
    private static final String GET_BLAHS_OPERATION = "getBlahs";

    /**
     * Creates a blah.
     *
     * @param blah The blah payload
     * @param uri  URI to use to build the http Location header
     * @return BlahPayload The created blah with the new _id
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createBlah(BlahPayload blah, @Context UriInfo uri) {
        try {
            final long start = System.currentTimeMillis();
            blah = BlahManager.getInstance().createBlah(LocaleId.en_us, blah);
            final Response response = RestUtilities.makeCreatedResourceResponse(blah, new URI(uri.getAbsolutePath() + blah.getId()));
            SystemManager.getInstance().setResponseTime(CREATE_BLAH_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestException(e);
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

    /**
     * Updates a blah.
     *
     * @param blah   The blah payload with the fields to update.
     * @param blahId The blah's id
     * @return An update response without content.
     */
    @PUT
    @Path("/{blahId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateBlah(
            BlahPayload blah,
            @PathParam("blahId") String blahId) {
        try {
            final long start = System.currentTimeMillis();
            blah.setId(blahId);
            BlahManager.getInstance().updateBlah(LocaleId.en_us, blah);
            final Response response = RestUtilities.makeOKNoContentResponse();
            SystemManager.getInstance().setResponseTime(UPDATE_BLAH_OPERATION, (System.currentTimeMillis() - start));
            return response;
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

    /**
     * Deletes the blah.
     * TODO this should either not be exposed or it should be a soft delete
     *
     * @param blahId The blah's id
     * @return The response without content.
     */
    @DELETE
    @Path("/{blahId}")
    public Response deleteBlah(@PathParam("blahId") String blahId) {
        try {
            final long start = System.currentTimeMillis();
            BlahManager.getInstance().deleteBlah(LocaleId.en_us, blahId);
            final Response response = RestUtilities.makeOKNoContentResponse();
            SystemManager.getInstance().setResponseTime(DELETE_BLAH_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestException(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    /**
     * Returns an array of blah type docs.
     *
     * @return List<BlahTypePayload> Blah types
     */
    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlahTypes() {
        try {
            final long start = System.currentTimeMillis();
            final Response response = RestUtilities.makeOkResponse(BlahManager.getInstance().getBlahTypes(LocaleId.en_us));
            SystemManager.getInstance().setResponseTime(GET_BLAH_TYPES_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } finally {

        }
    }

    /**
     * Returns information about a blah.
     * If a userId is provided, the blah's data will include blah stats
     * for the specified user instead of stats for the blah itself.
     *
     * @param blahId The blah's id
     * @param userId (Optional): a userId
     * @return List<BlahPayload> An array of blah docs.
     */
    @GET
    @Path("/{blahId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlahById(@PathParam("blahId") String blahId,
                                @QueryParam("userId") String userId,
                                @QueryParam("stats") boolean stats,
                                @QueryParam("s") String statsStartDate, // format is yymmdd (e.g., August 27, 2012 is 120827)
                                @QueryParam("e") String statsEndDate) {  // format is yymmdd (e.g., August 27, 2012 is 120827)
        try {
            final long start = System.currentTimeMillis();
            final Response response = RestUtilities.makeOkResponse(BlahManager.getInstance().getBlahById(LocaleId.en_us, blahId, userId, stats, statsStartDate, statsEndDate));
            SystemManager.getInstance().setResponseTime(GET_BLAH_BY_ID_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (ResourceNotFoundException e) {
            return RestUtilities.makeResourceNotFoundResponse(e);
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestException(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    /**
     * Returns information about the blahs.
     * If a userId is provided, the blah's data will include blah stats
     * for the specified user instead of stats for the blah itself.
     * If an authorId is provided, only blah's data for the author
     * will be returned.
     *
     * @param start         (Optional): The starting index to fetch when paging
     * @param count         (Optional): The max number of blahs to fetch
     * @param sortFieldName (Optional): name of the field to sort on
     * @param authorId      (Optional): The blah author's userId
     * @param userId        (Optional): The id of the user for whom blah stats will be returned
     * @param typeId        (Optional): The blah's type id. If not given, all types will be returned.
     * @return List<BlahPayload> An array of blahs
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBlahs(@QueryParam("start") Integer start,
                             @QueryParam("count") Integer count,
                             @QueryParam("sort") String sortFieldName,
                             @QueryParam("authorId") String authorId,
                             @QueryParam("userId") String userId,
                             @QueryParam("typeid") String typeId) {
        try {
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.makeOkResponse(BlahManager.getInstance().getBlahs(LocaleId.en_us, userId, authorId, typeId, start, count, sortFieldName));
            SystemManager.getInstance().setResponseTime(GET_BLAHS_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }
}
