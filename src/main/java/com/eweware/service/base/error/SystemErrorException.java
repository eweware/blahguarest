package com.eweware.service.base.error;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/29/12 Time: 3:08 PM
 */
public class SystemErrorException extends BaseException {

    public SystemErrorException() {
    }

    public SystemErrorException(String message) {
        super(message);
    }

    public SystemErrorException(Throwable cause) {
        super(cause);
    }

    public SystemErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemErrorException(String msg, Object entity, Integer errorCode) {
        super(msg, entity, errorCode);
    }

    public SystemErrorException(String message, Object entity) {
        super(message, entity);
    }

    public SystemErrorException(String msg, Throwable cause, Integer errorCode) {
        super(msg, cause, errorCode);
    }

    public SystemErrorException(String msg, Throwable throwable, Object entity, Integer errorCode) {
        super(msg, throwable, entity, errorCode);
    }

    public SystemErrorException(String msg, Integer errorCode) {
        super(msg, errorCode);
    }

    public SystemErrorException(Throwable e, Integer errorCode) {
        super(e, errorCode);
    }
}
