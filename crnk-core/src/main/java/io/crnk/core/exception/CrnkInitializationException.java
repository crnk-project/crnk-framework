package io.crnk.core.exception;

/**
 * General type for exceptions, which can be thrown during Crnk startup (building resource registry etc)
 */
public class CrnkInitializationException extends RuntimeException {

	protected CrnkInitializationException(String message) {
		this(message, null);
	}

	protected CrnkInitializationException(String message, Exception e) {
		super(message, e);
	}
}
