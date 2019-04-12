package io.crnk.core.repository.foward.strategy;

import io.crnk.core.engine.query.QueryContext;

import java.io.Serializable;
import java.util.Collection;

public interface ForwardingSetStrategy<T, I , D, J >
		extends ForwardingStrategy<T, I, D, J> {

	void setRelation(T source, J targetId, String fieldName, QueryContext queryContext);

	void setRelations(T source, Collection<J> targetIds, String fieldName, QueryContext queryContext);

	void addRelations(T source, Collection<J> targetIds, String fieldName, QueryContext queryContext);

	void removeRelations(T source, Collection<J> targetIds, String fieldName, QueryContext queryContext);
}
