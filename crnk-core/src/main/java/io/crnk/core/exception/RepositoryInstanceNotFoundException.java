package io.crnk.core.exception;

/**
 * Thrown when repository instance for a resource cannot be found
 */
public final class RepositoryInstanceNotFoundException extends CrnkMatchingException {

	public RepositoryInstanceNotFoundException(String missingRepositoryClassName) {
		super("Instance of the repository not found: " + missingRepositoryClassName);
	}
}