package io.crnk.core.exception;

/**
 * A field within a resource was not found
 */
public final class ResourceFieldNotFoundException extends InternalServerErrorException {// NOSONAR ignore deep class hierarchy

	public ResourceFieldNotFoundException(String message) {
		super(message);
	}
}
