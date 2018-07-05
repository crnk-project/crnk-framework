package io.crnk.core.repository.decorate;

import java.io.Serializable;

import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.repository.WrappedResourceRepository;

/**
 * Note that ResourceRepositoryDecorator are ignored by the Crnk engine and not treated as repositories themselves.
 */
public abstract class ResourceRepositoryDecoratorBase<T, I extends Serializable> extends WrappedResourceRepository<T, I>
		implements ResourceRepositoryDecorator<T, I>, ResourceRegistryAware {

	protected ResourceRepositoryV2<T, I> decoratedObject;

	@Override
	public void setResourceRegistry(ResourceRegistry resourceRegistry) {
		if (decoratedObject instanceof ResourceRegistryAware) {
			((ResourceRegistryAware) decoratedObject).setResourceRegistry(resourceRegistry);
		}
	}

	@Override
	public void setDecoratedObject(ResourceRepositoryV2<T, I> wrappedRepository) {
		this.decoratedObject = wrappedRepository;
		super.setWrappedRepository(wrappedRepository);
	}
}
