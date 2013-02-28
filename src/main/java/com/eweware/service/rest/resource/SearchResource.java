package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.mgr.BlahManager;
import main.java.com.eweware.service.mgr.UserManager;
import main.java.com.eweware.service.rest.RestUtilities;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author rk@post.harvard.edu
 */

@Path("/search")
public class SearchResource {

    // TODO search comments

	@GET
	@Produces(MediaType.APPLICATION_JSON)
    @Path("blahs")
	public Response searchBlahs(
			@QueryParam("q") String query,
			@QueryParam("f") String fieldName,
			@QueryParam("s") String subset,
			@QueryParam("c") Integer count
			) {
		try {
			BlahManager blahMgr = BlahManager.getInstance();
            return RestUtilities.make200OkResponse(blahMgr.search(LocaleId.en_us, fieldName, query, subset, count));
		} catch (SystemErrorException e) {
			return RestUtilities.make500AndLogSystemErrorResponse(e);
		} catch (Exception e) {
			return RestUtilities.make500AndLogSystemErrorResponse(e);
		}
	}


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("users")
    public Response searchUsers(
            @QueryParam("q") String query,
            @QueryParam("f") String fieldName
    ) {
        try {
            UserManager userManager = UserManager.getInstance();
            return RestUtilities.make200OkResponse(userManager.searchUserIndex(LocaleId.en_us, fieldName, query));
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }
}
