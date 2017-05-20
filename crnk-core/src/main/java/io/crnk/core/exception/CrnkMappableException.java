package io.crnk.core.exception;

import io.crnk.core.engine.document.ErrorData;

/**
 * Represents an exception which must be returned to the end user.
 * Consists of error data and related HTTP status, which should be returned in the response.
 */
public abstract class CrnkMappableException extends CrnkException {
	private final ErrorData errorData;
	private final int httpStatus;

	protected CrnkMappableException(int httpStatus, ErrorData errorData) {
		this(httpStatus, errorData, null);
	}

	protected CrnkMappableException(int httpStatus, ErrorData errorData, Throwable cause) {
		super(errorData.getDetail(), cause);
		this.httpStatus = httpStatus;
		this.errorData = errorData;
	}

	public ErrorData getErrorData() {
		return errorData;
	}

	public int getHttpStatus() {
		return httpStatus;
	}
}
