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
import java.util.HashMap;
import java.util.Map;

/**
 * <p>User-related API methods.</p>
 * <div>Note that some methods require authentication (previous login) to be accessed.</div>
 *
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
    private static final String LOGIN_USER_OPERATION = "loginUser";

    /**
     * <p>Use this method to check whether a username is available to use
     * as the username in a new user account.</p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> users/check/username/{username}</div>
     *
     * @param username <i>Path Parameter:</i> The username. Must be at least three
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
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> users/login/check</div>
     *
     * @return Returns http status 200 with a JSON payload containing a field
     *         named 'loggedIn' which is set to 'Y' if the user is logged in and
     *         to 'N' if the user is not logged in.
     */
    @GET
    @Path("/login/check")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkLogin(@Context HttpServletRequest request) {
        final Map<String, String> entity = new HashMap<String, String>(1);
        if (BlahguaSession.isAuthenticated(request)) {
            entity.put("loggedIn", "Y");
        } else {
            entity.put("loggedIn", "N");
        }
        return RestUtilities.make200OkResponse(entity);
    }

    /**
     * <p>Use this method to log in a user.</p>
     * <p/>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> users/login</div>
     *
     * @param entity A JSON entity (a UserPayload) containing a username and a password.
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
    public Response loginUser(UserPayload entity,
                              @Context HttpServletRequest request) {
        try {
            final long start = System.currentTimeMillis();
            final String username = (String) entity.get(UserDAOConstants.USERNAME);
            final String password = (String) entity.get(UserDAOConstants.PASSWORD);
            UserManager.getInstance().loginUser(LocaleId.en_us, username, password, request);
            final Response response = RestUtilities.make202AcceptedResponse();
            SystemManager.getInstance().setResponseTime(LOGIN_USER_OPERATION, (System.currentTimeMillis() - start));
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
     * <p>Use this method to log out a user.</p>
     * <p>This method does not require any parameters.</p>
     * <p/>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> users/login</div>
     *
     * @return If it succeeds, returns an http status 202 (ACCEPTED).
     */
    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logoutUser(@Context HttpServletRequest request) {
        try {
            BlahguaSession.destroySession(request);
            return RestUtilities.make202AcceptedResponse();
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Use this method to register a user.</p>
     * <p/>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> users</div>
     *
     * @param entity A JSON entity (a UserPayload) containing the username and password.
     * @return If successful, returns an http status code of 201 (CREATED).
     *         If a user with the username already exists, it will return 409 (CONFLICT).
     *         If the request is invalid, it will return 400 (BAD REQUEST).
     *         On error conditions, a JSON object is returned with details.
     * @see UserPayload
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerUser(UserPayload entity,
                                 @Context UriInfo uri,
                                 @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            final String username = (String) entity.get(UserDAOConstants.USERNAME);
            final String password = (String) entity.get(UserDAOConstants.PASSWORD);
            final UserPayload payload = UserManager.getInstance().registerUser(LocaleId.en_us, username, password);
            final Response response = RestUtilities.make201CreatedResourceResponse(payload, new URI(uri.getAbsolutePath() + payload.getId()));
            SystemManager.getInstance().setResponseTime(CREATE_USER_OPERATION, (System.currentTimeMillis() - s));
            return response;
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
     * <p>Use this method to create a user profile.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> users/profiles</div>
     *
     * @param entity A JSON entity (a UserProfilePayload) containing the user profile data to set.
     * @return If successful, returns an http status code of 201 (CREATED).
     *         If a profile object already exists, it will return 409 (CONFLICT).
     *         If the request is invalid, it will return 400 (BAD REQUEST).
     *         If the user is not authorized to access this method, returns 401.
     *         On error conditions, a JSON object is returned with details.
     * @see UserProfilePayload
     */
    @POST
    @Path("/profiles")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUserProfile(
            UserProfilePayload entity,
            @Context UriInfo uri,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            entity = UserManager.getInstance().createOrUpdateUserProfile(LocaleId.en_us, entity, userId, true);
            final Response response = RestUtilities.make201CreatedResourceResponse(entity, new URI(uri.getAbsolutePath() + entity.getId()));
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
     * <p>Use this method to set, update or delete either the user's email address
     * or challenge question from his account.</p>
     * <p>Note that the email address is no longer set in the profile: it is set in the user's account.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     *
     * @param entity A JSON entity containing the following fields:
     *               <div>'e' := the value is an email address string. If set to null,
     *               the email address will be removed from the user account. If this
     *               field is not provided in the entity, it is ignored (it is neither
     *               set, updated, nor deleted).</div>
     *               <div>'q' := an answer to a challenge question (a string). If set to null,
     *               the challenge question will be removed from the user account. If this
     *               field is not provided in the entity, it is ignored (it is neither
     *               set, updated, nor deleted).</div>
     * @return Returns an http status of 202 (ACCEPTED) if the address was accepted.
     *         If there is a security problem, returns status of 401.
     *         On error conditions, a JSON object is returned with details.
     */
    @POST
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setAccountInfo(
            Map<String, String> entity,
            @Context HttpServletRequest request) {
        try {
            final String username = BlahguaSession.ensureAuthenticated(request, false);
            UserManager.getInstance().setUserAccountData(username, entity.containsKey("e"), entity.get("e"), entity.containsKey("q"), entity.get("q"));
            return RestUtilities.make204OKNoContentResponse();
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
     * <p>Same as POST version of this method.</p>
     *
     * @see #setAccountInfo(java.util.Map, javax.servlet.http.HttpServletRequest)
     */
    @PUT
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAccount(
            Map<String, String> entity,
            @Context HttpServletRequest request) {
        return setAccountInfo(entity, request);
    }

    /**
     * <p>Use this method to update profile fields.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> PUT</div>
     * <div><b>URL:</b> users/profiles</div>
     *
     * @param entity A JSON entity (a UserProfilePayload) containing the profile fields to change.
     * @return If successful, returns http code 204 (OK NO CONTENT).
     *         If the request is invalid, returns 400 (BAD REQUEST).
     *         If the profile has not been created, returns 404 (NOT FOUND).
     *         If the user may not access this method, returns 401 (UNAUTHORIZED).
     *         On error conditions, a JSON object is returned with details.
     * @see UserProfilePayload
     */
    @PUT
    @Path("/profiles")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUserProfile(
            UserProfilePayload entity,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            UserManager.getInstance().createOrUpdateUserProfile(LocaleId.en_us, entity, userId, false);
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
     * <p>Use this method to obtain a string descriptor of the user's profile.</p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> users/profiles/descriptor/{userId}</div>
     *
     * @param userId <i>Path Parameter</i>. The user's id.
     * @return An http status of 200 with a JSON entity consisting of a
     *         single field named 'd' whose value is a string--the descriptor.
     *         If the request is invalid, returns 400 (BAD REQUEST).
     *         If the profile has not been created, returns 404 (NOT FOUND).
     *         On error conditions, a JSON object is returned with details.
     */
    @GET
    @Path("/profiles/descriptor/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getUserDescriptorString(
            @PathParam("userId") String userId,
            @Context HttpServletRequest request) {
        try {
            return RestUtilities.make200OkResponse(UserManager.getInstance().getUserProfileDescriptor(LocaleId.en_us, request, userId));
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (Exception e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        }
    }

    /**
     * <p>Returns the schema for the user profile record. The schema specifies
     * all fields in the user profile and their acceptable values (constraints), as appropriate.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> users/profiles</div>
     *
     * @return Returns a user profile schema JSON entity (a UserProfileSchema)
     *         with an http status of 200.
     * @see main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema
     */
    @GET
    @Path("/profiles")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getUserProfileSchema(@Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request, true);
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
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> users/profiles/{userId}</div>
     *
     * @param userId <i>Path Parameter</i>. The user's id.
     * @return If successful, returns an http code of 200 (OK) with a JSON entity (a UserProfilePayload)
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
            final String currUserId = BlahguaSession.ensureAuthenticated(request, true);
            final Response response = RestUtilities.make200OkResponse(UserManager.getInstance().getUserProfileById(LocaleId.en_us, (userId == null) ? currUserId : userId));
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
     * <p>User this method to update a user's username.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> PUT</div>
     * <div><b>URL:</b> users/update/username/{username}</div>
     *
     * @param username <i>Path Parameter</i>. The new username.
     * @return Returns http status code 204 (NO CONTENT) on success.
     *         If the user or user account doesn't exist, returns 404 (NOT FOUND).
     *         If the input is invalid, returns 400 (BAD REQUEST).
     *         If username is already taken, returns 409 (CONFLICT).
     *         On error conditions, a JSON object is returned with details.
     * @see #checkUsername(String)
     */
    @PUT
    @Path("/update/username/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUsername(
            @PathParam("username") String username,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request, true);
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
     * <p>User this method to update a user's password.</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> PUT</div>
     * <div><b>URL:</b> users/update/password/{password}</div>
     *
     * @param password <i>Path Parameter</i>. The new password.
     * @return Returns http status code 204 (NO CONTENT) on success.
     *         If the user or user account doesn't exist, returns 404 (NOT FOUND).
     *         If the input is invalid, returns 400 (BAD REQUEST).
     *         If username is already taken, returns 409 (CONFLICT).
     *         On error conditions, a JSON object is returned with details.
     */
    @PUT
    @Path("/update/password/{password}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(
            @PathParam("password") String password,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request, true);
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
     * <p>Use this method when the user forgets his password.</p>
     * <p>The user must have an email address in his profile. If so, the user
     * will receive an email with a link that will automatically log him into his account.</p>
     * <p>This method requires two inputs:</p>
     * <div>The user's username</div>
     * <div>The user's email address. This is just a security challenge: it is verified
     * against the email address in the user's profile.</div>
     * <div>The answer to the one and only challenge question.</div>
     * <p/>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> users/recover/user</div>
     *
     * @param entity An entity containing the following fields: 'u' whose value is a string, the username.
     *               'a' whose value is a string, the answer to the one and only security question.
     *               'e' whose value is a string, the user-supplied email address. Note that the latter
     *               is only used as one more way to try to confirm that the user knows his email address
     *               (which is not enough by itself to give us much confidence, but with the challenge
     *               answer it might be more of a deterrent).
     * @return If user has registered an email account, returns http status 204 (NO CONTENT),
     *         in which case it means that an email has been sent with a link that will log in the user.
     *         If the user doesn't have a registered email address, returns http status 404 (NOT FOUND).
     *         Never returns a payload.
     */
    @POST
    @Path("/recover/user")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response recoverUser(
            Map<String, String> entity,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            UserManager.getInstance().recoverUser(LocaleId.en_us, request, entity.get("u"), entity.get("e"), entity.get("a"));
            final Response response = RestUtilities.make204OKNoContentResponse();
            SystemManager.getInstance().setResponseTime(RECOVER_USER_OPERATION, (System.currentTimeMillis() - s));
            return response;
        } catch (SystemErrorException e) {
            return RestUtilities.make500AndLogSystemErrorResponse(e);
        } catch (ResourceNotFoundException e) {
            return RestUtilities.make404ResourceNotFoundResponse(e);
        } catch (InvalidRequestException e) {
            return RestUtilities.make400InvalidRequestResponse(e);
        } catch (InvalidAuthorizedStateException e) {
            return RestUtilities.make401UnauthorizedRequestResponse(e);
        }
    }

//
//    /**
//     * <p>Directly or indirectly received from user clicking on a recovery URL.</p>
//     * <p><b>TODO: </b>deal with UI page.</p>
//     * <p/>
//     * <div><b>METHOD:</b> GET</div>
//     * <div><b>URL:</b> users/recover/user/{recoveryCode}</div>
//     *
//     * @param recoveryCode <i>Path Parameter:</i> The recovery code sent to the user in an email or whatever.
//     * @return Returns an http status 202 (ACCEPTED) if the user is now logged in.
//     *         If there is an error in the request, the code 400 (BAD REQUEST) is sent.
//     *         If there is no user with the specified identifier, the code 404 (NOT FOUND) is sent.
//     *         If there is an authorization or security problem, returns 401 (UNAUTHORIZED).
//     *         On error conditions, a JSON object is returned with details.
//     */
//    @GET
//    @Path("/recover/user/{recoveryCode}/{username}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response recoverUser(
//            @PathParam("recoveryCode") String recoveryCode,
//            @PathParam("username") String encryptedCanonicalUsername,
//            @Context HttpServletResponse httpResponse) {
//        try {
//            final long start = System.currentTimeMillis();
//            UserManager.getInstance().recoverUserAndRedirectToMainPage(LocaleId.en_us, httpResponse, recoveryCode, encryptedCanonicalUsername);
//            SystemManager.getInstance().setResponseTime(RECOVER_USER_OPERATION, (System.currentTimeMillis() - start));
//            return
//        } catch (SystemErrorException e) {
//            return RestUtilities.make500AndLogSystemErrorResponse(e);
//        } catch (InvalidAuthorizedStateException e) {
//            return RestUtilities.make401UnauthorizedRequestResponse(e);
//        }
//    }

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
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> users/inbox</div>
     *
     * @param groupId       <i>Query Parameter:</i> Required. The inbox group id.
     * @param inboxNumber   <i>Query Parameter:</i> Optional. The inbox number to fetch. If not provided,
     *                      inboxes are fetched in sequential order within the group on each request.
     * @param start         <i>Query Parameter:</i> Optional. The start index of the inbox rows.
     * @param count         <i>Query Parameter:</i> Optional. The number of rows to fetch in a row.
     * @param sortFieldName <i>Query Parameter:</i> Optional. The name of an inbox field for sorting the results
     * @param sortDirection <i>Query Parameter:</i> Optional. <b>Would like to remove this option.</b> The direction of the sort as
     *                      an integer: +1 means ascending, -1 means descending. Default is descending.
     * @param blahTypeId    <i>Query Parameter:</i> Optional. <b>Would like to remove this option.</b> A blah type id with which to filter the results.
     * @return Returns an inbox JSON object as an array of inbox blah entities (InboxBlahPayload entities) with http code 200.
     *         If a group has no blahs, this will return an empty array. If the inbox number if not specified,
     *         inboxes are rotated in a monotonically increasing inbox number order, circling back to the
     *         first inbox when the maximum inbox number has been reached.
     *         On error conditions, a JSON object is returned with details.
     * @see main.java.com.eweware.service.base.payload.InboxBlahPayload
     */
    @GET
    @Path("/inbox")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInbox(
            @QueryParam("groupId") String groupId,
            @QueryParam("in") Integer inboxNumber,
            @QueryParam("start") Integer start,
            @QueryParam("count") Integer count,
            @QueryParam("sort") String sortFieldName,  // TODO would be nice to get rid of type option (to reduce db index size)
            @QueryParam("sortDir") Integer sortDirection,  // TODO would be nice to get rid of type option (to reduce db index size)
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

    /**
     * <p>Use this method to get a user record by user id</p>
     * <p><i>User must be logged in to use this method.</i></p>
     * <p/>
     * <div><b>METHOD:</b> GET</div>
     * <div><b>URL:</b> users/{userId}</div>
     *
     * @param userId         <i>Path Parameter</i>. The user id.
     * @param stats          <i>Query Parameter:</i> Optional. If true, include
     *                       a user statistics record along with the standard user information.
     *                       Default is false.
     * @param byUsername     <i>Query Parameter:</i> Optional. If true, then the user id
     *                       is interpreted as the username.
     * @param statsStartDate <i>Query Parameter:</i> Optional. When stats is true, this is used
     *                       to filter the stats records with this as a start date (inclusive).
     *                       Format is yymmdd (e.g., August 27, 2012 is 120827).
     * @param statsEndDate   <i>Query Parameter:</i> Optional. When stats is true, this is used
     *                       to filter the stats records with this as a end date (inclusive).
     *                       Format is yymmdd (e.g., August 27, 2012 is 120827).
     *                       A start date is required whenever an end date is provided (we don't
     *                       want to retrieve stats until the beginning of time).
     * @return Returns with the user entity (a UserPayload) possibly including statistics) with
     *         an http status 200.
     *         If there is an error in the request, the code 400 (BAD REQUEST) is sent.
     *         If there is no user with the specified identifier, the code 404 (NOT FOUND) is sent.
     *         On error conditions, a JSON object is returned with details.
     * @see UserPayload
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
            BlahguaSession.ensureAuthenticated(request, true);
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
            BlahguaSession.ensureAuthenticated(request, true);
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
     * <p><b>No longer in use</b></p>
     */
    @POST
    @Path("/validate/{validationCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateUser(
            @PathParam("validationCode") String validationCode,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request, true);
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