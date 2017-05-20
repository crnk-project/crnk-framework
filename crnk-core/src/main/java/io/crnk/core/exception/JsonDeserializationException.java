package io.crnk.core.exception;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.http.HttpStatus;

/**
 * Thrown, when is unable to read request body
 */
public class JsonDeserializationException extends BadRequestException { // NOSONAR exception hierarchy deep but ok
	private static final String TITLE = "invalid request body";

	public JsonDeserializationException(String message) {
		super(HttpStatus.BAD_REQUEST_400, ErrorData.builder()
				.setTitle(TITLE)
				.setDetail(message)
				.setStatus(String.valueOf(HttpStatus.BAD_REQUEST_400))
				.build());
	}
}
