package io.crnk.core.exception;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.http.HttpStatus;

/**
 * Thrown, when is unable to read request parameters
 */
public class ParametersDeserializationException extends BadRequestException {  // NOSONAR exception hierarchy deep but ok
	private static final String TITLE = "Request parameters error";

	public ParametersDeserializationException(String message) {
		this(message, null);
	}

	public ParametersDeserializationException(String message, Throwable cause) {
		super(HttpStatus.BAD_REQUEST_400, ErrorData.builder()
						.setTitle(TITLE)
						.setDetail(message)
						.setStatus(String.valueOf(HttpStatus.BAD_REQUEST_400))
						.build(),
				cause);
	}
}
