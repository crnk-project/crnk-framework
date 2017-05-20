package io.crnk.rs.internal;

import io.crnk.core.engine.error.ExceptionMapperHelper;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.utils.Optional;
import io.crnk.rs.type.JsonApiMediaType;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Maps exceptions for which a Crnk exception mapper has been registered in
 * the Crnk {@link ExceptionMapperRegistry} to proper JSON API responses.
 */
public class JsonApiExceptionMapper implements ExceptionMapper<Throwable> {

	private ExceptionMapperRegistry exceptionMapperRegistry;

	JsonApiExceptionMapper(ExceptionMapperRegistry exceptionMapperRegistry) {
		this.exceptionMapperRegistry = exceptionMapperRegistry;
	}

	/**
	 * Maps any given exception for which an exception mapper has been registered in the
	 * Crnk {@link ExceptionMapperRegistry} to a JSON API response.
	 *
	 * @param exception The exception to be mapped to JSON
	 * @return A JAX-RS response containing a JSON message describing the exception
	 */
	@Override
	public Response toResponse(Throwable exception) {
		Optional<io.crnk.core.engine.error.JsonApiExceptionMapper> exceptionMapper = exceptionMapperRegistry.findMapperFor(exception.getClass());
		io.crnk.core.engine.dispatcher.Response errorResponse;
		if (exceptionMapper.isPresent()) {
			errorResponse = exceptionMapper.get().toErrorResponse(exception).toResponse();
		} else {
			int statusCode = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
			errorResponse = ExceptionMapperHelper.toErrorResponse(exception, statusCode, exception.getClass().getName()).toResponse();
		}

		return Response.status(errorResponse.getHttpStatus()).entity(errorResponse.getDocument())
				.header("Content-Type", JsonApiMediaType.APPLICATION_JSON_API).build();
	}

}
