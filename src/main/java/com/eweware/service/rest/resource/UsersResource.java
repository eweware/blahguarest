package main.java.com.eweware.service.rest.resource;

import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.i18n.LocaleId;
import main.java.com.eweware.service.base.payload.UserPayload;
import main.java.com.eweware.service.base.payload.UserProfilePayload;
import main.java.com.eweware.service.base.store.dao.UserDAOConstants;
import main.java.com.eweware.service.mgr.BlahManager;
import main.java.com.eweware.service.mgr.SystemManager;
import main.java.com.eweware.service.mgr.UserManager;
import main.java.com.eweware.service.rest.RestUtilities;
import main.java.com.eweware.service.rest.session.BlahguaSession;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 */

@Path("/users")
public class UsersResource {

    private static final String GET_USERS_OPERATION = "getUsers";
    private static final String GET_USER_BY_ID_OPERATION = "getUserById";
    private static final String GET_USER_INBOX_OPERATION = "getUserInbox";
    private static final String GET_USER_PROFILE_BY_ID_OPERATION = "getUserProfileById";
    private static final String GET_USER_PROFILE_SCHEMA_OPERATION = "getUserProfileSchema";
    private static final String UPDATE_USER_OPERATION = "updateUser";
    private static final String UPDATE_USER_PROFILE_OPERATION = "updateUserProfile";
    private static final String CREATE_USER_OPERATION = "createUser";
    private static final String CREATE_USER_PROFILE_OPERATION = "createUserProfile";
    private static final String VALIDATE_USER_OPERATION = "validateUser";
    private static final String RECOVER_USER_OPERATION = "recoverUser";

    @GET
    @Path("/login/check")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkLogin(@Context HttpServletRequest request) {
        if (BlahguaSession.isAuthenticated(request)) {
            return Response.ok().build();
        } else {
            return Response.status(404).build();
        }
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(Map<String, Object> user,
                              @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String username = (String) user.get(UserDAOConstants.USERNAME);
            final String password = (String) user.get("pwd");
            UserManager.getInstance().loginUser(LocaleId.en_us, username, password, request);
            final Response response = RestUtilities.makeAcceptedResponse();
            SystemManager.getInstance().setResponseTime(RECOVER_USER_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.makeUnauthorizedRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.makeResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    @POST
    @Path("/recover")
    @Produces(MediaType.APPLICATION_JSON)
    public Response recoverUser(@QueryParam("r") String validationCode,  // if recoveryMethod == "r", this is the validation code
                                @QueryParam("k") String methodKey, // e.g., if recoveryMethod == "e", this is an email address
                                @QueryParam("m") String operation) {   // either "e" (email) or "r" (recover via code)
        try {
            final long start = System.currentTimeMillis();
            final UserProfilePayload payload = UserManager.getInstance().recoverUser(LocaleId.en_us, operation, methodKey, validationCode);
            final Response response = (payload == null) ? RestUtilities.makeAcceptedResponse() : RestUtilities.makeOkResponse(payload);
            SystemManager.getInstance().setResponseTime(RECOVER_USER_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.makeStateConflictResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    @POST
    @Path("/validate/{validationCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateUser(@PathParam("validationCode") String validationCode) {
        try {
            final long s = System.currentTimeMillis();
            UserManager.getInstance().validateUser(LocaleId.en_us, validationCode);
            final Response response = RestUtilities.makeAcceptedResponse();
            SystemManager.getInstance().setResponseTime(VALIDATE_USER_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.makeStateConflictResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    @POST
    @Path("/profiles/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUserProfile(UserProfilePayload profile, @Context UriInfo uri) {
        try {
            final long s = System.currentTimeMillis();
            profile = UserManager.getInstance().createOrUpdateUserProfile(LocaleId.en_us, profile, true);
            final Response response = RestUtilities.makeCreatedResourceResponse(profile, new URI(uri.getAbsolutePath() + profile.getId()));
            SystemManager.getInstance().setResponseTime(CREATE_USER_PROFILE_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.makeStateConflictResponse(e);
        } catch (URISyntaxException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(Map<String, Object> user,
                               @Context UriInfo uri,
                               @Context HttpHeaders headers,
                               @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
//            final MultivaluedMap<String, String> requestHeaders = headers.getRequestHeaders();
//            System.out.print("\ncreateUser: remote host=" + request.getRemoteHost() + " addr=" + request.getRemoteAddr() + " port=" + request.getRemotePort() + " | ");
//            for (Map.Entry<String, List<String>> entry : requestHeaders.entrySet()) {
//                System.out.print(entry.getKey() + " = ");
//                for (String val : entry.getValue()) {
//                    System.out.print(val);
//                    System.out.print(" | ");
//                }
//            }
//            System.out.println();

            final String username = (String) user.get(UserDAOConstants.USERNAME);
            final String password = (String) user.get(UserDAOConstants.PASSWORD);
            final UserPayload payload = UserManager.getInstance().createUser(LocaleId.en_us, username, password);
            final Response response = RestUtilities.makeCreatedResourceResponse(payload, new URI(uri.getAbsolutePath() + payload.getId()));
            SystemManager.getInstance().setResponseTime(CREATE_USER_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (URISyntaxException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }


    @PUT
    @Path("/profiles/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUserProfile(UserProfilePayload profile,
                                      @PathParam("userId") String userId) {
        try {
            final long s = System.currentTimeMillis();
            profile.setId(userId);
            UserManager.getInstance().createOrUpdateUserProfile(LocaleId.en_us, profile, false);
            final Response response = RestUtilities.makeOKNoContentResponse();
            SystemManager.getInstance().setResponseTime(UPDATE_USER_PROFILE_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.makeStateConflictResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    @PUT
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(UserPayload updates, @PathParam("userId") String userId) {
        try {
            final long s = System.currentTimeMillis();
            UserManager.getInstance().updateUser(LocaleId.en_us, userId, updates);
            final Response response = RestUtilities.makeOKNoContentResponse();
            SystemManager.getInstance().setResponseTime(UPDATE_USER_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.makeResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    private static final HashMap<String, Object> EMPTY_MAP = new HashMap<String, Object>(0);

    @GET
    @Path("/profiles")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getUserProfileSchema(@QueryParam("schema") boolean schema, @QueryParam("locale") String locale) {
        try {
            final long s = System.currentTimeMillis();
            Response response = null;
            if (schema) {
                response = RestUtilities.makeOkResponse(UserManager.getInstance().getUserProfileSchema(LocaleId.en_us));
            } else { // return nothing: it doesn't make sense and is not safe to return all user profiles
                response = RestUtilities.makeOkResponse(EMPTY_MAP);
            }
            SystemManager.getInstance().setResponseTime(GET_USER_PROFILE_SCHEMA_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    @GET
    @Path("/profiles/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserProfileById(@PathParam("userId") String userId) {
        try {
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.makeOkResponse(UserManager.getInstance().getUserProfileById(LocaleId.en_us, userId));
            SystemManager.getInstance().setResponseTime(GET_USER_PROFILE_BY_ID_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (ResourceNotFoundException e) {
            return RestUtilities.makeResourceNotFoundResponse(e);
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }

    @GET
    @Path("/{userId}/inbox")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserInbox(
            @PathParam("userId") String userId,
            @QueryParam("groupId") String groupId,
            @QueryParam("in") Integer inboxNumber,
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count,
            @QueryParam("sort") String sortFieldName,
            @QueryParam("sortDir") Integer sortDirection,
            @QueryParam("type") String blahTypeId) { // TODO would be nice to get rid of type option (to reduce db index size)
        try {
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.makeOkResponse(BlahManager.getInstance().getUserInbox(LocaleId.en_us, userId, groupId, inboxNumber, blahTypeId, start, count, sortFieldName, sortDirection));
            SystemManager.getInstance().setResponseTime(GET_USER_INBOX_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
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
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(
            @PathParam("userId") String userId,
            @QueryParam("stats") boolean stats,
            @QueryParam("u") boolean byUsername, // if true, the userId is actually the username
            @QueryParam("s") String statsStartDate, // format is yymmdd (e.g., August 27, 2012 is 120827)
            @QueryParam("e") String statsEndDate // format is yymmdd (e.g., August 27, 2012 is 120827)
    ) {
        try {
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.makeOkResponse(UserManager.getInstance().getUserById(LocaleId.en_us, userId, byUsername, stats, statsStartDate, statsEndDate));
            SystemManager.getInstance().setResponseTime(GET_USER_BY_ID_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.makeInvalidRequestResponse(e);
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
    public Response getUsers(
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count,
            @QueryParam("sort") String sortFieldName,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.makeOkResponse(UserManager.getInstance().getUsers(LocaleId.en_us, start, count, sortFieldName));
            SystemManager.getInstance().setResponseTime(GET_USERS_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        } catch (RuntimeException e) {
            return RestUtilities.makeAndLogSystemErrorResponse(e);
        }
    }
}
