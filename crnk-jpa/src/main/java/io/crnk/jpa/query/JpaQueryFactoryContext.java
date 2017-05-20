package io.crnk.jpa.query;

import io.crnk.meta.MetaLookup;

import javax.persistence.EntityManager;

public interface JpaQueryFactoryContext {

	EntityManager getEntityManager();

	MetaLookup getMetaLookup();

}
