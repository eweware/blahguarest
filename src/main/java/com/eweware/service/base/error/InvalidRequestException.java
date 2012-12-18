package main.java.com.eweware.service.base.error;

/**
 * @author rk@post.harvard.edu
 */
public class InvalidRequestException extends BaseException {

	public InvalidRequestException() {
	}

	public InvalidRequestException(String msg) {
		super(msg);
	}

	public InvalidRequestException(Throwable msg) {
		super(msg);
	}

	public InvalidRequestException(String msg, Throwable arg1) {
		super(msg, arg1);
	}

	public InvalidRequestException(String msg, Object entity) {
		super(msg, entity);
	}

    public InvalidRequestException(Throwable e, Integer errorCode) {
        super(e, errorCode);
    }

    public InvalidRequestException(String msg, Object entity, Integer errorCode) {
        super(msg, entity, errorCode);    
    }

    public InvalidRequestException(String msg, Throwable throwable, Integer errorCode) {
        super(msg, throwable, errorCode);
    }

    public InvalidRequestException(String msg, Throwable throwable, Object entity, Integer errorCode) {
        super(msg, throwable, entity, errorCode);
    }

    public InvalidRequestException(String msg, Integer errorCode) {
        super(msg, errorCode);    
    }
}
