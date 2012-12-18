package main.java.com.eweware.service.base.error;

/**
 * @author rk@post.harvard.edu
 *         Date: 6/20/12 Time: 6:41 PM
 */
public class InvalidAuthorizedStateException extends BaseException {

    public InvalidAuthorizedStateException() {
        super();
    }

    public InvalidAuthorizedStateException(String msg) {
        super(msg);
    }

    public InvalidAuthorizedStateException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

    public InvalidAuthorizedStateException(String msg, Object entity, Integer errorCode) {
        super(msg, entity, errorCode);
    }

    public InvalidAuthorizedStateException(Throwable throwable) {
        super(throwable);    
    }

    public InvalidAuthorizedStateException(String msg, Object entity) {
        super(msg, entity);
    }

    public InvalidAuthorizedStateException(Throwable e, Integer errorCode) {
        super(e, errorCode);
    }

    public InvalidAuthorizedStateException(String msg, Throwable entity, Integer errorCode) {
        super(msg, entity, errorCode);    
    }

    public InvalidAuthorizedStateException(String msg, Throwable throwable, Object entity, Integer errorCode) {
        super(msg, throwable, entity, errorCode);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public InvalidAuthorizedStateException(String msg, Integer errorCode) {
        super(msg, errorCode);    
    }
}
