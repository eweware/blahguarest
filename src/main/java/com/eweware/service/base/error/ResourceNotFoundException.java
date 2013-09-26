package com.eweware.service.base.error;

public class ResourceNotFoundException extends BaseException {

	public ResourceNotFoundException() {
	}

	public ResourceNotFoundException(String msg) {
		super(msg);
	}

	public ResourceNotFoundException(Throwable msg) {
		super(msg);
	}

	public ResourceNotFoundException(String msg, Throwable throwable) {
		super(msg, throwable);
	}

    public ResourceNotFoundException(String msg, Object entity, Integer errorCode) {
        super(msg, entity, errorCode);    
    }

    public ResourceNotFoundException(String msg, Object entity) {
		super(msg, entity);
	}

    public ResourceNotFoundException(Throwable e, Integer errorCode) {
        super(e, errorCode);    
    }

    public ResourceNotFoundException(String msg, Throwable throwable, Integer errorCode) {
        super(msg, throwable, errorCode);
    }

    public ResourceNotFoundException(String msg, Throwable throwable, Object entity, Integer errorCode) {
        super(msg, throwable, entity, errorCode);    
    }

    public ResourceNotFoundException(String msg, Integer errorCode) {
        super(msg, errorCode);
    }

    public ResourceNotFoundException(String msg, String entity, Integer errorCode) {
        super(msg, entity, errorCode);
    }
}
