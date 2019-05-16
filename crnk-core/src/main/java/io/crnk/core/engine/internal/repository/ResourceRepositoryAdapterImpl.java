package io.crnk.core.engine.internal.repository;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.filter.RepositoryFilterContext;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.ImmediateResult;
import io.crnk.core.engine.result.Result;
import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.legacy.repository.LegacyResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;

/**
 * A repository adapter for resource repository
 */
@SuppressWarnings("unchecked")
public class ResourceRepositoryAdapterImpl extends ResponseRepositoryAdapter implements ResourceRepositoryAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceRepositoryAdapterImpl.class);

    private final Object resourceRepository;

    private final ResourceRepositoryInformation repositoryInformation;
    private final ResourceInformation resourceInformation;

    private boolean return404OnNull;

    public ResourceRepositoryAdapterImpl(ResourceRepositoryInformation repositoryInformation, ModuleRegistry moduleRegistry,
                                         Object resourceRepository) {
        super(moduleRegistry);
        this.resourceInformation = repositoryInformation.getResource();
        this.repositoryInformation = repositoryInformation;
        this.resourceRepository = resourceRepository;
        return404OnNull =
                Boolean.parseBoolean(moduleRegistry.getPropertiesProvider().getProperty(CrnkProperties.RETURN_404_ON_NULL));
    }


    @Override
    public ResourceRepositoryInformation getRepositoryInformation() {
        return repositoryInformation;
    }

    @Override
    public Result<JsonApiResponse> findOne(Object id, QueryAdapter queryAdapter) {
        if (!resourceInformation.getAccess().isReadable()) {
            throw new MethodNotAllowedException(HttpMethod.GET.toString());
        }
        RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

            @SuppressWarnings("rawtypes")
            @Override
            protected JsonApiResponse invoke(RepositoryFilterContext context) {
                RepositoryRequestSpec request = context.getRequest();
                Serializable id = request.getId();
                Object resource;
                if (resourceRepository instanceof ResourceRepository) {
                    resource = ((ResourceRepository) resourceRepository).findOne(id, request.getQuerySpec
                            (resourceInformation));
                } else {
                    resource = ((LegacyResourceRepository) resourceRepository).findOne(id, request.getQueryParams());
                }
                if (resource == null && return404OnNull) {
                    throw new ResourceNotFoundException(resourceInformation.getResourceType());
                }
                return getResponse(resourceRepository, resource, request);
            }

        };
        RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindId(moduleRegistry, resourceInformation,
                queryAdapter, (Serializable) id);
        return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
    }

    @Override
    public Result<JsonApiResponse> findAll(QueryAdapter queryAdapter) {
        if (!resourceInformation.getAccess().isReadable()) {
            throw new MethodNotAllowedException(HttpMethod.GET.toString());
        }
        RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

            @SuppressWarnings("rawtypes")
            @Override
            protected JsonApiResponse invoke(RepositoryFilterContext context) {
                RepositoryRequestSpec request = context.getRequest();
                QueryAdapter queryAdapter = request.getQueryAdapter();
                Object resources;
                if (resourceRepository instanceof ResourceRepository) {
                    QuerySpec querySpec = request.getQuerySpec(resourceInformation);
                    resources = ((ResourceRepository) resourceRepository).findAll(querySpec);
                } else {
                    resources = ((LegacyResourceRepository) resourceRepository).findAll(request.getQueryParams());
                }
                return getResponse(resourceRepository, resources, request);
            }

        };
        RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindAll(moduleRegistry, resourceInformation,
                queryAdapter);
        return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
    }

    @Override
    public Result<JsonApiResponse> findAll(Collection ids, QueryAdapter queryAdapter) {
        if (!resourceInformation.getAccess().isReadable()) {
            throw new MethodNotAllowedException(HttpMethod.GET.toString());
        }
        RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

            @SuppressWarnings("rawtypes")
            @Override
            protected JsonApiResponse invoke(RepositoryFilterContext context) {
                RepositoryRequestSpec request = context.getRequest();
                QueryAdapter queryAdapter = request.getQueryAdapter();
                Collection<?> ids = request.getIds();
                Object resources;
                if (resourceRepository instanceof ResourceRepository) {
                    resources =
                            ((ResourceRepository) resourceRepository).findAll(ids, request.getQuerySpec(resourceInformation));
                } else {
                    resources = ((LegacyResourceRepository) resourceRepository).findAll(ids, request.getQueryParams());
                }
                return getResponse(resourceRepository, resources, request);
            }

        };
        RepositoryRequestSpec requestSpec =
                RepositoryRequestSpecImpl.forFindIds(moduleRegistry, resourceInformation, queryAdapter, ids);
        return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
    }

    @Override
    public Result<JsonApiResponse> update(Object entity, QueryAdapter queryAdapter) {
        return save(entity, queryAdapter, HttpMethod.PATCH);
    }

    @Override
    public Result<JsonApiResponse> create(Object entity, QueryAdapter queryAdapter) {
        return save(entity, queryAdapter, HttpMethod.POST);
    }

    private Result<JsonApiResponse> save(Object entity, QueryAdapter queryAdapter, final HttpMethod method) {
        if (method == HttpMethod.POST && !resourceInformation.getAccess().isPostable()) {
            throw new MethodNotAllowedException(method.toString());
        } else if (method == HttpMethod.PATCH && !resourceInformation.getAccess().isPatchable()) {
            throw new MethodNotAllowedException(method.toString());
        }

        RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

            @SuppressWarnings("rawtypes")
            @Override
            protected JsonApiResponse invoke(RepositoryFilterContext context) {
                RepositoryRequestSpec request = context.getRequest();
                Object entity = request.getEntity();

                Object resource;
                if (resourceRepository instanceof ResourceRepository) {
                    if (method == HttpMethod.POST) {
                        resource = ((ResourceRepository) resourceRepository).create(entity);
                    } else {
                        resource = ((ResourceRepository) resourceRepository).save(entity);
                    }
                } else {
                    resource = ((LegacyResourceRepository) resourceRepository).save(entity);
                }
                return getResponse(resourceRepository, resource, request);
            }

        };
        RepositoryRequestSpec requestSpec =
                RepositoryRequestSpecImpl.forSave(moduleRegistry, method, resourceInformation, queryAdapter, entity);
        return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
    }

    @Override
    public Result<JsonApiResponse> delete(Object id, QueryAdapter queryAdapter) {
        if (!resourceInformation.getAccess().isDeletable()) {
            throw new MethodNotAllowedException(HttpMethod.DELETE.toString());
        }
        RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

            @SuppressWarnings("rawtypes")
            @Override
            protected JsonApiResponse invoke(RepositoryFilterContext context) {
                RepositoryRequestSpec request = context.getRequest();
                Serializable id = request.getId();
                if (resourceRepository instanceof ResourceRepository) {
                    ((ResourceRepository) resourceRepository).delete(id);
                } else {
                    ((LegacyResourceRepository) resourceRepository).delete(id);
                }
                return new JsonApiResponse();
            }
        };
        RepositoryRequestSpec requestSpec =
                RepositoryRequestSpecImpl.forDelete(moduleRegistry, resourceInformation, queryAdapter, (Serializable) id);
        return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
    }

    public Object getResourceRepository() {
        return resourceRepository;
    }


    public Class<?> getResourceClass() {
        return resourceInformation.getResourceClass();
    }
}
