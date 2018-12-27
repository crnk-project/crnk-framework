package io.crnk.jpa.internal;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.module.Module.ModuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

abstract class AbstractJpaExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {

	private Logger LOGGER = LoggerFactory.getLogger(getClass());

	protected ModuleContext context;

	public AbstractJpaExceptionMapper(ModuleContext context) {
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

		LOGGER.error("failed to process request due to jpa exception", exception);

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
