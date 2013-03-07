package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.InvalidAuthorizedStateException;
import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.mgr.BlahManager;
import main.java.com.eweware.service.mgr.UserManager;
import main.java.com.eweware.service.rest.RestUtilities;
import main.java.com.eweware.service.rest.session.BlahguaSession;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * <p>Search-specific API methods. </p>
 * <p><b>Not in use. We've temporarily switched indexing off until we get specific requirements for this.
 * In addition, the real implementation will probably use Solr4 (with it's realtime update capability)
 * rather than Zoeie.</b></p>
 * <div>Note that some methods require authentication (previous login) to be accessed.</div>
 * @author rk@post.harvard.edu
 */

@Path("/search")
public class SearchResource {

    // TODO search comments

    /**
     * <p>Search is currently turned off (no indexing). We'll probably use a Solr4 cluster when we need it.</p>
     */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
    @Path("blahs")
	public Response searchBlahs(
			@QueryParam("q") String query,
			@QueryParam("f") String fieldName,
			@QueryParam("s") String subset,
			@QueryParam("c") Integer count,
            @Context HttpServletRequest request
			) {
		try {
            BlahguaSession.ensureAuthenticated(request);
            BlahManager blahMgr = BlahManager.getInstance();
            return RestUtilities.make200OkResponse(blahMgr.search(LocaleId.en_us, fieldName, query, subset, count));
		} catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (SystemErrorException e) {
			return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Search is currently turned off (no indexing). We'll probably use a Solr4 cluster when we need it.</p>
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("users")
    public Response searchUsers(
            @QueryParam("q") String query,
            @QueryParam("f") String fieldName,
            @Context HttpServletRequest request
    ) {
        try {
            BlahguaSession.ensureAuthenticated(request);
            UserManager userManager = UserManager.getInstance();
            return RestUtilities.make200OkResponse(userManager.searchUserIndex(LocaleId.en_us, fieldName, query));
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }
}
