package io.crnk.jpa.internal;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.error.JsonApiExceptionMapper;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.module.Module;
import io.crnk.core.module.Module.ModuleContext;
import io.crnk.core.utils.Optional;

import javax.persistence.RollbackException;

/**
 * RollbackException can hide the more interesting causes.
 */
public class PersistenceRollbackExceptionMapper implements ExceptionMapper<RollbackException> {

	private ModuleContext context;

	public PersistenceRollbackExceptionMapper(Module.ModuleContext context) {
		this.context = context;
	}

	@Override
	public ErrorResponse toErrorResponse(RollbackException exception) {
		Throwable cause = exception.getCause();
		if (cause != null) {
			Optional<JsonApiExceptionMapper> mapper = context.getExceptionMapperRegistry().findMapperFor(cause.getClass());
			if (mapper.isPresent()) {
				return mapper.get().toErrorResponse(cause);
			}
		}
		// no mapper found, return default error
		ErrorData errorData = ErrorData.builder().setStatus(Integer.toString(HttpStatus.INTERNAL_SERVER_ERROR_500)).setTitle(exception.getMessage()).build();
		return ErrorResponse.builder().setSingleErrorData(errorData).setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500).build();
	}

	@Override
	public RollbackException fromErrorResponse(ErrorResponse errorResponse) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		return false;
	}

}
