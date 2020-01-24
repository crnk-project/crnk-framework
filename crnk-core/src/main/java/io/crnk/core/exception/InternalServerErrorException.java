package io.crnk.core.exception;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.http.HttpStatus;

public class InternalServerErrorException extends CrnkMappableException {  // NOSONAR exception hierarchy deep but ok

	private static final String TITLE = "INTERNAL_SERVER_ERROR";

	public InternalServerErrorException(String message) {
		super(HttpStatus.INTERNAL_SERVER_ERROR_500,
				ErrorData.builder()
						.setTitle(TITLE)
						.setDetail(message)
						.setStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR_500)).build());
	}

	public InternalServerErrorException(int httpStatus, ErrorData errorData) {
		super(httpStatus, errorData);
	}

	public InternalServerErrorException(int httpStatus, ErrorData errorData, Throwable cause) {
		super(httpStatus, errorData, cause);
	}
}
