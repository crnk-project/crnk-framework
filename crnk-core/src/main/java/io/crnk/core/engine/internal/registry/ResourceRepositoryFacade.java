package io.crnk.core.engine.internal.registry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.error.ErrorResponse;
import io.crnk.core.engine.error.ExceptionMapper;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistry;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;

class ResourceRepositoryFacade implements ResourceRepositoryV2<Object, Serializable> {

	private final ModuleRegistry moduleRegistry;

	private RegistryEntry entry;

	public ResourceRepositoryFacade(RegistryEntry entry, ModuleRegistry moduleRegistry) {
		this.entry = entry;
		this.moduleRegistry = Objects.requireNonNull(moduleRegistry);
	}

	@Override
	public Class getResourceClass() {
		return entry.getResourceInformation().getImplementationClass();
	}

	@Override
	public Object findOne(Serializable id, QuerySpec querySpec) {
		ResourceRepositoryAdapter adapter = entry.getResourceRepository();
		return toResource(adapter.findOne(id, toAdapter(querySpec)));
	}


	@Override
	public ResourceList findAll(QuerySpec querySpec) {
		ResourceRepositoryAdapter adapter = entry.getResourceRepository();
		return toResources(adapter.findAll(toAdapter(querySpec)));
	}

	@Override
	public ResourceList findAll(Iterable ids, QuerySpec querySpec) {
		ResourceRepositoryAdapter adapter = entry.getResourceRepository();
		return toResources(adapter.findAll(ids, toAdapter(querySpec)));
	}

	@Override
	public Object save(Object resource) {
		ResourceRepositoryAdapter adapter = entry.getResourceRepository();
		return toResource(adapter.update(resource, createEmptyAdapter()));
	}

	@Override
	public Object create(Object resource) {
		ResourceRepositoryAdapter adapter = entry.getResourceRepository();
		return toResource(adapter.create(resource, createEmptyAdapter()));
	}

	@Override
	public void delete(Serializable id) {
		ResourceRepositoryAdapter adapter = entry.getResourceRepository();
		toResource(adapter.delete(id, createEmptyAdapter()));
	}

	private QueryAdapter createEmptyAdapter() {
		return toAdapter(new QuerySpec(getResourceClass()));
	}

	private QueryAdapter toAdapter(QuerySpec querySpec) {
		ResourceRegistry resourceRegistry = moduleRegistry.getResourceRegistry();
		HttpRequestContext requestContext = moduleRegistry.getHttpRequestContextProvider().getRequestContext();
		QueryContext queryContext = requestContext != null ? requestContext.getQueryContext() : null;
		return new QuerySpecAdapter(querySpec, resourceRegistry, queryContext);
	}


	private ResourceList toResources(Result<JsonApiResponse> responseResult) {
		JsonApiResponse response = responseResult.get();
		Collection elements = (Collection) toResource(responseResult);

		DefaultResourceList result = new DefaultResourceList();
		result.addAll(elements);
		result.setMeta(response.getMetaInformation());
		result.setLinks(response.getLinksInformation());
		return result;
	}

	private Object toResource(Result<JsonApiResponse> responseResult) {
		JsonApiResponse response = responseResult.get();
		if (response.getErrors() != null && response.getErrors().iterator().hasNext()) {

			List<ErrorData> errorList = new ArrayList<>();
			response.getErrors().forEach(it -> errorList.add(it));
			Optional<Integer> errorCode = errorList.stream().filter(it -> it.getStatus() != null)
					.map(it -> Integer.parseInt(it.getStatus()))
					.collect(Collectors.maxBy(Integer::compare));

			ErrorResponse errorResponse = new ErrorResponse(errorList, errorCode.get());

			ExceptionMapperRegistry exceptionMapperRegistry = moduleRegistry.getExceptionMapperRegistry();
			ExceptionMapper<Throwable> exceptionMapper = exceptionMapperRegistry.findMapperFor(errorResponse).get();
			return exceptionMapper.fromErrorResponse(errorResponse);
		}
		return response.getEntity();
	}
}