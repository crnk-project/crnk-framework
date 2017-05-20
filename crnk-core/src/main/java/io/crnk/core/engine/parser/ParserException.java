package io.crnk.core.engine.parser;

import io.crnk.core.exception.CrnkMatchingException;

/**
 * Thrown when parser exception occurs.
 */
public class ParserException extends CrnkMatchingException {

	public ParserException(String message) {
		super(message);
	}

	public ParserException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
