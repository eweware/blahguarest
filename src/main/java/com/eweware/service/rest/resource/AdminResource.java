package main.java.com.eweware.service.rest.resource;

import javax.ws.rs.Path;

/**
 * @author rk@post.harvard.edu
 *         Date: 4/8/13 Time: 12:39 PM
 */
@Path("xylo")
public class AdminResource {

/*
    @POST
    @Path("/blah/{blahId}")
    public Response deleteBlah(
            @PathParam("blahId") String blahId,
            @Context HttpServletRequest request
    ) {
        try {
            BlahguaSession.ensureAdmin(request);
            AdminManager.getInstance().deleteBlah(blahId);
            return RestUtilities.make202AcceptedResponse();
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    @POST
    @Path("/user/clear/{userId}")
    public Response removeUserBlahs(
            @PathParam("userId") String userId,
            @Context HttpServletRequest request
    ) {
        try {
            BlahguaSession.ensureAdmin(request);
            AdminManager.getInstance().removeUserBlahs(userId);
            return RestUtilities.make202AcceptedResponse();
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    @POST
    @Path("/user/delete/{userId}")
    public Response deleteUser(
            @PathParam("userId") String userId,
            @Context HttpServletRequest request
    ) {
        try {
            BlahguaSession.ensureAdmin(request);
            AdminManager.getInstance().deleteUser(userId);
            return RestUtilities.make202AcceptedResponse();
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    @POST
    @Path("group/clear/blahs/{groupId}")
    public Response clearGroupBlahs(
            @PathParam("groupId") String groupId,
            @Context HttpServletRequest request
    ) {
        try {
            BlahguaSession.ensureAdmin(request);
            AdminManager.getInstance().clearGroupBlahs(groupId);
            return RestUtilities.make202AcceptedResponse();
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }
*/

}
