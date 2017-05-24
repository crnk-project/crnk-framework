package io.crnk.core.exception;

/**
 * Indicates that no corresponding controller for a request had not been found.
 */
public class MethodNotFoundException extends CrnkMatchingException {// NOSONAR ignore deep class hierarchy

	public MethodNotFoundException(String uri, String method) {
		super(String.format("%s: %s", method, uri));
	}
}
