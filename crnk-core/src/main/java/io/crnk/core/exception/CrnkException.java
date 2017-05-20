package io.crnk.core.exception;

/**
 * General type for exceptions, which can be thrown during Crnk request processing.
 */
public abstract class CrnkException extends RuntimeException {

	public CrnkException(String message) {
		super(message);
	}

	public CrnkException(String message, Throwable cause) {
		super(message, cause);
	}
}