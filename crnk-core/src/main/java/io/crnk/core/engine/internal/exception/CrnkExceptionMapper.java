package io.crnk.core.engine.internal.exception;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.CrnkMappableException;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.exception.InternalServerErrorException;
import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Exception mapper for a generic exception which can be thrown in request processing.
 */
public final class CrnkExceptionMapper implements ExceptionMapper<CrnkMappableException> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CrnkExceptionMapper.class);

	@Override
	public ErrorResponse toErrorResponse(CrnkMappableException exception) {
		// log 5xx status as error and anything else as warn
		if (exception.getHttpStatus() >= 500 && exception.getHttpStatus() < 600) {
			LOGGER.error("failed to process request", exception);
		} else {
			LOGGER.warn("failed to process request", exception);
		}

		return ErrorResponse.builder().setStatus(exception.getHttpStatus()).setSingleErrorData(exception.getErrorData()).build();
	}

	@Override
	public CrnkMappableException fromErrorResponse(ErrorResponse errorResponse) {
		String message = getMessage(errorResponse);

		int httpStatus = errorResponse.getHttpStatus();
		if (httpStatus == HttpStatus.FORBIDDEN_403) {
			return new ForbiddenException(message);
		}
		if (httpStatus == HttpStatus.METHOD_NOT_ALLOWED_405) {
			return new MethodNotAllowedException(message);
		}
		if (httpStatus == HttpStatus.UNAUTHORIZED_401) {
			return new UnauthorizedException(message);
		}
		if (httpStatus == HttpStatus.NOT_FOUND_404) {
			return new ResourceNotFoundException(message);
		}
		if (httpStatus == HttpStatus.BAD_REQUEST_400) {
			return new BadRequestException(message);
		}
		if (httpStatus == HttpStatus.INTERNAL_SERVER_ERROR_500) {
			return new InternalServerErrorException(message);
		}
		throw new IllegalStateException(errorResponse.toString());
	}

	private String getMessage(ErrorResponse errorResponse) {
		Iterator<ErrorData> errors = errorResponse.getErrors().iterator();
		String message = null;
		if (errors.hasNext()) {
			ErrorData data = errors.next();
			message = data.getDetail();
			if (message == null) {
				message = data.getTitle();
			}
			if (message == null) {
				message = data.getCode();
			}
		}
		return message;
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		int httpStatus = errorResponse.getHttpStatus();
		return httpStatus == HttpStatus.NOT_FOUND_404 || httpStatus == HttpStatus.METHOD_NOT_ALLOWED_405 ||
				httpStatus == HttpStatus.BAD_REQUEST_400 || httpStatus == HttpStatus.FORBIDDEN_403
				|| httpStatus == HttpStatus.UNAUTHORIZED_401 || httpStatus == HttpStatus.INTERNAL_SERVER_ERROR_500;
	}
}
