package io.crnk.spring.exception;

import java.util.Optional;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.module.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.BeanCreationException;

/**
 * Reveal real exceptions behind Spring-related bean exceptions. Most importantly, SB4BPrincipalProvider throwing IdentityExceptions.
 */
public class BeanExceptionMapper implements ExceptionMapper<BeanCreationException> {

	private Logger LOGGER = LoggerFactory.getLogger(getClass());

	protected Module.ModuleContext context;

	public BeanExceptionMapper(Module.ModuleContext context) {
		this.context = context;
	}

	@Override
	public ErrorResponse toErrorResponse(BeanCreationException exception) {
		Throwable cause = exception.getCause();
		while (cause instanceof BeanCreationException || cause instanceof BeanInstantiationException) {
			cause = cause.getCause();
		}
		if (cause != null) {
			Optional<ExceptionMapper> mapper = context.getExceptionMapperRegistry().findMapperFor(cause.getClass());
			if (mapper.isPresent()) {
				return mapper.get().toErrorResponse(cause);
			}
		}

		LOGGER.error("failed to setup spring beans", exception);

		// no mapper found, return default error
		int status = HttpStatus.INTERNAL_SERVER_ERROR_500;
		ErrorData errorData = ErrorData.builder().setStatus(Integer.toString(status))
				.setTitle(exception.getMessage()).build();
		return ErrorResponse.builder().setSingleErrorData(errorData).setStatus(status).build();
	}


	@Override
	public BeanCreationException fromErrorResponse(ErrorResponse errorResponse) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		return false;
	}
}
