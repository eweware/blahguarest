package main.java.com.eweware.service.base.error;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/2/12 Time: 1:37 PM
 */
public abstract class BaseException extends Exception {

    private static final String EMPTY_STRING = "";

    private Object entity = EMPTY_STRING;
    private Integer errorCode = ErrorCodes.SERVER_SEVERE_ERROR;

    public BaseException() {
        super();
    }

    public BaseException(String s) {
        super(s);
    }

    public BaseException(Throwable throwable) {
        super(throwable);
    }

    public BaseException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public BaseException(String msg, Object entity, Integer errorCode) {
        this(msg);
        this.errorCode = errorCode;
        this.entity = entity;
    }

    public BaseException(String msg, Throwable throwable, Integer errorCode) {
        super(msg, throwable);
        this.errorCode = errorCode;
    }

    public BaseException(String msg, Throwable throwable, Object entity, Integer errorCode) {
        super(msg, throwable);
        this.errorCode = errorCode;
        this.entity = entity;
    }

    public BaseException(String msg, Integer errorCode) {
        super(msg);
        this.errorCode = errorCode;
    }

    public BaseException(String msg, Object entity) {
        super(msg);
        this.entity = entity;
    }

    public BaseException(Throwable e, Integer errorCode) {
        super(e);
        this.errorCode = errorCode;
    }

    public Object getEntity() {
        return entity;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
