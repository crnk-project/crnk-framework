package io.crnk.jpa;

import java.io.Serializable;

public class DefaultJpaRepositoryFactory implements JpaRepositoryFactory {

	@Override
	public <T, I extends Serializable> JpaEntityRepository<T, I> createEntityRepository(JpaModule module,
																						JpaRepositoryConfig<T> config) {
		return new JpaEntityRepository<>(module, config);
	}

	@Override
	public <T, I extends Serializable, D, J extends Serializable> JpaRelationshipRepository<T, I, D, J> createRelationshipRepository(
			JpaModule module, Class<T> sourceResourceClass, JpaRepositoryConfig<D> config) {
		return new JpaRelationshipRepository<>(module, sourceResourceClass, config);
	}
}
