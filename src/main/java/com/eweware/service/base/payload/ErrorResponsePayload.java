package main.java.com.eweware.service.base.payload;

/**
 * @author rk@post.harvard.edu
 * 
 * Used to communicate an error to a client.
 */
public final class ErrorResponsePayload {

	private Integer errorCode;
    private Object entity;
    private String message;

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
		return errorCode;
	}
	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
