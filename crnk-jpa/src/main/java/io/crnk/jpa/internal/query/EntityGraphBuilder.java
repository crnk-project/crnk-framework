package io.crnk.jpa.internal.query;

import io.crnk.meta.model.MetaAttributePath;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Set;

public interface EntityGraphBuilder {

	<T> void build(EntityManager em, Query criteriaQuery, Class<T> entityClass,
				   Set<MetaAttributePath> fetchPaths);

}
