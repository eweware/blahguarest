package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.ResourceNotFoundException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.mgr.AdminManager;
import main.java.com.eweware.service.rest.RestUtilities;
import main.java.com.eweware.service.rest.session.BlahguaSession;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * @author rk@post.harvard.edu
 *         Date: 4/8/13 Time: 12:39 PM
 */
@Path("xylo")
public class AdminResource {

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


}
