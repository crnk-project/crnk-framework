package io.crnk.core.engine.internal.exception;

import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.utils.Prioritizable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

public final class TimeoutExceptionMapper implements ExceptionMapper<TimeoutException>, Prioritizable {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimeoutExceptionMapper.class);

	@Override
	public ErrorResponse toErrorResponse(TimeoutException exception) {
		LOGGER.error("failed with timeout", exception);

		return ErrorResponse.builder().setStatus(HttpStatus.GATEWAY_TIMEOUT_504).build();
	}

	@Override
	public TimeoutException fromErrorResponse(ErrorResponse errorResponse) {
		return new TimeoutException();
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		int httpStatus = errorResponse.getHttpStatus();
		return httpStatus == HttpStatus.GATEWAY_TIMEOUT_504;
	}

	@Override
	public int getPriority() {
		return Prioritizable.MEDIUM_PRIORITY;
	}
}
