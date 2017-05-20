package io.crnk.core.repository.decorate;

import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.ResourceRepositoryV2;

import java.io.Serializable;

/**
 * Allows to intercept calls to repositories by modules and make changes.
 */
public interface RepositoryDecoratorFactory {

	/**
	 * Allows to wrap a repository with {@link ResourceRepositoryDecorator}.
	 *
	 * @param repository to wrap
	 * @return decorated repository
	 */
	<T, I extends Serializable> ResourceRepositoryDecorator<T, I> decorateRepository(
			ResourceRepositoryV2<T, I> repository);

	/**
	 * Allows to wrap a repository with {@link RelationshipRepositoryDecorator}.
	 *
	 * @param repository to wrap
	 * @return decorated repository
	 */
	<T, I extends Serializable, D, J extends Serializable> RelationshipRepositoryDecorator<T, I, D, J> decorateRepository(
			RelationshipRepositoryV2<T, I, D, J> repository);

}
