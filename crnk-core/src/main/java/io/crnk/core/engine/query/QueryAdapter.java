package io.crnk.core.engine.query;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.pagingspec.PagingSpec;

import java.util.Map;
import java.util.Set;

public interface QueryAdapter {

    Map<String, Set<PathSpec>> getIncludedRelations();

    Map<String, Set<PathSpec>> getIncludedFields();

    ResourceInformation getResourceInformation();

    ResourceRegistry getResourceRegistry();

    QueryContext getQueryContext();

    /**
     * @return maximum number of resources to return or null for unbounded
     */
    @Deprecated
    Long getLimit();

    @Deprecated
    void setLimit(Long limit);

    /**
     * @return maximum number of resources to skip in the response.
     */
    @Deprecated
    long getOffset();

    @Deprecated
    void setOffset(long offset);

    /**
     * @return duplicate of this instance
     */
    QueryAdapter duplicate();

    /**
     * The {@link QuerySpec} instance for this query adapter if possible.
     *
     * @return may return null if the implementation does not support QueryParams
     */
    QuerySpec toQuerySpec();

    boolean getCompactMode();

    void setPagingSpec(PagingSpec pagingSpec);

    PagingSpec getPagingSpec();

    boolean isEmpty();

    boolean isSelfLink();
}
