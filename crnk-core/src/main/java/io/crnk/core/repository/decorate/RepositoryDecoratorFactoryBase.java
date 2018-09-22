package io.crnk.core.repository.decorate;

import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;

import java.io.Serializable;

/**
 * Base class for {@links RepositoryDecorator} implementations doing nothing.
 */
public class RepositoryDecoratorFactoryBase implements RepositoryDecoratorFactory {

	@Override
	public <T, I extends Serializable> ResourceRepositoryDecorator<T, I> decorateRepository(
			ResourceRepositoryV2<T, I> repository) {
		// nothing to decorate
		return null;
	}

	@Override
	public <T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryDecorator<T, I, D, J> decorateRepository(
			RelationshipRepositoryV2<T, I, D, J> repository) {
		// nothing to decorate
		return null;
	}
}
