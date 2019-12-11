package io.crnk.reactive.internal.adapter;

import java.io.Serializable;
import java.util.Collection;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.RepositoryRequestSpecImpl;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.Result;
import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.reactive.repository.ReactiveResourceRepository;
import reactor.core.publisher.Mono;

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
        if (!resourceInformation.getAccess().isReadable()) {
            throw new MethodNotAllowedException(HttpMethod.POST.toString());
        }
        QuerySpec querySpec = queryAdapter.toQuerySpec();
        RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindId(moduleRegistry, resourceInformation,
                queryAdapter, (Serializable) id);
        Mono result = repository.findOne(id, querySpec);
        return toResponse(result, requestSpec);
    }


    @Override
    public Result<JsonApiResponse> findAll(QueryAdapter queryAdapter) {
        if (!resourceInformation.getAccess().isReadable()) {
            throw new MethodNotAllowedException(HttpMethod.POST.toString());
        }
        QuerySpec querySpec = queryAdapter.toQuerySpec();
        RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindAll(moduleRegistry, resourceInformation,
                queryAdapter);
        Mono result = repository.findAll(querySpec);
        return toResponse(result, requestSpec);
    }

    @Override
    public Result<JsonApiResponse> findAll(Collection ids, QueryAdapter queryAdapter) {
		PreconditionUtil.verify(!ids.isEmpty(), "empty set of IDs passed as argument");
        if (!resourceInformation.getAccess().isReadable()) {
            throw new MethodNotAllowedException(HttpMethod.POST.toString());
        }
        QuerySpec querySpec = queryAdapter.toQuerySpec();
        RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindIds(moduleRegistry, resourceInformation,
                queryAdapter, ids);
        Mono result = repository.findAll(ids, querySpec);
        return toResponse(result, requestSpec);
    }

    @Override
    public Result<JsonApiResponse> update(Object entity, QueryAdapter queryAdapter) {
        if (!resourceInformation.getAccess().isPatchable()) {
            throw new MethodNotAllowedException(HttpMethod.POST.toString());
        }

        RepositoryRequestSpec requestSpec =
                RepositoryRequestSpecImpl.forSave(moduleRegistry, HttpMethod.PATCH, resourceInformation, queryAdapter, entity);
        Mono result = repository.save(entity);
        return toResponse(result, requestSpec);
    }

    @Override
    public Result<JsonApiResponse> create(Object entity, QueryAdapter queryAdapter) {
        if (!resourceInformation.getAccess().isPostable()) {
            throw new MethodNotAllowedException(HttpMethod.POST.toString());
        }

        RepositoryRequestSpec requestSpec =
                RepositoryRequestSpecImpl.forSave(moduleRegistry, HttpMethod.POST, resourceInformation, queryAdapter, entity);
        Mono result = repository.create(entity);
        return toResponse(result, requestSpec);
    }

    @Override
    public Result<JsonApiResponse> delete(Object id, QueryAdapter queryAdapter) {
        if (!resourceInformation.getAccess().isDeletable()) {
            throw new MethodNotAllowedException(HttpMethod.DELETE.toString());
        }

        RepositoryRequestSpec requestSpec =
                RepositoryRequestSpecImpl.forDelete(moduleRegistry, resourceInformation, queryAdapter, (Serializable) id);
        Mono result = repository.delete(id);
        return toResponse(result, requestSpec);
    }

    @Override
    public Object getImplementation() {
        return repository;
    }

    @Override
    public ResourceRepositoryInformation getRepositoryInformation() {
        return repositoryInformation;
    }
}
