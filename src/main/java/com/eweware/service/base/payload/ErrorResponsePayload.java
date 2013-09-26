package com.eweware.service.base.payload;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * <p>Used to communicate an error to a client.</p>
 * @author rk@post.harvard.edu
 */
public final class ErrorResponsePayload extends LinkedHashMap<String, Object> implements Serializable {

	private static final String ERROR_CODE_FIELDNAME = "errorCode";
    private static final String MESSAGE_FIELDNAME = "message";
    private static final String ENTITY_FIELDNAME = "entity";

	public ErrorResponsePayload() {
		super();
	}

	public ErrorResponsePayload(Integer errorCode, String message) {
		this(errorCode, message, null);
	}
	
	public ErrorResponsePayload(Integer errorCode, String message, Object entity) {
		this();
		this.setErrorCode(errorCode);
        this.setEntity(entity);
        this.setMessage(message);
	}
	public Integer getErrorCode() {
		return (Integer) get(ERROR_CODE_FIELDNAME);
	}
	public void setErrorCode(Integer errorCode) {
        put(ERROR_CODE_FIELDNAME, errorCode);
    }

    public Object getEntity() {
        return get(ENTITY_FIELDNAME);
    }

    public void setEntity(Object entity) {
        put(ENTITY_FIELDNAME, entity);
    }

	public String getMessage() {
		return (String) get(MESSAGE_FIELDNAME);
	}

	public void setMessage(String message) {
        put(MESSAGE_FIELDNAME, message);
    }
}
