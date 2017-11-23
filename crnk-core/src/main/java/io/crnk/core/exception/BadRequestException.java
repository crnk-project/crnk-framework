package io.crnk.core.exception;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.http.HttpStatus;

public class BadRequestException extends CrnkMappableException { // NOSONAR ignore deep class hierarchy

	private static final String TITLE = "BAD_REQUEST";

	public BadRequestException(String message) {
		this(HttpStatus.BAD_REQUEST_400, ErrorData.builder().setTitle(TITLE).setDetail(message)
				.setStatus(String.valueOf(HttpStatus.BAD_REQUEST_400)).build());
	}

	public BadRequestException(String message, Throwable e) {
		this(HttpStatus.BAD_REQUEST_400, ErrorData.builder().setTitle(TITLE).setDetail(message)
				.setStatus(String.valueOf(HttpStatus.BAD_REQUEST_400)).build(), e);
	}

	public BadRequestException(int httpStatus, ErrorData errorData) {
		this(httpStatus, errorData, null);
	}

	public BadRequestException(int httpStatus, ErrorData errorData, Throwable cause) {
		super(httpStatus, errorData, cause);
	}
}
