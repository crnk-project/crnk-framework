package io.crnk.core.repository.decorate;

import io.crnk.core.engine.internal.utils.Decorator;
import io.crnk.core.repository.ResourceRepositoryV2;

import java.io.Serializable;

public interface ResourceRepositoryDecorator<T, I extends Serializable> extends ResourceRepositoryV2<T, I>, Decorator<ResourceRepositoryV2<T, I>> {

	@Override
	void setDecoratedObject(ResourceRepositoryV2<T, I> decoratedObject);
}
