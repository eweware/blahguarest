package main.java.com.eweware.service.rest;

import main.java.com.eweware.service.base.error.*;
import main.java.com.eweware.service.base.payload.ErrorResponsePayload;
import main.java.com.eweware.service.rest.session.SessionState;

import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Date;

/**
 * @author rk@post.harvard.edu
 *         Date: 6/15/12 Time: 2:41 PM
 */
public final class RestUtilities {

    public static final Response make500AndLogSystemErrorResponse(BaseException e) {
        String msg = e.getMessage();
        if (e.getCause() != null && e.getCause().getMessage() != null) {
            msg += ": "+e.getCause().getMessage();
        }
        final String st = stackTraceAsString(e);
        if (st.length() > 0) {
            msg += "\n" + st;
        }
        System.out.println(new Date() + ": " + msg);
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(e.getErrorCode(), msg, e.getEntity())).build();
    }

    public static final Response make500AndLogSystemErrorResponse(Throwable e) {
        String msg = e.getMessage();
        final String st = stackTraceAsString(e);
        if (st.length() > 0) {
            msg += "\n" + st;
        }
        System.out.println(new Date() + ": " + e.getMessage());
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(ErrorCodes.SERVER_SEVERE_ERROR, msg)).build();
    }

    public static Response make404ResourceNotFoundResponse(ResourceNotFoundException e) {
        String msg = e.getMessage();
        final String st = stackTraceAsString(e);
        if (st.length() > 0) {
            msg += "\n" + st;
        }
        return Response.status(Response.Status.NOT_FOUND).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(e.getErrorCode(), msg, e.getEntity())).build();
    }

    public static Response make409StateConflictResponse(String msg, int errorCode) {
        return Response.status(Response.Status.CONFLICT).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(errorCode, msg, null)).build();
    }

    public static Response make409StateConflictResponse(StateConflictException e) {
        String msg = new Date() + ": makeStateConflictResponse: " + e.getMessage();
        final String st = stackTraceAsString(e);
        if (st.length() > 0) {
            msg += "\n" + st;
        }
        return Response.status(Response.Status.CONFLICT).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(e.getErrorCode(), msg, e.getEntity())).build();
    }

    public static Response make400InvalidRequestResponse(InvalidRequestException e) {
        String msg = new Date() + ": makeInvalidRequestException: " + e.getMessage();
        final String st = stackTraceAsString(e);
        if (st.length() > 0) {
            msg += "\n" + st;
        }
        return Response.status(Response.Status.BAD_REQUEST).header("Cache-Control", "no-cache").entity(new ErrorResponsePayload(e.getErrorCode(), e.getMessage(), e.getEntity())).build();
    }

    public static Response make401UnauthorizedRequestResponse(InvalidAuthorizedStateException e) {
        String msg = new Date() + ": makeUnauthorizedException: " + e.getMessage();
        final String st = stackTraceAsString(e);
        if (st.length() > 0) {
            msg += "\n" + st;
        }
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

    private static String stackTraceAsString(Throwable t) {
        final Writer writer = new StringWriter();
        final PrintWriter printer = new PrintWriter(writer);
        t.printStackTrace(printer);
        return writer.toString();
    }
}
