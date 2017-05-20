package io.crnk.core.engine.information.resource;

import io.crnk.core.engine.internal.information.resource.ReflectionFieldAccessor;

/**
 * Provides access to a field of a resource. See {@link ReflectionFieldAccessor}
 * for a default implementation.
 *
 * @author Remo
 */
public interface ResourceFieldAccessor {

	Object getValue(Object resource);

	void setValue(Object resource, Object fieldValue);

}
