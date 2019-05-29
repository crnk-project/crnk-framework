package io.crnk.core.engine.error.handlers;

import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;

public class NoAnnotationExceptionMapper implements ExceptionMapper<NoAnnotationExceptionMapper.ShouldNotAppearException> {
	@Override
	public ErrorResponse toErrorResponse(ShouldNotAppearException exception) {
		return ErrorResponse.builder().setStatus(500).build();
	}

	@Override
	public ShouldNotAppearException fromErrorResponse(ErrorResponse errorResponse) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		return false;
	}

	public static class ShouldNotAppearException extends RuntimeException {
	}
}
