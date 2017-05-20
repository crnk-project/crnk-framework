package io.crnk.client;

import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.RelationshipRepository;

import java.io.Serializable;
import java.util.List;

/**
 * Implemented by every {@link RelationshipRepository} stub.
 *
 * @deprecated make use of QuerySpec
 */
@Deprecated
public interface RelationshipRepositoryStub<T, TID extends Serializable, D, DID extends Serializable>
		extends RelationshipRepository<T, TID, D, DID> {

	@Override
	List<D> findManyTargets(TID sourceId, String fieldName, QueryParams queryParams);


}
