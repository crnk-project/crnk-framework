package io.crnk.core.engine.parser;

import io.crnk.core.exception.CrnkException;

/**
 * Thrown when parser exception occurs.
 */
public class ParserException extends CrnkException { // NOSONAR ignore exception class hierarchy

	public ParserException(String message) {
		super(message);
	}

	public ParserException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
