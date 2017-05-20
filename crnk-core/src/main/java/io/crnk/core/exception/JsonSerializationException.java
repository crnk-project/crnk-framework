package io.crnk.core.exception;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.http.HttpStatus;

/**
 * Thrown when a Jackson serialization related exception occurs.
 */
public class JsonSerializationException extends InternalServerErrorException {  // NOSONAR exception hierarchy deep but ok
	private static final String TITLE = "JSON serialization error";

	public JsonSerializationException(String message) {
		super(HttpStatus.INTERNAL_SERVER_ERROR_500, ErrorData.builder()
				.setTitle(TITLE)
				.setDetail(message)
				.setStatus(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR_500))
				.build());
	}
}
