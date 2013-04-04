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

    private static final String GET_USER_INFO_OPERATION = "getUserInfo";
    private static final String GET_USER_INFO_FOR_BLAH_OPERATION = "getUserInfo4Blah";
    private static final String GET_ANONYMOUS_INBOX_OPERATION = "getUserInbox";
    private static final String GET_USER_PROFILE_BY_ID_OPERATION = "getUserProfileById";
    private static final String GET_USER_PROFILE_SCHEMA_OPERATION = "getUserProfileSchema";
    private static final String UPDATE_USERNAME_OPERATION = "updateUser";
    private static final String UPDATE_PASSWORD_OPERATION = "updatePwd";
    private static final String UPDATE_USER_PROFILE_OPERATION = "updateUserProfile";
    private static final String CREATE_USER_OPERATION = "createUser";
    private static final String CREATE_USER_PROFILE_OPERATION = "createUserProfile";
    private static final String RECOVER_USER_OPERATION = "recoverUser";
    private static final String LOGIN_USER_OPERATION = "loginUser";

    private UserManager userManager;
    private SystemManager systemManager;
    private BlahManager blahManager;

    /**
     * <p>Use this method to check whether a username is available to use
     * as the username in a new user account.</p>
     * <p/>
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> users/check/username</div>
     *
     * @param entity Expects a JSON entity with the username in a
     *               parameter named 'U'. The username be at least three
     *               and less than 64 characters long.
     * @return If successful (username is available), returns an http code of 204 (OK NO CONTENT).
     *         If the request is invalid, it returns 400 (BAD REQUEST).
     *         If the username already exists, it returns 409 (CONFLICT).
     *         On error conditions, a JSON object is returned with details.
     * @see UserDAOConstants
     */
    @POST
    @Path("/check/username")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkUsername(
            Map<String, String> entity) {
        try {
            final String username = entity.get("U");
            getUserManager().ensureUserExistsByUsername(username);
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
     * @see UserDAOConstants
     */
    @GET
    @Path("/login/check")
    @Consumes(MediaType.APPLICATION_JSON)
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
     *               The rules for username and password length and structure are in
     *               the Login methods cited below.
     * @return If it succeeds, returns an http status 202 (ACCEPTED).
     *         Else, the following http status codes are possible: 400 (either
     *         the request was invalid or the user is not authorized to login),
     *         or 404 (user resource does not exist). In either case a JSON
     *         error message will specify more details.
     * @see main.java.com.eweware.service.user.validation.Login#ensureUsernameString(String)
     * @see main.java.com.eweware.service.user.validation.Login#ensurePasswordString(String)
     * @see UserDAOConstants
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
            final String username = (String) entity.get(UserPayload.USERNAME);
            final String password = (String) entity.get(UserPayload.PASSWORD);
            getUserManager().loginUser(LocaleId.en_us, username, password, request);
            final Response response = RestUtilities.make202AcceptedResponse();
            getSystemManager().setResponseTime(LOGIN_USER_OPERATION, (System.currentTimeMillis() - start));
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
     * @see UserDAOConstants
     */
    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
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
     * @see UserDAOConstants
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
            final String username = (String) entity.get(UserPayload.USERNAME);
            final String password = (String) entity.get(UserPayload.PASSWORD);
            final UserPayload payload = getUserManager().registerUser(LocaleId.en_us, username, password);
            final Response response = RestUtilities.make201CreatedResourceResponse(payload, new URI(uri.getAbsolutePath() + payload.getId()));
            getSystemManager().setResponseTime(CREATE_USER_OPERATION, (System.currentTimeMillis() - s));
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
     *               No user id or username is required.
     * @return If successful, returns an http status code of 201 (CREATED).
     *         If a profile object already exists, it will return 409 (CONFLICT).
     *         If the request is invalid, it will return 400 (BAD REQUEST).
     *         If the user is not authorized to access this method, returns 401.
     *         On error conditions, a JSON object is returned with details.
     * @see main.java.com.eweware.service.base.store.dao.UserProfileDAOConstants
     * @see UserProfilePayload
     * @see main.java.com.eweware.service.base.store.dao.UserProfileDAOConstants
     */
    @POST
    @Path("/profile/info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUserProfile(
            UserProfilePayload entity,
            @Context UriInfo uri,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            entity = getUserManager().createOrUpdateUserProfile(LocaleId.en_us, entity, userId, true);
            final Response response = RestUtilities.make201CreatedResourceResponse(entity, new URI(uri.getAbsolutePath() + entity.getId()));
            getSystemManager().setResponseTime(CREATE_USER_PROFILE_OPERATION, (System.currentTimeMillis() - s));
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
     *               <div>'E' := the value is an email address string. If set to null,
     *               the email address will be removed from the user account. If this
     *               field is not provided in the entity, it is ignored (it is neither
     *               set, updated, nor deleted).</div>
     *               <div>'A' := an answer to a challenge question (a string). If set to null,
     *               the challenge question will be removed from the user account. If this
     *               field is not provided in the entity, it is ignored (it is neither
     *               set, updated, nor deleted).</div>
     * @return Returns an http status of 202 (ACCEPTED) if the address was accepted.
     *         If there is a security problem, returns status of 401.
     *         On error conditions, a JSON object is returned with details.
     * @see UserDAOConstants
     */
    @POST
    @Path("/account")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response setAccountInfo(
            Map<String, String> entity,
            @Context HttpServletRequest request) {
        try {
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            getUserManager().setUserAccountData(userId, entity.containsKey("E"), entity.get("E"), entity.containsKey("A"), entity.get("A"));
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
     * @see UserDAOConstants
     * @see main.java.com.eweware.service.base.store.dao.UserAccountDAOConstants
     */
    @PUT
    @Path("/account")
    @Consumes(MediaType.APPLICATION_JSON)
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
     * <div><b>URL:</b> users/profile/info</div>
     *
     * @param entity A JSON entity (a UserProfilePayload) containing the profile fields to change.
     * @return If successful, returns http code 204 (OK NO CONTENT).
     *         If the request is invalid, returns 400 (BAD REQUEST).
     *         If the profile has not been created, returns 404 (NOT FOUND).
     *         If the user may not access this method, returns 401 (UNAUTHORIZED).
     *         On error conditions, a JSON object is returned with details.
     * @see main.java.com.eweware.service.base.store.dao.UserProfileDAOConstants
     * @see UserProfilePayload
     */
    @PUT
    @Path("/profile/info")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUserProfile(
            UserProfilePayload entity,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            getUserManager().createOrUpdateUserProfile(LocaleId.en_us, entity, userId, false);
            final Response response = RestUtilities.make204OKNoContentResponse();
            getSystemManager().setResponseTime(UPDATE_USER_PROFILE_OPERATION, (System.currentTimeMillis() - s));
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
     * <div><b>METHOD:</b> POST</div>
     * <div><b>URL:</b> users/profiles/descriptor</div>
     *
     * @param entity Expects a JSON entity containing the user id in a
     *               field named 'I'
     * @return An http status of 200 with a JSON entity consisting of a
     *         single field named 'd' whose value is a string--the descriptor.
     *         If the request is invalid, returns 400 (BAD REQUEST).
     *         If the profile has not been created, returns 404 (NOT FOUND).
     *         On error conditions, a JSON object is returned with details.
     */
    @POST
    @Path("/descriptor")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getUserDescriptorString(
            Map<String, String> entity,
            @Context HttpServletRequest request) {
        try {
            final String userId = entity.get("I");
            return RestUtilities.make200OkResponse(getUserManager().getUserProfileDescriptor(LocaleId.en_us, request, userId));
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
     * <div><b>URL:</b> users/profile/schema</div>
     *
     * @return Returns a user profile schema JSON entity (a UserProfileSchema)
     *         with an http status of 200.
     * @see main.java.com.eweware.service.base.store.dao.UserProfileDAOConstants
     * @see main.java.com.eweware.service.base.store.dao.schema.UserProfileSchema
     */
    @GET
    @Path("/profile/schema")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getUserProfileSchema(@Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            Response response = RestUtilities.make200OkResponse(getUserManager().getUserProfileSchema(LocaleId.en_us));
            getSystemManager().setResponseTime(GET_USER_PROFILE_SCHEMA_OPERATION, (System.currentTimeMillis() - s));
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
     * <div><b>URL:</b> users/profile/info</div>
     *
     * @return If successful, returns an http code of 200 (OK) with a JSON entity (a UserProfilePayload)
     *         containing the user profile settings.
     *         If there is no profile for this user (or if user doesn't exist), returns 404 (NOT FOUND).
     *         If the request is invalid, returns 400 (BAD REQUEST).
     *         On error conditions, a JSON object is returned with details.
     * @see main.java.com.eweware.service.base.store.dao.UserProfileDAOConstants
     */
    @GET
    @Path("/profile/info")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserProfile(
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            final Response response = RestUtilities.make200OkResponse(getUserManager().getUserProfileById(LocaleId.en_us, (userId == null) ? userId : userId));
            getSystemManager().setResponseTime(GET_USER_PROFILE_BY_ID_OPERATION, (System.currentTimeMillis() - s));
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
     * @param entity Expects a JSON entity containing the username
     *               in a field named 'U'. The rules for username
     *               length and structure are given in the Login citation below.
     * @return Returns http status code 204 (NO CONTENT) on success.
     *         If the user or user account doesn't exist, returns 404 (NOT FOUND).
     *         If the input is invalid, returns 400 (BAD REQUEST).
     *         If username is already taken, returns 409 (CONFLICT).
     *         On error conditions, a JSON object is returned with details.
     * @see main.java.com.eweware.service.user.validation.Login#ensureUsernameString(String)
     */
    @PUT
    @Path("/update/username")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUsername(
            Map<String, String> entity,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            BlahguaSession.ensureAuthenticated(request);
            final String username = entity.get("U");
            getUserManager().updateUsername(LocaleId.en_us, request, username);
            final Response response = RestUtilities.make204OKNoContentResponse();
            getSystemManager().setResponseTime(UPDATE_USERNAME_OPERATION, (System.currentTimeMillis() - s));
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
     * @param entity Expects a JSON entity containing the password
     *               in a field named 'P'. The rules for password
     *               length and structure are given in the Login method
     *               cited below.
     * @return Returns http status code 204 (NO CONTENT) on success.
     *         If the user or user account doesn't exist, returns 404 (NOT FOUND).
     *         If the input is invalid, returns 400 (BAD REQUEST).
     *         If username is already taken, returns 409 (CONFLICT).
     *         On error conditions, a JSON object is returned with details.
     * @see main.java.com.eweware.service.user.validation.Login#ensurePasswordString(String)
     */
    @PUT
    @Path("/update/password")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(
            Map<String, String> entity,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            final String password = entity.get("P");
            getUserManager().updatePassword(LocaleId.en_us, request, userId, password);
            final Response response = RestUtilities.make204OKNoContentResponse();
            getSystemManager().setResponseTime(UPDATE_PASSWORD_OPERATION, (System.currentTimeMillis() - s));
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
     * @param entity An entity containing the following fields: 'U' whose value is a string, the username.
     *               'A' whose value is a string, the answer to the one and only security question.
     *               'E' whose value is a string, the user-supplied email address. Note that the latter
     *               is only used as one more way to try to confirm that the user knows his email address
     *               (which is not enough by itself to give us much confidence, but with the challenge
     *               answer it might be more of a deterrent).
     * @return If user has registered an email account, returns http status 204 (NO CONTENT),
     *         in which case it means that an email has been sent with a link that will log in the user.
     *         If the user doesn't have a registered email address, returns http status 404 (NOT FOUND).
     *         Never returns a payload.
     * @see UserDAOConstants
     * @see main.java.com.eweware.service.base.store.dao.UserAccountDAOConstants
     */
    @POST
    @Path("/recover/user")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response recoverUser(
            Map<String, String> entity,
            @Context HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            getUserManager().recoverUser(LocaleId.en_us, request, entity.get("U"), entity.get("E"), entity.get("A"));
            final Response response = RestUtilities.make204OKNoContentResponse();
            getSystemManager().setResponseTime(RECOVER_USER_OPERATION, (System.currentTimeMillis() - s));
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
     * @see main.java.com.eweware.service.base.store.dao.InboxBlahDAOConstants
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
            final Response response = RestUtilities.make200OkResponse(getBlahManager().getInbox(LocaleId.en_us, groupId, request, inboxNumber, blahTypeId, start, count, sortFieldName, sortDirection));
            getSystemManager().setResponseTime(GET_ANONYMOUS_INBOX_OPERATION, (System.currentTimeMillis() - s));
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
     * <div><b>URL:</b> users/info</div>
     *
     * @param stats          <i>Query Parameter:</i> Optional. If true, include
     *                       a user statistics record along with the standard user information.
     *                       Default is false.
     * @param s <i>Query Parameter:</i> Optional. When stats is true, this is used
     *                       to filter the stats records with this as a start date (inclusive).
     *                       Format is yymmdd (e.g., August 27, 2012 is 120827).
     * @param e   <i>Query Parameter:</i> Optional. When stats is true, this is used
     *                       to filter the stats records with this as a end date (inclusive).
     *                       Format is yymmdd (e.g., August 27, 2012 is 120827).
     *                       A start date is required whenever an end date is provided (we don't
     *                       want to retrieve stats until the beginning of time).
     * @return Returns with the user entity (a UserPayload) possibly including statistics) with
     *         an http status 200.
     *         If there is an error in the request, the code 400 (BAD REQUEST) is sent.
     *         If there is no user with the specified identifier, the code 404 (NOT FOUND) is sent.
     *         On error conditions, a JSON object is returned with details.
     * @see UserDAOConstants
     */
    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserInfo(
            @QueryParam("stats") final boolean stats,
            @QueryParam("s") final String s,
            @QueryParam("e") final String e,
            @Context HttpServletRequest request) {

        try {
            final long start = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            final Response response = RestUtilities.make200OkResponse(getUserManager().getUserInfo(LocaleId.en_us, userId, stats, s, e));
            getSystemManager().setResponseTime(GET_USER_INFO_OPERATION, (System.currentTimeMillis() - start));
            return response;
        } catch (InvalidRequestException e1) {
            return RestUtilities.make400InvalidRequestResponse(e1);
        } catch (InvalidAuthorizedStateException e1) {
            return RestUtilities.make401UnauthorizedRequestResponse(e1);
        } catch (ResourceNotFoundException e1) {
            return RestUtilities.make404ResourceNotFoundResponse(e1);
        } catch (SystemErrorException e1) {
            return RestUtilities.make500AndLogSystemErrorResponse(e1);
        } catch (Exception e1) {
            return RestUtilities.make500AndLogSystemErrorResponse(e1);
        }
    }

    /**
     * <p>Returns information about the logged-in user in relation to the specified blah.</p>
     *
     * @param blahId The blah id
     * @return Returns http status 200 (OK) with the user entity (a UserBlahInfoPayload) including voting information
     *         for the user.
     *         If there is an error in the request, the code 400 (BAD REQUEST) is sent.
     *         If there is no user with the specified identifier, the code 404 (NOT FOUND) is sent.
     *         If the user is not authorized to access this method, returns 401.
     *         On error conditions, a JSON object is returned with details.
     * @see main.java.com.eweware.service.base.payload.UserBlahInfoPayload
     * @see main.java.com.eweware.service.base.store.dao.UserBlahInfoDAOConstants
     */
    @GET
    @Path("info/{blahId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserInfoForBlah(
            @PathParam("blahId") final String blahId,
            @Context final HttpServletRequest request) {
        try {
            final long s = System.currentTimeMillis();
            final String userId = BlahguaSession.ensureAuthenticated(request, true);
            final Response response = RestUtilities.make200OkResponse(
                    getUserManager().getUserInfoForBlah(userId, blahId));
            getSystemManager().setResponseTime(GET_USER_INFO_FOR_BLAH_OPERATION, (System.currentTimeMillis() - s));
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


    private UserManager getUserManager() throws SystemErrorException {
        if (userManager == null) {
            userManager = UserManager.getInstance();
        }
        return userManager;
    }

    private SystemManager getSystemManager() throws SystemErrorException {
        if (systemManager == null) {
            systemManager = SystemManager.getInstance();
        }
        return systemManager;
    }

    private BlahManager getBlahManager() throws SystemErrorException {
        if (blahManager == null) {
            blahManager = BlahManager.getInstance();
        }
        return blahManager;
    }
}