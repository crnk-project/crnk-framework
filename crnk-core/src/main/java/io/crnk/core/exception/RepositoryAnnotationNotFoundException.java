package io.crnk.core.exception;

public class RepositoryAnnotationNotFoundException extends InternalServerErrorException {// NOSONAR ignore deep class hierarchy

	public RepositoryAnnotationNotFoundException(String message) {
		super(message);
	}
}
