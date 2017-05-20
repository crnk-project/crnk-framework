package io.crnk.core.exception;

/**
 * A resource does not contain field annotated with JsonApiId annotation.
 */
public final class ResourceIdNotFoundException extends CrnkInitializationException {

	public ResourceIdNotFoundException(String className) {
		super("Id field not found in class: " + className);
	}
}
