package io.crnk.data.jpa.query;

import io.crnk.meta.provider.MetaPartition;

import jakarta.persistence.EntityManager;

public interface JpaQueryFactoryContext {

	EntityManager getEntityManager();

	MetaPartition getMetaPartition();

}
