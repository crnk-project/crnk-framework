package io.crnk.core.exception;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.http.HttpStatus;

public class BadRequestException extends CrnkMappableException {

	private static final String TITLE = "BAD_REQUEST";

	public BadRequestException(String message) {
		super(HttpStatus.BAD_REQUEST_400, ErrorData.builder().setTitle(TITLE).setDetail(message)
				.setStatus(String.valueOf(HttpStatus.BAD_REQUEST_400)).build());
	}

	public BadRequestException(int httpStatus, ErrorData errorData) {
		super(httpStatus, errorData);
	}

	public BadRequestException(int httpStatus, ErrorData errorData, Throwable cause) {
		super(httpStatus, errorData, cause);
	}
}
