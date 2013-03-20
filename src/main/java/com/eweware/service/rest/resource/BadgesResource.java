package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.InvalidAuthorizedStateException;
import main.java.com.eweware.service.base.error.InvalidRequestException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.store.dao.BadgeAuthorityDAO;
import main.java.com.eweware.service.mgr.BadgesManager;
import main.java.com.eweware.service.rest.RestUtilities;
import main.java.com.eweware.service.rest.session.BlahguaSession;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

/**
 * <p>Badges resource.</p>
 *
 * @author rk@post.harvard.edu
 *         Date: 3/18/13 Time: 5:37 PM
 */
@Path("badges")
public class BadgesResource {

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
            @Context HttpServletRequest request) {
        try {
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            final String authorityId = (String) entity.get("i");
            final String badgeTypeId = (String) entity.get("t");
            final String html = getBadgesMgr().createBadgeForUser(userId, authorityId, badgeTypeId);
            return RestUtilities.make200OkResponse(html);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }
}
