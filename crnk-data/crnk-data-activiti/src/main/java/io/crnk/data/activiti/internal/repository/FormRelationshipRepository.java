package io.crnk.data.activiti.internal.repository;


import io.crnk.data.activiti.resource.FormResource;
import io.crnk.data.activiti.resource.TaskResource;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextAware;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.OneRelationshipRepositoryBase;
import io.crnk.core.repository.RelationshipMatcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class FormRelationshipRepository<T extends TaskResource, F extends FormResource> extends OneRelationshipRepositoryBase<T,
        String, F, String> implements ResourceRegistryAware, HttpRequestContextAware {

    private static final String RELATIONSHIP_NAME = "form";

    private ResourceRegistry resourceRegistry;

    private HttpRequestContextProvider requestContextProvider;

    @Override
    public RelationshipMatcher getMatcher() {
        RelationshipMatcher matcher = new RelationshipMatcher();
        matcher.rule().field(RELATIONSHIP_NAME).add();
        return matcher;
    }

    @Override
    public Map<String, F> findOneRelations(Collection<String> taskIds, String fieldName, QuerySpec querySpec) {
        Map<String, F> map = new HashMap<>();

        Class<?> formClass = querySpec.getResourceClass();

        for (String taskId : taskIds) {
            ResourceRepositoryAdapter resourceRepository = resourceRegistry.getEntry(formClass).getResourceRepository();
            HttpRequestContext requestContext = requestContextProvider.getRequestContext();
            QueryContext queryContext = requestContext.getQueryContext();
            QuerySpecAdapter querySpecAdapter = new QuerySpecAdapter(querySpec, resourceRegistry, queryContext);

            F form = (F) resourceRepository.findOne(taskId, querySpecAdapter).get().getEntity();
            map.put(taskId, form);
        }
        return map;
    }

    @Override
    public void setResourceRegistry(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public void setHttpRequestContextProvider(HttpRequestContextProvider requestContextProvider) {
        this.requestContextProvider = requestContextProvider;
    }
}
