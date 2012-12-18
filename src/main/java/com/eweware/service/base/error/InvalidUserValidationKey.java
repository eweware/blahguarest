package main.java.com.eweware.service.base.error;


public class InvalidUserValidationKey extends BaseException {

	public InvalidUserValidationKey() {
	}

	public InvalidUserValidationKey(String message) {
		super(message);
	}

	public InvalidUserValidationKey(Throwable cause) {
		super(cause);
	}

	public InvalidUserValidationKey(String message, Throwable cause) {
		super(message, cause);
	}

    public InvalidUserValidationKey(String msg, Object entity, Integer errorCode) {
        super(msg, entity, errorCode);
    }

    public InvalidUserValidationKey(String message, Object entity) {
		super(message, entity);
	}

    public InvalidUserValidationKey(String msg, Throwable entity, Integer errorCode) {
        super(msg, entity, errorCode);    
    }

    public InvalidUserValidationKey(String msg, Throwable throwable, Object entity, Integer errorCode) {
        super(msg, throwable, entity, errorCode);
    }

    public InvalidUserValidationKey(String msg, Integer errorCode) {
        super(msg, errorCode);    
    }

    public InvalidUserValidationKey(Throwable e, Integer errorCode) {
        super(e, errorCode);
    }
}
