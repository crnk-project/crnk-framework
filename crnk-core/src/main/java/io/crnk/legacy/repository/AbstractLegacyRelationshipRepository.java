package io.crnk.legacy.repository;

import io.crnk.legacy.queryParams.QueryParams;

import java.io.Serializable;

public abstract class AbstractLegacyRelationshipRepository<S, S_ID extends Serializable, T, T_ID extends Serializable>
		implements LegacyRelationshipRepository<S, S_ID, T, T_ID> {


	@Override
	public void setRelation(S source, T_ID targetId, String fieldName) {
		throw new UnsupportedOperationException("setRelation not implemented");
	}

	@Override
	public void setRelations(S source, Iterable<T_ID> targetIds, String fieldName) {
		throw new UnsupportedOperationException("setRelations not implemented");

	}

	@Override
	public void addRelations(S source, Iterable<T_ID> targetIds, String fieldName) {
		throw new UnsupportedOperationException("addRelations not implemented");

	}

	@Override
	public void removeRelations(S source, Iterable<T_ID> targetIds, String fieldName) {
		throw new UnsupportedOperationException("removeRelations not implemented");

	}

	@Override
	public T findOneTarget(S_ID entityId, String fieldName, QueryParams queryParams) {
		throw new UnsupportedOperationException("findOneTarget not implemented");

	}

	@Override
	public Iterable<T> findManyTargets(S_ID entityId, String fieldName, QueryParams queryParams) {
		throw new UnsupportedOperationException("findManyTargets not implemented");
	}

}