package io.crnk.core.engine.error;

/**
 * ExceptionMapper supporting both Crnk servers and clients.
 */
public interface ExceptionMapper<E extends Throwable> {

	/**
	 * Converts the given exception to an JSON API response. Used on the server-side.
	 */
	ErrorResponse toErrorResponse(E exception);

	/**
	 * Convert the given error response to an exception. Used on the client-side.
	 *
	 * @param errorResponse error response
	 * @return exception
	 */
	E fromErrorResponse(ErrorResponse errorResponse);

	/**
	 * Decides whether the given errorResponse can be handled by this mapper.
	 * If true is returned, {@link #fromErrorResponse(ErrorResponse)} will be called.
	 * <p>
	 * If multiple mappers accept a given error response, the most specific exception is chosen, meaning
	 * the one with the most superTypes.
	 *
	 * @return true if it can be handled.
	 */
	boolean accepts(ErrorResponse errorResponse);
}
