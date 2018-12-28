package io.crnk.core.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;

import java.io.Serializable;
import java.util.Collection;

/**
 * @deprecated make use of {@link ResourceRepository}.
 */
@Deprecated
public interface ResourceRepositoryV2<T, I extends Serializable> extends ResourceRepository<T, I> {

    /**
     * Make use of {@link #findAll(Collection, QuerySpec)}.
     */
    ResourceList<T> findAll(Iterable<I> ids, QuerySpec querySpec);

    @Override
    default ResourceList<T> findAll(Collection<I> ids, QuerySpec querySpec) {
        return this.findAll((Iterable) ids, querySpec);
    }
}
