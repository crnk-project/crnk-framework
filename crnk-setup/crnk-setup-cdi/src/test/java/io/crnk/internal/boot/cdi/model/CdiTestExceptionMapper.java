package io.crnk.internal.boot.cdi.model;

import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;

import jakarta.enterprise.context.ApplicationScoped;


@ApplicationScoped
public class CdiTestExceptionMapper implements ExceptionMapper<IllegalStateException> {

	@Override
	public ErrorResponse toErrorResponse(IllegalStateException cve) {
		throw new IllegalStateException();
	}

	@Override
	public IllegalStateException fromErrorResponse(ErrorResponse errorResponse) {
		throw new IllegalStateException();
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		return false;
	}
}