package io.crnk.core.queryspec.internal;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.IncludeFieldSpec;
import io.crnk.core.queryspec.IncludeRelationSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.pagingspec.PagingSpec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class QuerySpecAdapter implements QueryAdapter {

    private final QueryContext queryContext;

    private QuerySpec querySpec;

    private ResourceRegistry resourceRegistry;

    private boolean compactMode;

    private boolean isSelfLink;

    public QuerySpecAdapter(QuerySpec querySpec, ResourceRegistry resourceRegistry, QueryContext queryContext) {
        this.querySpec = querySpec;
        this.resourceRegistry = resourceRegistry;
        this.queryContext = queryContext;
        if (queryContext != null && queryContext.getRequestPath() != null) {
            this.isSelfLink = queryContext.getRequestPath().contains("/relationships");
        }
    }

    public QuerySpec getQuerySpec() {
        return querySpec;
    }

    @Override
    public Map<String, Set<PathSpec>> getIncludedRelations() {
        Map<String, Set<PathSpec>> params = new HashMap<>();
        if (querySpec != null) {
            addRelations(params, querySpec);
            for (QuerySpec relatedSpec : querySpec.getNestedSpecs()) {
                addRelations(params, relatedSpec);
            }
        }
        return params;
    }

    private void addRelations(Map<String, Set<PathSpec>> params, QuerySpec spec) {
        if (!spec.getIncludedRelations().isEmpty()) {
            Set<PathSpec> set = new HashSet<>();
            for (IncludeRelationSpec relation : spec.getIncludedRelations()) {
                set.add(relation.getPath());
            }
            params.put(getResourceType(spec), set);
        }
    }

    private String getResourceType(QuerySpec spec) {
        if (spec.getResourceType() != null) {
            return spec.getResourceType();
        }
        RegistryEntry entry = resourceRegistry.getEntry(spec.getResourceClass());
        ResourceInformation resourceInformation = entry.getResourceInformation();
        return resourceInformation.getResourceType();
    }

    @Override
    public Map<String, Set<PathSpec>> getIncludedFields() {
        Map<String, Set<PathSpec>> params = new HashMap<>();
        if (querySpec != null) {
            addFields(params, querySpec);
            for (QuerySpec relatedSpec : querySpec.getNestedSpecs()) {
                addFields(params, relatedSpec);
            }
        }
        return params;
    }

    private void addFields(Map<String, Set<PathSpec>> params, QuerySpec spec) {
        if (!spec.getIncludedFields().isEmpty()) {
            Set<PathSpec> set = new HashSet<>();
            for (IncludeFieldSpec relation : spec.getIncludedFields()) {
                set.add(relation.getPath());
            }
            params.put(getResourceType(spec), set);
        }
    }

    @Override
    public ResourceInformation getResourceInformation() {
        return resourceRegistry.getEntry(getResourceType(querySpec)).getResourceInformation();
    }

    @Override
    public ResourceRegistry getResourceRegistry() {
        return resourceRegistry;
    }

    @Override
    public QueryContext getQueryContext() {
        return queryContext;
    }

    @Override
    public QueryAdapter duplicate() {
        QuerySpecAdapter adapter = new QuerySpecAdapter(querySpec != null ? querySpec.clone() : null, resourceRegistry, queryContext);
        adapter.setCompactMode(compactMode);
        return adapter;
    }

    @Override
    public QuerySpec toQuerySpec() {
        return getQuerySpec();
    }

    @Override
    public boolean getCompactMode() {
        return compactMode;
    }

    @Override
    public void setPagingSpec(final PagingSpec pagingSpec) {
        querySpec.setPaging(pagingSpec);
    }

    @Override
    public PagingSpec getPagingSpec() {
        return querySpec.getPaging();
    }

    @Override
    public boolean isEmpty() {
        return querySpec == null;
    }

    public void setCompactMode(boolean compactMode) {
        this.compactMode = compactMode;
    }

    @Override
    public String toString() {
        return querySpec.toString();
    }

    @Override
    public boolean isSelfLink() {
        return isSelfLink;
    }
}
