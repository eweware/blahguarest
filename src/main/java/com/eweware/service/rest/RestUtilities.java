package main.java.com.eweware.service.rest;

import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.payload.ErrorResponsePayload;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rk@post.harvard.edu
 *         Date: 6/15/12 Time: 2:41 PM
 */
public final class RestUtilities {

    private static final Logger logger = Logger.getLogger("RestUtilities");

    public static final Response make500AndLogSystemErrorResponse(HttpServletRequest request, BaseException e) {
        printHeaders(request);
        logger.log(Level.SEVERE, "System error", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(e.getErrorCode(), e.getMessage(), e.getEntity())).build();
    }

    public static void printHeaders(HttpServletRequest request) {
        final Enumeration headers = request.getHeaderNames();
        final StringBuilder b = new StringBuilder();
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
        logger.warning(b.toString());
    }

    public static final Response make500AndLogSystemErrorResponse(BaseException e) {
        String msg = e.getMessage();
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            msg += ": "+e.getCause().getMessage();
        }
        logger.log(Level.SEVERE, "System error", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(e.getErrorCode(), msg, e.getEntity())).build();
    }

    public static final Response make500AndLogSystemErrorResponse(HttpServletRequest request, Throwable e) {
        printHeaders(request);
        logger.log(Level.SEVERE, "System error", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(ErrorCodes.SERVER_SEVERE_ERROR, e.getMessage())).build();
    }

    public static final Response make500AndLogSystemErrorResponse(Throwable e) {
        String msg = e.getMessage();
        logger.log(Level.SEVERE, "System error", e);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(ErrorCodes.SERVER_SEVERE_ERROR, msg)).build();
    }

    public static Response make404ResourceNotFoundResponse(ResourceNotFoundException e) {
        String msg = e.getMessage();
        logger.log(Level.WARNING, "Resource Not Found: " + e.getMessage());
        return Response.status(Response.Status.NOT_FOUND).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(e.getErrorCode(), msg, e.getEntity())).build();
    }

    public static Response make409StateConflictResponse(String msg, int errorCode) {
        logger.log(Level.WARNING, "State Conflict");
        return Response.status(Response.Status.CONFLICT).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(errorCode, msg, null)).build();
    }

    public static Response make409StateConflictResponse(StateConflictException e) {
        logger.log(Level.WARNING, "State Conflict: " + e.getMessage());
        return Response.status(Response.Status.CONFLICT).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(e.getErrorCode(), e.getMessage(), e.getEntity())).build();
    }

    public static Response make400InvalidRequestResponse(InvalidRequestException e) {
        logger.log(Level.WARNING, "Invalid Request: " + e.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(e.getErrorCode(), e.getMessage(), e.getEntity())).build();
    }

    public static Response make401UnauthorizedRequestResponse(HttpServletRequest request, InvalidAuthorizedStateException e) {
        printHeaders(request);
        logger.log(Level.WARNING, "Unauthorized: " + e.getMessage());
        return Response.status(Response.Status.UNAUTHORIZED).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(e.getErrorCode(), e.getMessage(), e.getEntity())).build();
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
}
