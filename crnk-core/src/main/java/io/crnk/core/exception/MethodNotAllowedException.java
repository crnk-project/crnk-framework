package io.crnk.core.exception;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.http.HttpStatus;

public class MethodNotAllowedException extends CrnkMappableException {// NOSONAR ignore deep class hierarchy

	public MethodNotAllowedException(String method) {
		super(HttpStatus.METHOD_NOT_ALLOWED_405, createErrorData(method));
	}

	public static ErrorData createErrorData(String method) {
		return ErrorData.builder()
				.setStatus(String.valueOf(HttpStatus.METHOD_NOT_ALLOWED_405))
				.setCode("METHOD_NOT_ALLOWED")
				.setDetail(method.startsWith("method") ? method : "method not allowed: " + method)
				.build();
	}
}

