package io.crnk.jpa;

import io.crnk.core.engine.information.resource.ResourceField;

import java.io.Serializable;

public class DefaultJpaRepositoryFactory implements JpaRepositoryFactory {

	@Override
	public <T, I > JpaEntityRepository<T, I> createEntityRepository(JpaModule module,
																						JpaRepositoryConfig<T> config) {

		return new JpaEntityRepository<>(config);
	}

	@Override
	public <T, I , D, J > JpaRelationshipRepository<T, I, D, J>
	createRelationshipRepository(
			JpaModule module, ResourceField field, JpaRepositoryConfig<D> config) {
		return new JpaRelationshipRepository<>(module, field, config);
	}
}
