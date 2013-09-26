package com.eweware.service.base.error;

/**
 * @author rk@post.harvard.edu
 *         Date: 9/21/12 Time: 7:00 PM
 */
public class DuplicateKeyException extends RuntimeException {
    public DuplicateKeyException() {
        super();
    }

    public DuplicateKeyException(String s) {
        super(s);
    }

    public DuplicateKeyException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public DuplicateKeyException(Throwable throwable) {
        super(throwable);
    }
}
