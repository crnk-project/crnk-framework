package io.crnk.core.exception;

/**
 * Thrown when repository instance for a resource cannot be found
 */
public final class RepositoryInstanceNotFoundException extends InternalServerErrorException {// NOSONAR ignore deep class hierarchy

	public RepositoryInstanceNotFoundException(String missingRepositoryClassName) {
		super("Instance of the repository not found: " + missingRepositoryClassName);
	}
}