package com.eweware.service.base.error;

/**
 * @author rk@post.harvard.edu
 *         Date: 8/28/12 Time: 2:34 PM
 */
public class InvalidUserValidationMethodParameters extends BaseException {
    
    public InvalidUserValidationMethodParameters() {
        super();   
    }

    public InvalidUserValidationMethodParameters(String s) {
        super(s);   
    }

    public InvalidUserValidationMethodParameters(String s, Throwable throwable) {
        super(s, throwable);   
    }

    public InvalidUserValidationMethodParameters(String msg, Object entity, Integer errorCode) {
        super(msg, entity, errorCode);   
    }

    public InvalidUserValidationMethodParameters(String msg, Throwable throwable, Integer errorCode) {
        super(msg, throwable, errorCode);   
    }

    public InvalidUserValidationMethodParameters(String msg, Throwable throwable, Object entity, Integer errorCode) {
        super(msg, throwable, entity, errorCode);   
    }

    public InvalidUserValidationMethodParameters(String msg, Integer errorCode) {
        super(msg, errorCode);   
    }

    public InvalidUserValidationMethodParameters(String msg, Object entity) {
        super(msg, entity);   
    }

    public InvalidUserValidationMethodParameters(Throwable e, Integer errorCode) {
        super(e, errorCode);   
    }

    public InvalidUserValidationMethodParameters(Throwable throwable) {
        super(throwable);   
    }
}
