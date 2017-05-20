package io.crnk.core.engine.internal.utils;

import io.crnk.core.exception.CrnkException;

/**
 * Indicate an exception when accessing resource properties
 */
public class PropertyException extends CrnkException {

	private final Class<?> resourceClass;
	private final String field;

	public PropertyException(Throwable cause, Class<?> resourceClass, String field) {
		super(cause.getMessage(), cause);
		this.resourceClass = resourceClass;
		this.field = field;
	}

	public PropertyException(String message, Class<?> resourceClass, String field) {
		super(message);
		this.resourceClass = resourceClass;
		this.field = field;
	}

	public Class<?> getResourceClass() {
		return resourceClass;
	}

	public String getField() {
		return field;
	}
}
