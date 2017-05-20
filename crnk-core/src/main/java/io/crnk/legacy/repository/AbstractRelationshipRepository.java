package io.crnk.legacy.repository;

import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.annotations.*;

import java.io.Serializable;

public abstract class AbstractRelationshipRepository<S, S_ID extends Serializable, T, T_ID extends Serializable>
		implements RelationshipRepository<S, S_ID, T, T_ID> {


	@Override
	@JsonApiSetRelation
	public void setRelation(S source, T_ID targetId, String fieldName) {
		throw new UnsupportedOperationException("setRelation not implemented");
	}

	@Override
	@JsonApiSetRelations
	public void setRelations(S source, Iterable<T_ID> targetIds, String fieldName) {
		throw new UnsupportedOperationException("setRelations not implemented");

	}

	@Override
	@JsonApiAddRelations
	public void addRelations(S source, Iterable<T_ID> targetIds, String fieldName) {
		throw new UnsupportedOperationException("addRelations not implemented");

	}

	@Override
	@JsonApiRemoveRelations
	public void removeRelations(S source, Iterable<T_ID> targetIds, String fieldName) {
		throw new UnsupportedOperationException("removeRelations not implemented");

	}

	@Override
	@JsonApiFindOneTarget
	public T findOneTarget(S_ID entityId, String fieldName, QueryParams queryParams) {
		throw new UnsupportedOperationException("findOneTarget not implemented");

	}

	@Override
	@JsonApiFindManyTargets
	public Iterable<T> findManyTargets(S_ID entityId, String fieldName, QueryParams queryParams) {
		throw new UnsupportedOperationException("findManyTargets not implemented");
	}

}