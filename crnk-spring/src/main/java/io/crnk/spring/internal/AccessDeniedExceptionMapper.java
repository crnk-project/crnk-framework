package io.crnk.spring.internal;

import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.error.ExceptionMapperHelper;
import io.crnk.core.engine.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;

/**
 * Mapper class for Spring AccessDeniedExceptions.
 */
public class AccessDeniedExceptionMapper implements ExceptionMapper<AccessDeniedException> {

	private static final String META_TYPE_VALUE = "AccessDeniedException";
	private static final int ACCESS_DENIED = HttpStatus.FORBIDDEN_403;

	@Override
	public ErrorResponse toErrorResponse(AccessDeniedException exception) {
		return ExceptionMapperHelper.toErrorResponse(exception, ACCESS_DENIED, META_TYPE_VALUE);
	}

	@Override
	public AccessDeniedException fromErrorResponse(ErrorResponse errorResponse) {
		return new AccessDeniedException(ExceptionMapperHelper.createErrorMessage(errorResponse));
	}

	@Override
	public boolean accepts(ErrorResponse errorResponse) {
		return ExceptionMapperHelper.accepts(errorResponse, ACCESS_DENIED, META_TYPE_VALUE);
	}
}
