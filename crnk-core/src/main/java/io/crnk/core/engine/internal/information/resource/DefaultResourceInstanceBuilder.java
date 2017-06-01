package io.crnk.core.engine.internal.information.resource;

import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.information.resource.ResourceInstanceBuilder;
import io.crnk.core.engine.internal.utils.ClassUtils;

/**
 * Default implementation for {@link ResourceInstanceBuilder}} that creates a new instance of the given class
 * using its default constructor.
 */
public class DefaultResourceInstanceBuilder<T> implements ResourceInstanceBuilder<T> {

	private Class<T> resourceClass;

	public DefaultResourceInstanceBuilder(Class<T> resourceClass) {
		this.resourceClass = resourceClass;
	}

	@Override
	public T buildResource(Resource body) {
		return ClassUtils.newInstance(resourceClass);
	}
}