package io.crnk.core.repository.foward.strategy;

import java.io.Serializable;

public class SetOppositeStrategy<T, I extends Serializable, D, J extends Serializable>
		extends ForwardingStrategyBase implements ForwardingSetStrategy<T, I, D, J> {


	@Override
	public void setRelation(T source, J targetId, String fieldName) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void setRelations(T source, Iterable<J> targetIds, String fieldName) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void addRelations(T source, Iterable<J> targetIds, String fieldName) {
		throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public void removeRelations(T source, Iterable<J> targetIds, String fieldName) {
		throw new UnsupportedOperationException("not yet implemented");
	}
}
