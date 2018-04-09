package io.crnk.core.repository.decorate;

import io.crnk.core.engine.internal.utils.Decorator;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.Repository;
import io.crnk.core.repository.ResourceRepositoryV2;

import java.io.Serializable;

/**
 * Allows to intercept calls to repositories by modules and make changes.
 */
public interface RepositoryDecoratorFactory {

	/**
	 * Generic method allowing to decorate any kind of repository. By default dispatches to
	 * RelationshipRepositoryV2 and RelationshipRepositoryV2 decorators.
	 *
	 * @param repository
	 * @return
	 */
	default Object decorateRepository(Object repository) {
		Repository decorator = null;
		if (repository instanceof RelationshipRepositoryV2) {
			decorator = decorateRepository((RelationshipRepositoryV2) repository);
		} else if (repository instanceof ResourceRepositoryV2) {
			decorator = decorateRepository((ResourceRepositoryV2) repository);
		}
		if (decorator instanceof Decorator) {
			((Decorator) decorator).setDecoratedObject(repository);
		}
		return decorator != null ? decorator : repository;
	}

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
