package io.crnk.core.repository;

import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code RelationshipRepository} implementation that provides additional support to bulk-request relations. This
 * is beneficial in most cases where inclusions are involved, as it allows to request all relationships in one step
 * instead of individual steps for each resource.
 *
 * @deprecated use OneRelationshipRepository and ManyRelationshipRepository
 */
@Deprecated
public interface BulkRelationshipRepository<T, I, D, J>
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

    /**
     * default method to maintain compatibility for {@link ManyRelationshipRepository} and {@link OneRelationshipRepository}
     */
    @Override
    default Map<I, ResourceList<D>> findManyRelations(Collection<I> sourceIds, String fieldName, QuerySpec querySpec) {
        MultivaluedMap<I, D> targets = findTargets(sourceIds, fieldName, querySpec);
        Map<I, ResourceList<D>> map = new HashMap<>();
        for (I sourceId : sourceIds) {
            if(!targets.containsKey(sourceId)){
                map.put(sourceId, (ResourceList<D>) new DefaultResourceList());
            }else {
                List<D> list = targets.getList(sourceId);
                if (!(list instanceof ResourceList)) {
                    DefaultResourceList resourceList = new DefaultResourceList();
                    resourceList.addAll(list);
                    list = resourceList;
                }
                map.put(sourceId, (ResourceList<D>) list);
            }
        }
        return map;
    }

    @Override
    default Map<I, D> findOneRelations(Collection<I> sourceIds, String fieldName, QuerySpec querySpec) {
        MultivaluedMap<I, D> targets = findTargets(sourceIds, fieldName, querySpec);
        Map<I, D> map = new HashMap<>();
        for (I sourceId : sourceIds) {
            map.put(sourceId, targets.getUnique(sourceId, true));
        }
        return map;
    }
}
