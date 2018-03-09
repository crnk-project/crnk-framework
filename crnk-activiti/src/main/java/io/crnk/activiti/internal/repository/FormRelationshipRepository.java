package io.crnk.activiti.internal.repository;


import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextAware;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.repository.ReadOnlyRelationshipRepositoryBase;
import io.crnk.activiti.resource.FormResource;
import io.crnk.activiti.resource.TaskResource;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;


public class FormRelationshipRepository<T extends TaskResource, F extends FormResource> extends ReadOnlyRelationshipRepositoryBase<T,
		String, F, String> implements ResourceRegistryAware, HttpRequestContextAware {

	private static final String RELATIONSHIP_NAME = "form";

	private final Class<T> taskClass;

	private final Class<F> formClass;

	private ResourceRegistry resourceRegistry;

	private HttpRequestContextProvider requestContextProvider;

	public FormRelationshipRepository(Class<T> taskClass, Class<F> formClass) {
		this.taskClass = taskClass;
		this.formClass = formClass;
	}

	@Override
	public Class<T> getSourceResourceClass() {
		return taskClass;
	}

	@Override
	public Class<F> getTargetResourceClass() {
		return formClass;
	}

	@Override
	public F findOneTarget(String taskId, String fieldName, QuerySpec querySpec) {
		if (RELATIONSHIP_NAME.equals(fieldName)) {

			ResourceRepositoryAdapter resourceRepository = resourceRegistry.getEntry(formClass).getResourceRepository();

			HttpRequestContext requestContext = requestContextProvider.getRequestContext();
			QueryContext queryContext = requestContext.getQueryContext();
			QuerySpecAdapter querySpecAdapter = new QuerySpecAdapter(querySpec, resourceRegistry, queryContext);

			return (F) resourceRepository.findOne(taskId, querySpecAdapter).get().getEntity();
		} else {
			throw new UnsupportedOperationException("unknown fieldName '" + fieldName + "'");
		}
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
