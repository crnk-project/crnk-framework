package io.crnk.core.repository;

import java.io.Serializable;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;

public abstract class ReadOnlyRelationshipRepositoryBase<S, I extends Serializable, T, J extends Serializable>
		implements RelationshipRepositoryV2<S, I, T, J> {


	@Override
	public T findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResourceList<T> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRelation(S source, J targetId, String fieldName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRelations(S source, Iterable<J> targetIds, String fieldName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addRelations(S source, Iterable<J> targetIds, String fieldName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeRelations(S source, Iterable<J> targetIds, String fieldName) {
		throw new UnsupportedOperationException();
	}
}
