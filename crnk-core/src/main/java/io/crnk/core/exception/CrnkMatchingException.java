package io.crnk.core.exception;

/**
 * Exceptions which derive from this class should be considered as part of the method matching mechanism, that is they
 * shouldn't be considered as an error that must be sent in response.
 * <p>
 * Exceptions of this kind should be logged and the request processing should be continued.
 */
public abstract class CrnkMatchingException extends CrnkException {

	protected CrnkMatchingException(String message) {
		super(message);
	}

	protected CrnkMatchingException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
