package io.crnk.core.engine.error.handlers;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ErrorResponseBuilder;
import io.crnk.core.engine.error.JsonApiExceptionMapper;

public class SomeExceptionMapper implements JsonApiExceptionMapper<SomeExceptionMapper.SomeException> {

	@Override
	public ErrorResponse toErrorResponse(SomeException Throwable) {
		return new ErrorResponseBuilder()
				.setStatus(500)
				.setSingleErrorData(ErrorData.builder()
						.setTitle("hello")
						.build())
				.build();
	}

	public static class SomeException extends RuntimeException {
	}
}

