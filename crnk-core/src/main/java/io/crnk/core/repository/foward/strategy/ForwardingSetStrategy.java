package io.crnk.core.repository.foward.strategy;

import java.io.Serializable;

public interface ForwardingSetStrategy<T, I extends Serializable, D, J extends Serializable>
		extends ForwardingStrategy<T, I, D, J> {

	void setRelation(T source, J targetId, String fieldName);

	void setRelations(T source, Iterable<J> targetIds, String fieldName);

	void addRelations(T source, Iterable<J> targetIds, String fieldName);

	void removeRelations(T source, Iterable<J> targetIds, String fieldName);
}
