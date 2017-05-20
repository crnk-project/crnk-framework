package io.crnk.core.repository;

import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.queryspec.QuerySpec;

import java.io.Serializable;

/**
 * {@code RelationshipRepositoryV2} implementation that provides additional support to bulk-request relations.
 */
public interface BulkRelationshipRepositoryV2<T, I extends Serializable, D, J extends Serializable>
		extends RelationshipRepositoryV2<T, I, D, J> {

	/**
	 * Bulk request multiple targets at once.
	 */
	MultivaluedMap<I, D> findTargets(Iterable<I> sourceIds, String fieldName, QuerySpec querySpec);

}
