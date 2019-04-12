package io.crnk.core.repository.decorate;

import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.ResourceRepository;

import java.io.Serializable;

/**
 * Base class for {@links RepositoryDecorator} implementations doing nothing.
 */
public class RepositoryDecoratorFactoryBase implements RepositoryDecoratorFactory {

	@Override
	public <T, I > ResourceRepositoryDecorator<T, I> decorateRepository(
			ResourceRepository<T, I> repository) {
		// nothing to decorate
		return null;
	}

	@Override
	public <T, I , D, J > RelationshipRepositoryDecorator<T, I, D, J> decorateRepository(
			RelationshipRepository<T, I, D, J> repository) {
		// nothing to decorate
		return null;
	}
}
