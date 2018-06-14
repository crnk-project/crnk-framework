package io.crnk.core.exception;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;

public class RequestBodyException extends CrnkMappableException {// NOSONAR ignore deep class hierarchy

	private static final String TITLE = "Request body error";

	public RequestBodyException(@SuppressWarnings("SameParameterValue") HttpMethod method, String resourceName, String details) {
		super(HttpStatus.BAD_REQUEST_400, ErrorData.builder()
				.setStatus(String.valueOf(HttpStatus.BAD_REQUEST_400))
				.setTitle(TITLE)
				.setDetail(String.format("Request body doesn't meet the requirements (%s), %s method, resource name %s",
						details, method.name(), resourceName))
				.build());
	}

	public RequestBodyException(String details, Throwable cause) {
		super(HttpStatus.BAD_REQUEST_400, ErrorData.builder()
				.setStatus(String.valueOf(HttpStatus.BAD_REQUEST_400))
				.setTitle(TITLE)
				.setDetail(details)
				.build(), cause);
	}
}
