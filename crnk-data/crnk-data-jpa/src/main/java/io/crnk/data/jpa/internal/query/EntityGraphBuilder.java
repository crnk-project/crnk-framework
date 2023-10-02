package io.crnk.data.jpa.internal.query;

import io.crnk.meta.model.MetaAttributePath;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Set;

public interface EntityGraphBuilder {

	<T> void build(EntityManager em, Query criteriaQuery, Class<T> entityClass,
				   Set<MetaAttributePath> fetchPaths);

}
