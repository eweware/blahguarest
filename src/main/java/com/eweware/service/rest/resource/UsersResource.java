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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * @author rk@post.harvard.edu
 */

@Path("/users")
public class UsersResource {

    private static final String GET_USERS_OPERATION = "getUsers";
    private static final String GET_USER_BY_ID_OPERATION = "getUserById";
    //    private static final String GET_USER_INBOX_OPERATION = "getUserInbox";
    private static final String GET_ANONYMOUS_INBOX_OPERATION = "getUserInbox";
    private static final String GET_USER_PROFILE_BY_ID_OPERATION = "getUserProfileById";
    private static final String GET_USER_PROFILE_SCHEMA_OPERATION = "getUserProfileSchema";
    private static final String UPDATE_USER_OPERATION = "updateUser";
    private static final String UPDATE_USER_PROFILE_OPERATION = "updateUserProfile";
    private static final String CREATE_USER_OPERATION = "createUser";
    private static final String CREATE_USER_PROFILE_OPERATION = "createUserProfile";
    private static final String VALIDATE_USER_OPERATION = "validateUser";
    private static final String RECOVER_USER_OPERATION = "recoverUser";

    /**
     * <p>Use this method to check whether a username is available to use
     * as the username in a new user account.</p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> users/check/username/{username}</div>
     *
     * @param username The username. Must be at least three
     *                 and less than 64 characters long.
     * @return If successful (username is available), returns an http code of 204 (OK NO CONTENT).
     *         If the request is invalid, it returns 400 (BAD REQUEST).
     *         If the username already exists, it returns 409 (CONFLICT).
     *         On error conditions, a JSON object is returned with details.
     */
    @GET
    @Path("/check/username/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkUsername(
            @PathParam("username") String username) {
        try {
            UserManager.getInstance().ensureUserExistsByUsername(username);
            return RestUtilities.make204OKNoContentResponse();
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Use this method to check whether a user is logged in as an authenticated user.
     * An authenticated user has logged in with a username and password scheme.</p>
     * <p>This call doesn't require any parameters: it uses the session object.</p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> users/login/check</div>
     *
     * @param request Internal parameter: the request object.
     * @return Returns http status 200 if the user session is authenticated, else
     *         returns 404.
     */
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

    /**
     * <p>Use this method to log in a user.</p>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> users/login</div>
     *
     * @param user    A JSON object containing a username and a password.
     * @param request Internal: the current request.
     * @return If it succeededs, returns an http status 202 (ACCEPTED).
     *         Else, the following http status codes are possible: 400 (either
     *         the request was invalid or the user is not authorized to login),
     *         or 404 (user resource does not exist). In either case a JSON
     *         error message will specify more details.
     * @see UserPayload
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUser(Map<String, Object> user,
                              @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String username = (String) user.get(UserDAOConstants.USERNAME);
            final String password = (String) user.get(UserDAOConstants.PASSWORD);
            UserManager.getInstance().loginUser(LocaleId.en_us, username, password, request);
            final Response response = RestUtilities.make202AcceptedResponse();
            SystemManager.getInstance().setResponseTime(RECOVER_USER_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Use this method to create a user profile.</p>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> users/profiles</div>
     *
     * @param profile A JSON object containing the user profile data.
     * @param uri     Internal: a uri used to build the location header.
     * @return If successful, returns an http status code of 201 (CREATED).
     *         If a profile object already exists, it will return 409 (CONFLICT).
     *         If the request is invalid, it will return 400 (BAD REQUEST).
     *         If the user is not authorized to access this method, returns 401.
     *         On error conditions, a JSON object is returned with details.
     * @see UserProfilePayload
     */
    @POST
    @Path("/profiles/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUserProfile(
            UserProfilePayload profile,
            @Context UriInfo uri,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            profile = UserManager.getInstance().createOrUpdateUserProfile(LocaleId.en_us, profile, true);
            final Response response = RestUtilities.make201CreatedResourceResponse(profile, new URI(uri.getAbsolutePath() + profile.getId()));
            SystemManager.getInstance().setResponseTime(CREATE_USER_PROFILE_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Use this method to register a user.</p>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> users</div>
     *
     * @param user A JSON object containing a username and password.
     * @param uri  Internal: the uri to build the location header
     * @return If successful, returns an http status code of 201 (CREATED).
     *         If a user with the username already exists, it will return 409 (CONFLICT).
     *         If the request is invalid, it will return 400 (BAD REQUEST).
     *         On error conditions, a JSON object is returned with details.
     * @see UserPayload
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerUser(Map<String, Object> user,
                                 @Context UriInfo uri,
                                 @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            final String username = (String) user.get(UserDAOConstants.USERNAME);
            final String password = (String) user.get(UserDAOConstants.PASSWORD);
            final UserPayload payload = UserManager.getInstance().registerUser(LocaleId.en_us, username, password);
            final Response response = RestUtilities.make201CreatedResourceResponse(payload, new URI(uri.getAbsolutePath() + payload.getId()));
            SystemManager.getInstance().setResponseTime(CREATE_USER_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Use this method to update profile fields.</p>
     * <div><b>METHOD:</b> PUT</div>
     * <div><b>URL:</b> users/profiles/{userId}</div>
     *
     * @param profile A JSON object containing the fields to update.
     * @param userId  The user id
     * @return If successful, returns http code 204 (OK NO CONTENT).
     *         If the request is invalid, returns 400 (BAD REQUEST).
     *         If the profile has not been created, returns 404 (NOT FOUND).
     *         On error conditions, a JSON object is returned with details.
     * @see UserProfilePayload
     */
    @PUT
    @Path("/profiles/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUserProfile(
            UserProfilePayload profile,
            @PathParam("userId") String userId,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            profile.setId(userId);
            UserManager.getInstance().createOrUpdateUserProfile(LocaleId.en_us, profile, false);
            final Response response = RestUtilities.make204OKNoContentResponse();
            SystemManager.getInstance().setResponseTime(UPDATE_USER_PROFILE_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>User this method to update a user's username. The user
     * must be logged in.</p>
     * <div><b>METHOD:</b> PUT</div>
     * <div><b>URL:</b> users/{userId}/username/{username}</div>
     *
     * @param username The new username
     * @return Returns http status code 204 (NO CONTENT) on success.
     *         If the user or user account doesn't exist, returns 404 (NOT FOUND).
     *         If the input is invalid, returns 400 (BAD REQUEST).
     *         If username is already taken, returns 409 (CONFLICT).
     *         On error conditions, a JSON object is returned with details.
     */
    @PUT
    @Path("/{userId}/username/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUsername(
            @PathParam("username") String username,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            UserManager.getInstance().updateUsername(LocaleId.en_us, request, username);
            final Response response = RestUtilities.make204OKNoContentResponse();
            SystemManager.getInstance().setResponseTime(UPDATE_USER_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>User this method to update a user's password. The user must be logged in.</p>
     * <div><b>METHOD:</b> PUT</div>
     * <div><b>URL:</b> users/{userId}/password/{password}</div>
     *
     * @param password The new password
     * @return Returns http status code 204 (NO CONTENT) on success.
     *         If the user or user account doesn't exist, returns 404 (NOT FOUND).
     *         If the input is invalid, returns 400 (BAD REQUEST).
     *         If username is already taken, returns 409 (CONFLICT).
     *         On error conditions, a JSON object is returned with details.
     */
    @PUT
    @Path("/{userId}/password/{password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(
            @PathParam("password") String password,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            UserManager.getInstance().updatePassword(LocaleId.en_us, request, password);
            final Response response = RestUtilities.make204OKNoContentResponse();
            SystemManager.getInstance().setResponseTime(UPDATE_USER_OPERATION, (System.currentTimeMillis() - s));
            return response;
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


    /**
     * <p>Returns the schema for the user profile record. The schema specifies
     * all fields in the user profile and their acceptable values (constraints), as appropriate.</p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> users/profiles</div>
     *
     * @return Returns a user profile schema JSON object with an http status of 200.
     * @see main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema
     */
    @GET
    @Path("/profiles")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getUserProfileSchema(@Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            Response response = RestUtilities.make200OkResponse(UserManager.getInstance().getUserProfileSchema(LocaleId.en_us));
            SystemManager.getInstance().setResponseTime(GET_USER_PROFILE_SCHEMA_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Use this method to get the user's profile by user id.</p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> users/profiles/{userId}</div>
     *
     * @param userId The user id
     * @return If successful, returns an http code of 200 (OK) with a payload
     *         containing the user profile settings.
     *         If there is no profile for this user (or if user doesn't exist), returns 404 (NOT FOUND).
     *         If the request is invalid, returns 400 (BAD REQUEST).
     *         On error conditions, a JSON object is returned with details.
     * @see UserProfilePayload
     */
    @GET
    @Path("/profiles/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserProfileById(
            @PathParam("userId") String userId,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            final Response response = RestUtilities.make200OkResponse(UserManager.getInstance().getUserProfileById(LocaleId.en_us, userId));
            SystemManager.getInstance().setResponseTime(GET_USER_PROFILE_BY_ID_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
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

    /**
     * <p><Use this method to get an inbox for the current session./p>
     * <p>This method may be used by both anonymous and logged in (authenticated) users.
     * If the user is <i>anonymous</i> (not logged in), only a group open to anonymous
     * users is acceptable. In the latter case, it is not necessary to join a group
     * to get the inbox.</p>
     * <p>If a group id is requested for a group that is not open
     * to anonymous users, then the user must have already logged in to access it.
     * Moreover, the user must have already joined (i.e., be a member of) the group.</p>
     * <p>Various query parameters are available to modify this query.</p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> users/inbox</div>
     *
     * @param groupId       Required <b>query parameter</b>: the inbox group id.
     * @param inboxNumber   Optional <b>query parameter</b>: the inbox number to fetch. If not provided,
     *                      inboxes are fetched in sequential order within the group on each request.
     * @param start         Optional <b>query parameter</b>: the start index of the inbox rows.
     * @param count         Optional <b>query parameter</b>: the number of rows to fetch in a row.
     * @param sortFieldName Optional <b>query parameter</b>: the name of an inbox field for sorting the results
     * @param sortDirection Optional <b>query parameter</b>: the direction of the sort as
     *                      an integer: +1 means ascending, -1 means descending. Default is descending.
     * @param blahTypeId    Optional <b>query parameter</b>: a blah type id with which to filter the results.
     * @param request       Internal: the request object
     * @return Returns an inbox JSON object as an array of inbox blah items with http code 200.
     *         If a group has no blahs, this will return an empty array. If the inbox number if not specified,
     *         inboxes are rotated in a monotonically increasing inbox number order, circling back to the
     *         first inbox when the maximum inbox number has been reached.
     *         On error conditions, a JSON object is returned with details.
     */
    @GET
    @Path("/inbox")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInbox(
            @QueryParam("groupId") String groupId,
            @QueryParam("in") Integer inboxNumber,
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count,
            @QueryParam("sort") String sortFieldName,
            @QueryParam("sortDir") Integer sortDirection,
            @QueryParam("type") String blahTypeId,  // TODO would be nice to get rid of type option (to reduce db index size)
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            final Response response = RestUtilities.make200OkResponse(BlahManager.getInstance().getInbox(LocaleId.en_us, groupId, request, inboxNumber, blahTypeId, start, count, sortFieldName, sortDirection));
            SystemManager.getInstance().setResponseTime(GET_ANONYMOUS_INBOX_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

//    /**
//     * <p><This should now be obsoleted./p>
//     * <div><b>METHOD:</b> </div>
//     * <div><b>URL:</b> </div>
//     * @param userId
//     * @param groupId
//     * @param inboxNumber
//     * @param start
//     * @param count
//     * @param sortFieldName
//     * @param sortDirection
//     * @param blahTypeId
//     * @return
//     */
//    @GET
//    @Path("/{userId}/inbox")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getUserInbox(
//            @PathParam("userId") String userId,
//            @QueryParam("groupId") String groupId,
//            @QueryParam("in") Integer inboxNumber,
//            @QueryParam("start") Integer start,
//            @QueryParam("count") Integer count,
//            @QueryParam("sort") String sortFieldName,
//            @QueryParam("sortDir") Integer sortDirection,
//            @QueryParam("type") String blahTypeId) { // TODO would be nice to get rid of type option (to reduce db index size)
//        try {
//            final long s = System.currentTimeMillis();
//            final Response response = RestUtilities.make200OkResponse(BlahManager.getInstance().getUserInbox(LocaleId.en_us, userId, groupId, inboxNumber, blahTypeId, start, count, sortFieldName, sortDirection));
//            SystemManager.getInstance().setResponseTime(GET_USER_INBOX_OPERATION, (System.currentTimeMillis() - s));
//            return response;
//        } catch (InvalidRequestException e) {
//            return RestUtilities.make400InvalidRequestResponse(e);
//        } catch (ResourceNotFoundException e) {
//            return RestUtilities.make404ResourceNotFoundResponse(e);
//        } catch (StateConflictException e) {
//            return RestUtilities.make409StateConflictResponse(e);
//        } catch (SystemErrorException e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(e);
//        } catch (Exception e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(e);
//        }
//    }

    /**
     * <p>Use this method to get a user record by user id</p>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> users/{userId}</div>
     *
     * @param userId         The user id
     * @param stats          Optional <b>query parameter</b>: a boolean. If true, include
     *                       a user statistics record along with the standard user information.
     *                       Default is false.
     * @param byUsername     Optional <b>query parameter</b>: If true, then the user id
     *                       is interpreted as the username.
     * @param statsStartDate Optional <b>query parameter</b>: when stats is true, this is used
     *                       to filter the stats records with this as a start date (inclusive).
     *                       Format is yymmdd (e.g., August 27, 2012 is 120827).
     * @param statsEndDate   Optional <b>query parameter</b>: when stats is true, this is used
     *                       to filter the stats records with this as a end date (inclusive).
     *                       Format is yymmdd (e.g., August 27, 2012 is 120827).
     *                       A start date is required whenever an end date is provided (we don't
     *                       want to retrieve stats until the beginning of time).
     * @return Returns with the user record payload (possibly including statistics) with
     *         an http status 200.
     *         If there is an error in the request, the code 400 (BAD REQUEST) is sent.
     *         If there is no user with the specified identifier, the code 404 (NOT FOUND) is sent.
     *         On error conditions, a JSON object is returned with details.
     */
    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserById(
            @PathParam("userId") String userId,
            @QueryParam("stats") boolean stats,
            @QueryParam("u") boolean byUsername,
            @QueryParam("s") String statsStartDate,
            @QueryParam("e") String statsEndDate,
            @Context HttpServletRequest request) {

        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            final Response response = RestUtilities.make200OkResponse(UserManager.getInstance().getUserById(LocaleId.en_us, userId, byUsername, stats, statsStartDate, statsEndDate));
            SystemManager.getInstance().setResponseTime(GET_USER_BY_ID_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p><b>DO NOT USE. ONLY FOR DEBUGGING WILL BE REMOVED OR SEQUESTERED IN A FUTURE VERSION.</b></p>
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count,
            @QueryParam("sort") String sortFieldName,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            final Response response = RestUtilities.make200OkResponse(UserManager.getInstance().getUsers(LocaleId.en_us, start, count, sortFieldName));
            SystemManager.getInstance().setResponseTime(GET_USERS_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }


    /**
     * <p>No longer in use</p>
     * <div><b>METHOD:</b> </div>
     * <div><b>URL:</b> </div>
     *
     * @param validationCode
     * @param methodKey
     * @param operation
     * @return
     */
    @POST
    @Path("/recover")
    @Produces(MediaType.APPLICATION_JSON)
    public Response recoverUser(
            @QueryParam("r") String validationCode,  // if recoveryMethod == "r", this is the validation code
            @QueryParam("k") String methodKey, // e.g., if recoveryMethod == "e", this is an email address
            @QueryParam("m") String operation,
            @Context HttpServletRequest request) {   // either "e" (email) or "r" (recover via code)
        try {
            final long start = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            final UserProfilePayload payload = UserManager.getInstance().recoverUser(LocaleId.en_us, operation, methodKey, validationCode);
            final Response response = (payload == null) ? RestUtilities.make202AcceptedResponse() : RestUtilities.make200OkResponse(payload);
            SystemManager.getInstance().setResponseTime(RECOVER_USER_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>No longer in use</p>
     * <div><b>METHOD:</b> </div>
     * <div><b>URL:</b> </div>
     *
     * @param validationCode
     * @return
     */
    @POST
    @Path("/validate/{validationCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateUser(
            @PathParam("validationCode") String validationCode,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            UserManager.getInstance().validateUser(LocaleId.en_us, validationCode);
            final Response response = RestUtilities.make202AcceptedResponse();
            SystemManager.getInstance().setResponseTime(VALIDATE_USER_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        } catch (StateConflictException e) {
            return RestUtilities.make409StateConflictResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }
}
