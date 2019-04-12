package io.crnk.reactive.internal.adapter;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.RepositoryRequestSpecImpl;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.Result;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.reactive.repository.ReactiveResourceRepository;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.Collection;

public class ReactiveResourceRepositoryAdapter extends ReactiveRepositoryAdapterBase implements ResourceRepositoryAdapter {

	private final ReactiveResourceRepository repository;

	private final ResourceRepositoryInformation repositoryInformation;

	private final ResourceInformation resourceInformation;

	public ReactiveResourceRepositoryAdapter(ResourceRepositoryInformation repositoryInformation, ModuleRegistry moduleRegistry,
											 ReactiveResourceRepository repository) {
		super(moduleRegistry);
		this.repositoryInformation = repositoryInformation;
		this.resourceInformation = repositoryInformation.getResource();
		this.repository = repository;
	}


	@Override
	public Result<JsonApiResponse> findOne(Object id, QueryAdapter queryAdapter) {
		QuerySpec querySpec = queryAdapter.toQuerySpec();
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindId(moduleRegistry, resourceInformation,
				queryAdapter, (Serializable) id);
		Mono result = repository.findOne(id, querySpec);
		return toResponse(result, requestSpec);
	}


	@Override
	public Result<JsonApiResponse> findAll(QueryAdapter queryAdapter) {
		QuerySpec querySpec = queryAdapter.toQuerySpec();
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindAll(moduleRegistry, resourceInformation,
				queryAdapter);
		Mono result = repository.findAll(querySpec);
		return toResponse(result, requestSpec);
	}

	@Override
	public Result<JsonApiResponse> findAll(Collection ids, QueryAdapter queryAdapter) {
		QuerySpec querySpec = queryAdapter.toQuerySpec();
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindIds(moduleRegistry, resourceInformation,
				queryAdapter, ids);
		Mono result = repository.findAll((Collection) ids, querySpec);
		return toResponse(result, requestSpec);
	}

	@Override
	public Result<JsonApiResponse> update(Object entity, QueryAdapter queryAdapter) {
		RepositoryRequestSpec requestSpec =
				RepositoryRequestSpecImpl.forSave(moduleRegistry, HttpMethod.PATCH, resourceInformation, queryAdapter, entity);
		Mono result = repository.save(entity);
		return toResponse(result, requestSpec);
	}

	@Override
	public Result<JsonApiResponse> create(Object entity, QueryAdapter queryAdapter) {
		RepositoryRequestSpec requestSpec =
				RepositoryRequestSpecImpl.forSave(moduleRegistry, HttpMethod.POST, resourceInformation, queryAdapter, entity);
		Mono result = repository.create(entity);
		return toResponse(result, requestSpec);
	}

	@Override
	public Result<JsonApiResponse> delete(Object id, QueryAdapter queryAdapter) {
		RepositoryRequestSpec requestSpec =
				RepositoryRequestSpecImpl.forDelete(moduleRegistry, resourceInformation, queryAdapter, (Serializable) id);
		Mono result = repository.delete(id);
		return toResponse(result, requestSpec);
	}

	@Override
	public Object getResourceRepository() {
		return repository;
	}

	@Override
	public ResourceRepositoryInformation getRepositoryInformation() {
		return repositoryInformation;
	}
}
