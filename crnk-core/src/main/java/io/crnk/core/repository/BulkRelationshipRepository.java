package io.crnk.core.repository;

import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

/**
 * {@code RelationshipRepository} implementation that provides additional support to bulk-request relations. This
 * is beneficial in most cases where inclusions are involved, as it allows to request all relationships in one step
 * instead of individual steps for each resource.
 */
public interface BulkRelationshipRepository<T, I extends Serializable, D, J extends Serializable>
		extends RelationshipRepository<T, I, D, J> {

	/**
	 * Bulk request multiple targets at once.
	 */
	MultivaluedMap<I, D> findTargets(Collection<I> sourceIds, String fieldName, QuerySpec querySpec);

	@Override
	default D findOneTarget(I sourceId, String fieldName, QuerySpec querySpec) {
		MultivaluedMap<I, D> map = findTargets(Arrays.asList(sourceId), fieldName, querySpec);
		if (map.isEmpty()) {
			return null;
		}
		return map.getUnique(sourceId);
	}

	@Override
	default ResourceList<D> findManyTargets(I sourceId, String fieldName, QuerySpec querySpec) {
		MultivaluedMap<I, D> map = findTargets(Arrays.asList(sourceId), fieldName, querySpec);
		if (map.isEmpty()) {
			return new DefaultResourceList<>();
		}
		return (ResourceList<D>) map.getList(sourceId);
	}


}
