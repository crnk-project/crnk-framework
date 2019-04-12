package io.crnk.core.repository.decorate;

import io.crnk.core.engine.internal.utils.Decorator;
import io.crnk.core.repository.ResourceRepository;

import java.io.Serializable;

public interface ResourceRepositoryDecorator<T, I > extends ResourceRepository<T, I>, Decorator<ResourceRepository<T, I>> {

	@Override
	void setDecoratedObject(ResourceRepository<T, I> decoratedObject);
}
