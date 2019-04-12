package io.crnk.core.repository.decorate;

import io.crnk.core.engine.internal.utils.Decorator;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.Repository;
import io.crnk.core.repository.ResourceRepository;

import java.io.Serializable;

/**
 * Allows to intercept calls to repositories by modules and make changes.
 */
public interface RepositoryDecoratorFactory {

	/**
	 * Generic method allowing to decorate any kind of repository. By default dispatches to
	 * RelationshipRepository and RelationshipRepository decorators.
	 *
	 * @param repository
	 * @return
	 */
	default Object decorateRepository(Object repository) {
		Repository decorator = null;
		if (repository instanceof RelationshipRepository) {
			decorator = decorateRepository((RelationshipRepository) repository);
		} else if (repository instanceof ResourceRepository) {
			decorator = decorateRepository((ResourceRepository) repository);
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
	<T, I > ResourceRepositoryDecorator<T, I> decorateRepository(
			ResourceRepository<T, I> repository);

	/**
	 * Allows to wrap a repository with {@link RelationshipRepositoryDecorator}.
	 *
	 * @param repository to wrap
	 * @return decorated repository
	 */
	<T, I , D, J > RelationshipRepositoryDecorator<T, I, D, J> decorateRepository(
			RelationshipRepository<T, I, D, J> repository);

}
