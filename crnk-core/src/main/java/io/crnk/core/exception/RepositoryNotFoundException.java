package io.crnk.core.exception;

import java.util.Set;

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

	public RepositoryNotFoundException(String resourceType, Set<String> availableResourcetypes) {
		super("Repository for a resource not found: " + resourceType + ", available=" + availableResourcetypes);
	}
}
