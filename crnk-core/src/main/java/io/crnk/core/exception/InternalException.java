package io.crnk.core.exception;

/**
 * Defines internal Crnk exception
 */
public class InternalException extends CrnkInitializationException {

	public InternalException(String message) {
		super(message);
	}

	public InternalException(String message, Exception e) {
		super(message, e);
	}
}
