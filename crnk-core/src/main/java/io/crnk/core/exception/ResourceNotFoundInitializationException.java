package io.crnk.core.exception;

public class ResourceNotFoundInitializationException extends CrnkInitializationException {

	public ResourceNotFoundInitializationException(String className) {
		super("Resource of class not found: " + className);
	}
}
