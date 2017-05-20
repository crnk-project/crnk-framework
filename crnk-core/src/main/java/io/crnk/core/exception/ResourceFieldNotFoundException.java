package io.crnk.core.exception;

/**
 * A field within a resource was not found
 */
public final class ResourceFieldNotFoundException extends CrnkMatchingException {

	public ResourceFieldNotFoundException(String message) {
		super(message);
	}
}
