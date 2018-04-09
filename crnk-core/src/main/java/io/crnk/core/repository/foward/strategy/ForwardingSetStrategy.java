package io.crnk.core.repository.foward.strategy;

import io.crnk.core.engine.query.QueryContext;

import java.io.Serializable;

public interface ForwardingSetStrategy<T, I extends Serializable, D, J extends Serializable>
		extends ForwardingStrategy<T, I, D, J> {

	void setRelation(T source, J targetId, String fieldName, QueryContext queryContext);

	void setRelations(T source, Iterable<J> targetIds, String fieldName, QueryContext queryContext);

	void addRelations(T source, Iterable<J> targetIds, String fieldName, QueryContext queryContext);

	void removeRelations(T source, Iterable<J> targetIds, String fieldName, QueryContext queryContext);
}
