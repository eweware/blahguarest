package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.InvalidAuthorizedStateException;
import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.payload.BadgePayload;
import main.java.com.eweware.service.base.payload.BadgingNotificationEntity;
import main.java.com.eweware.service.base.store.dao.BadgeAuthorityDAO;
import main.java.com.eweware.service.mgr.BadgesManager;
import main.java.com.eweware.service.rest.RestUtilities;
import main.java.com.eweware.service.rest.session.BlahguaSession;
import org.apache.http.HttpStatus;

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

    private BadgesManager badgesMgr;

    private BadgesManager getBadgesMgr() {
        if (badgesMgr == null) {
            badgesMgr = BadgesManager.getInstance();
        }
        return badgesMgr;
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
    public Response getAuthorities() {
        try {
            return RestUtilities.make200OkResponse(getBadgesMgr().getAuthorities());
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Requests that a badge be created for a user from a specified authority.</p>
     * <p/>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> badges</div>
     *
     * @param entity A JSON entity containing the id of the badging authority
     *               in a field named 'i'. If the authority manages more than
     *               one type of badge, the authority-specific badge id is a string
     *               and it must also be specified in the JSON entity in a field
     *               named 't': if the authority manages only one kind of badge,
     *               this is unnecessary.
     * @return If it succeeds, returns an http status 200 (OK) with a JSON
     *         entity containing the badge id (a field in BadgeDAOConstants) and the
     *         badge is added to the user's collection of badges.
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
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            final String authorityId = (String) entity.get("i");
            final String badgeTypeId = (String) entity.get("t");
            return getBadgesMgr().createBadgeForUser(response, userId, authorityId, badgeTypeId);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
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
            Map<String, Object> entity
    ) {
        try {
            return getBadgesMgr().addBadge(entity);
        } catch (SystemErrorException e) {
            logger.log(Level.SEVERE, "Error processing add badge notification. Entity: " + entity, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(BadgesManager.makeError(BadgingNotificationEntity.ERROR_CODE_TRANSACTION_SERVER_ERROR, e.getMessage())).build();
        }
    }

    @GET
    @Path("/{badgeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getBadgeById(@PathParam("badgeId") String badgeId,
                                 @Context HttpServletRequest request) {
        try {
            BlahguaSession.ensureAuthenticated(request);
            final BadgePayload entity = getBadgesMgr().getBadgeById(badgeId);
            return RestUtilities.make200OkResponse(entity);
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
}
