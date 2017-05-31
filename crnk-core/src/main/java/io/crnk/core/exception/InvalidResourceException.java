package io.crnk.core.exception;

public class InvalidResourceException extends CrnkInitializationException {

	public InvalidResourceException(String message) {
		this(message, null);
	}

	public InvalidResourceException(String message, Exception e) {
		super(message, e);
	}
}
