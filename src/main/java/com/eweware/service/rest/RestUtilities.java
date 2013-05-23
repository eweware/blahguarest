package main.java.com.eweware.service.rest;

import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.payload.ErrorResponsePayload;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 *         Date: 6/15/12 Time: 2:41 PM
 */
public final class RestUtilities {

    private static final Logger logger = Logger.getLogger(RestUtilities.class.getName());

    private static final String SYSTEM_ERROR = "System error";
    private static final String APPLICATION_ERROR = "Application error";
    private static final String AUTHORIZATION_ERROR = "Authorization error";

    public static final Response make500AndLogSystemErrorResponse(HttpServletRequest request, Throwable e) {
        logger.log(Level.SEVERE, "Internal System Error. Info:\n" + getRequestInfo(request), e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(ErrorCodes.SERVER_SEVERE_ERROR, getFullMessage(e), SYSTEM_ERROR)).build();
    }

    public static Response make404ResourceNotFoundResponse(HttpServletRequest request, ResourceNotFoundException e) {
        logger.log(Level.WARNING, "Resource Not Found. Info: " + getRequestInfo(request), e);
        return Response.status(Response.Status.NOT_FOUND).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(e.getErrorCode(), APPLICATION_ERROR, e.getEntity())).build();
    }

    public static Response make409StateConflictResponse(HttpServletRequest request, StateConflictException e) {
        logger.log(Level.WARNING, "State Conflict. Info: " + getRequestInfo(request), e);
        return Response.status(Response.Status.CONFLICT).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(e.getErrorCode(), APPLICATION_ERROR, e.getEntity())).build();
    }

    public static Response make400InvalidRequestResponse(HttpServletRequest request, InvalidRequestException e) {
        logger.log(Level.WARNING, "Invalid Request. Info: " + getRequestInfo(request), e);
        return Response.status(Response.Status.BAD_REQUEST).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(e.getErrorCode(), APPLICATION_ERROR, e.getEntity())).build();
    }

    public static Response make401UnauthorizedRequestResponse(HttpServletRequest request, InvalidAuthorizedStateException e) {
        logger.log(Level.WARNING, "Unauthorized Access. Info: " + getRequestInfo(request), e);
        return Response.status(Response.Status.UNAUTHORIZED).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(e.getErrorCode(), AUTHORIZATION_ERROR, e.getEntity())).build();
    }

    public static final Response make201CreatedResourceResponse(Object entity, URI location) {
        return Response.status(Response.Status.CREATED).header("Cache-Control", "no-cache").entity(entity).location(location).header("Content-Location",location).build();
    }

    public static Response make202AcceptedResponse() {
        return Response.status(Response.Status.ACCEPTED).header("Cache-Control", "no-cache").build();
    }

    public static Response make200OkResponse(Object entity) {
        return Response.status(Response.Status.OK).header("Cache-Control", "no-cache").entity(entity).build();
    }

    public static Response make204OKNoContentResponse() {
        return Response.status(Response.Status.NO_CONTENT).header("Cache-Control", "no-cache").build();
    }

    public static String getRequestInfo(HttpServletRequest request) {
        final Enumeration headers = request.getHeaderNames();
        final StringBuilder b = new StringBuilder();
        final String remoteHost = request.getRemoteHost();
        if (remoteHost != null) {
            b.append("Remote Host: ");
            b.append(remoteHost);
        }
        final String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null) {
            b.append("\nRemote Addr: ");
            b.append(remoteAddr);
            b.append("\nRemote Port: ");
            b.append(request.getRemotePort());
        }
        final String remoteUser = request.getRemoteUser();
        if (remoteUser != null) {
            b.append("\nRemote User: ");
            b.append(remoteUser);
        }
        b.append("\nHEADERS:\n");
        while (headers.hasMoreElements()) {
            String name = (String) headers.nextElement();
            final String value = request.getHeader(name);
            if (value != null) {
                b.append(name);
                b.append("=");
                b.append(value);
                b.append("\n");
            }
        }
        return b.toString();
    }

    private static String getFullMessage(Throwable e) {
        final StringBuilder b = new StringBuilder();
        getFullMessage1(e, b);
        return b.toString();
    }

    private static void getFullMessage1(Throwable e, StringBuilder b) {
        final String msg = e.getMessage();
        if (msg != null) {
            b.append(msg);
            b.append("\n");
        }
        final Throwable cause = e.getCause();
        if (cause != null) {
            getFullMessage1(cause, b);
        }
    }
}
