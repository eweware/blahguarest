package com.eweware.service.base.error;


public class StateConflictException extends BaseException {
    
	public StateConflictException() {
	}

	public StateConflictException(String msg) {
		super(msg);
	}

	public StateConflictException(Throwable cause) {
		super(cause);
	}

	public StateConflictException(String msg, Throwable cause) {
		super(msg, cause);
	}

    public StateConflictException(String msg, Object entity, Integer errorCode) {
        super(msg, entity, errorCode);    
    }

    public StateConflictException(String msg, Object entity) {
		super(msg, entity);
	}

    public StateConflictException(Throwable e, Integer errorCode) {
        super(e, errorCode);    
    }

    public StateConflictException(String msg, Throwable entity, Integer errorCode) {
        super(msg, entity, errorCode);    
    }

    public StateConflictException(String msg, Throwable throwable, Object entity, Integer errorCode) {
        super(msg, throwable, entity, errorCode);    
    }

    public StateConflictException(String msg, Integer errorCode) {
        super(msg, errorCode);    
    }
}
