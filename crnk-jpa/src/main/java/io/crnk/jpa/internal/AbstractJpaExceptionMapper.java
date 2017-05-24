package io.crnk.jpa.internal;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.module.Module.ModuleContext;
import io.crnk.core.utils.Optional;

abstract class AbstractWrappedExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {

	protected ModuleContext context;

	public AbstractWrappedExceptionMapper(ModuleContext context) {
		this.context = context;
	}

	@Override
	public ErrorResponse toErrorResponse(E exception) {
		Throwable cause = exception.getCause();
		if (cause != null) {
			Optional<JsonApiExceptionMapper> mapper = context.getExceptionMapperRegistry().findMapperFor(cause.getClass());
			if (mapper.isPresent()) {
				return mapper.get().toErrorResponse(cause);
			}
		}
		// no mapper found, return default error
		int status = getStatus();
		ErrorData errorData = ErrorData.builder().setStatus(Integer.toString(status))
				.setTitle(exception.getMessage()).build();
		return ErrorResponse.builder().setSingleErrorData(errorData).setStatus(status).build();
	}

	protected int getStatus() {
		return HttpStatus.INTERNAL_SERVER_ERROR_500;
	}

	@Override
	public E fromErrorResponse(ErrorResponse errorResponse) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		return false;
	}

}
