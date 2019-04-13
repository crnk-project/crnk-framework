package io.crnk.data.jpa.query;

import io.crnk.meta.provider.MetaPartition;

import javax.persistence.EntityManager;

public interface JpaQueryFactoryContext {

	EntityManager getEntityManager();

	MetaPartition getMetaPartition();

}
