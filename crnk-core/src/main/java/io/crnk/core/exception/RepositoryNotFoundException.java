package io.crnk.core.exception;

/**
 * Thrown when repository definition for a resource cannot be found in specified package.
 */
public final class RepositoryNotFoundException extends InternalServerErrorException {// NOSONAR ignore deep class hierarchy

	public RepositoryNotFoundException(Class clazz) {
		super("Repository for a resource not found: " + clazz.getCanonicalName());
	}

	public RepositoryNotFoundException(String resourceType) {
		super("Repository for a resource not found: " + resourceType);
	}
}
