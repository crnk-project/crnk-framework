package io.crnk.core.engine.error.handlers;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ErrorResponseBuilder;
import io.crnk.core.engine.error.ExceptionMapper;

public class SomeExceptionMapper implements ExceptionMapper<SomeExceptionMapper.SomeException> {

	@Override
	public ErrorResponse toErrorResponse(SomeException Throwable) {
		return new ErrorResponseBuilder()
				.setStatus(500)
				.setSingleErrorData(ErrorData.builder()
						.setTitle("hello")
						.build())
				.build();
	}

	@Override
	public SomeException fromErrorResponse(ErrorResponse errorResponse) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		return false;
	}

	public static class SomeException extends RuntimeException {

	}
}

