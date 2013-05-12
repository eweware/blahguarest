package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.mgr.SystemManager;
import main.java.com.eweware.service.base.payload.BadgePayload;
import main.java.com.eweware.service.base.payload.BadgingNotificationEntity;
import main.java.com.eweware.service.mgr.BadgesManager;
import main.java.com.eweware.service.rest.RestUtilities;
import main.java.com.eweware.service.rest.session.BlahguaSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>Badges resource.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 3/18/13 Time: 5:37 PM
 */
@Path("badges")
public class BadgesResource {

    private static final Logger logger = Logger.getLogger("BadgesResource");

    private static final String CREATE_BADGE_OPERATION = "createBadge";
    private static final String GET_BADGE_BY_ID_OPERATION = "getBadgeById";
    private static final String ADD_BADGE_TO_USER_OPERATION = "addBadgeToUser";
    private static final String DELETE_BADGE_OPERATION = "deleteBadge";

    private BadgesManager badgesMgr;
    private SystemManager systemManager;

    private BadgesManager getBadgesMgr() {
        if (badgesMgr == null) {
            badgesMgr = BadgesManager.getInstance();
        }
        return badgesMgr;
    }

    private SystemManager getSystemManager() throws SystemErrorException {
        if (systemManager == null) {
            systemManager = SystemManager.getInstance();
        }
        return systemManager;
    }

    /**
     * <p>Returns list of badging authorities in our directory.</p>
     * <p/>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> badges/authorities</div>
     *
     * @return Http 200 (OK) response includes an entity that is
     *         a list of badging authorities known to Blahgua. The badging
     *         authority entities' fields are described in BadgeAuthorityDAOConstants.
     * @see main.java.com.eweware.service.base.store.dao.BadgeAuthorityDAOConstants
     */
    @GET
    @Path("authorities")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuthorities(@Context HttpServletRequest request) {
        try {
            return RestUtilities.make200OkResponse(getBadgesMgr().getAuthorities());
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    /**
     * <p>Requests that a badge be created for a user from a specified authority.</p>
     * <p/>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> badges</div>
     *
     * @param entity A JSON entity containing the id of the badging authority
     *               in a field named 'I'. If the authority manages more than
     *               one type of badge, the authority-specific badge id is a string
     *               and it must also be specified in the JSON entity in a field
     *               named 'T': if the authority manages only one kind of badge,
     *               this is unnecessary.
     * @return If it succeeds, returns an http status 200 (OK) with HTML contents
     *         that initiate a transaction between the user and the badge authority.
     *         If the user already has active badges for that authority, the
     *         reponse is http status 202 (ACCEPTED) without any contents.
     *         Else, the following http status codes are possible: 400 (either
     *         the request was invalid or the user is not authorized to login),
     *         or 404 (the badge authority does not exist or is unavailable).
     *         An error message will specify more details when there's an error.
     */
    @POST
    @Produces(MediaType.TEXT_HTML)
    public Response createBadgeForUser(
            Map<String, Object> entity,
            @Context HttpServletRequest request,
            @Context HttpServletResponse response) {
        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            final String authorityId = (String) entity.get("I");
            final String badgeTypeId = (String) entity.get("T");
            final Response badgeForUser = getBadgesMgr().createBadgeForUser(userId, authorityId, badgeTypeId);
            getSystemManager().setResponseTime(CREATE_BADGE_OPERATION, (System.currentTimeMillis() - start));
            return badgeForUser;
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(request, e);
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBadge(@PathParam("i") String badgeId, @Context HttpServletRequest request) {
        final long start = System.currentTimeMillis();
        try {
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            getBadgesMgr().deleteBadgeForUser(userId, badgeId);
            final Response response = RestUtilities.make202AcceptedResponse();
            getSystemManager().setResponseTime(DELETE_BADGE_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(request, e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(request, e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(request, e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    /**
     * <p>A badging authority calls this to add a badge for a user.</p>
     * @param entity    A JSON entity representing the badge notification metadata
     *                  containing fields in BadgingNotificationEntity.
     * @return
     * @see main.java.com.eweware.service.base.payload.BadgingNotificationEntity
     */
    @POST
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addBadgeToUser(
            Map<String, Object> entity,
            @Context HttpServletRequest request
    ) {
        try {
            final long start = System.currentTimeMillis();
            final Response response = getBadgesMgr().addBadge(entity);
            getSystemManager().setResponseTime(ADD_BADGE_TO_USER_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (SystemErrorException e) {
            logger.log(Level.SEVERE, "Error processing add badge notification. Entity: " + entity + "\nINFO:\n" + RestUtilities.getRequestInfo(request), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(BadgesManager.makeError(BadgingNotificationEntity.ERROR_CODE_TRANSACTION_SERVER_ERROR, "System Error")).build();
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(request, e);
        }
    }

    @GET
    @Path("/{badgeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getBadgeById(@PathParam("badgeId") String badgeId,
                                 @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            final BadgePayload entity = getBadgesMgr().getBadgeById(badgeId);
            final Response response = RestUtilities.make200OkResponse(entity);
            getSystemManager().setResponseTime(GET_BADGE_BY_ID_OPERATION, (System.currentTimeMillis() - start));
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
