package io.crnk.core.engine.internal.repository;

import java.io.Serializable;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.filter.RepositoryFilterContext;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.legacy.internal.AnnotatedResourceRepositoryAdapter;
import io.crnk.legacy.repository.ResourceRepository;

/**
 * A repository adapter for resource repository
 */
@SuppressWarnings("unchecked")
public class ResourceRepositoryAdapter<T, I extends Serializable> extends ResponseRepositoryAdapter {

	private final Object resourceRepository;

	private final boolean isAnnotated;

	private boolean return404OnNull;

	public ResourceRepositoryAdapter(ResourceInformation resourceInformation, ModuleRegistry moduleRegistry,
			Object resourceRepository) {
		super(resourceInformation, moduleRegistry);
		this.resourceRepository = resourceRepository;
		this.isAnnotated = resourceRepository instanceof AnnotatedResourceRepositoryAdapter;
		return404OnNull =
				Boolean.parseBoolean(moduleRegistry.getPropertiesProvider().getProperty(CrnkProperties.RETURN_404_ON_NULL));
	}

	public JsonApiResponse findOne(I id, QueryAdapter queryAdapter) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@SuppressWarnings("rawtypes")
			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				QueryAdapter queryAdapter = request.getQueryAdapter();
				Serializable id = request.getId();
				Object resource;
				if (isAnnotated) {
					resource = ((AnnotatedResourceRepositoryAdapter) resourceRepository).findOne(id, queryAdapter);
				}
				else if (resourceRepository instanceof ResourceRepositoryV2) {
					resource = ((ResourceRepositoryV2) resourceRepository).findOne(id, request.getQuerySpec
							(resourceInformation));
				}
				else {
					resource = ((ResourceRepository) resourceRepository).findOne(id, request.getQueryParams());
				}
				if (resource == null && return404OnNull) {
					throw new ResourceNotFoundException(resourceInformation.getResourceType());
				}
				return getResponse(resourceRepository, resource, request);
			}

		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindId(moduleRegistry, resourceInformation,
				queryAdapter, id);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
	}

	public JsonApiResponse findAll(QueryAdapter queryAdapter) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@SuppressWarnings("rawtypes")
			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				QueryAdapter queryAdapter = request.getQueryAdapter();
				Object resources;
				if (isAnnotated) {
					resources = ((AnnotatedResourceRepositoryAdapter) resourceRepository).findAll(queryAdapter);
				}
				else if (resourceRepository instanceof ResourceRepositoryV2) {
					QuerySpec querySpec = request.getQuerySpec(resourceInformation);
					resources = ((ResourceRepositoryV2) resourceRepository).findAll(querySpec);
				}
				else {
					resources = ((ResourceRepository) resourceRepository).findAll(request.getQueryParams());
				}
				return getResponse(resourceRepository, resources, request);
			}

		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindAll(moduleRegistry, resourceInformation,
				queryAdapter);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
	}

	public JsonApiResponse findAll(Iterable ids, QueryAdapter queryAdapter) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@SuppressWarnings("rawtypes")
			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				QueryAdapter queryAdapter = request.getQueryAdapter();
				Iterable<?> ids = request.getIds();
				Object resources;
				if (isAnnotated) {
					resources = ((AnnotatedResourceRepositoryAdapter) resourceRepository).findAll(ids, queryAdapter);
				}
				else if (resourceRepository instanceof ResourceRepositoryV2) {
					resources =
							((ResourceRepositoryV2) resourceRepository).findAll(ids, request.getQuerySpec(resourceInformation));
				}
				else {
					resources = ((ResourceRepository) resourceRepository).findAll(ids, request.getQueryParams());
				}
				return getResponse(resourceRepository, resources, request);
			}

		};
		RepositoryRequestSpec requestSpec =
				RepositoryRequestSpecImpl.forFindIds(moduleRegistry, resourceInformation, queryAdapter, ids);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
	}

	public <S extends T> JsonApiResponse update(S entity, QueryAdapter queryAdapter) {
		return save(entity, queryAdapter, HttpMethod.PATCH);
	}

	public <S extends T> JsonApiResponse create(S entity, QueryAdapter queryAdapter) {
		return save(entity, queryAdapter, HttpMethod.POST);
	}

	private <S extends T> JsonApiResponse save(S entity, QueryAdapter queryAdapter, final HttpMethod method) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@SuppressWarnings("rawtypes")
			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Object entity = request.getEntity();

				Object resource;
				if (isAnnotated) {
					resource = ((AnnotatedResourceRepositoryAdapter) resourceRepository).save(entity);
				}
				else if (resourceRepository instanceof ResourceRepositoryV2) {
					if (method == HttpMethod.POST) {
						resource = ((ResourceRepositoryV2) resourceRepository).create(entity);
					}
					else {
						resource = ((ResourceRepositoryV2) resourceRepository).save(entity);
					}
				}
				else {
					resource = ((ResourceRepository) resourceRepository).save(entity);
				}
				return getResponse(resourceRepository, resource, request);
			}

		};
		RepositoryRequestSpec requestSpec =
				RepositoryRequestSpecImpl.forSave(moduleRegistry, method, resourceInformation, queryAdapter, entity);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
	}

	public JsonApiResponse delete(I id, QueryAdapter queryAdapter) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@SuppressWarnings("rawtypes")
			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				QueryAdapter queryAdapter = request.getQueryAdapter();
				Serializable id = request.getId();
				if (isAnnotated) {
					((AnnotatedResourceRepositoryAdapter) resourceRepository).delete(id, queryAdapter);
				}
				else if (resourceRepository instanceof ResourceRepositoryV2) {
					((ResourceRepositoryV2) resourceRepository).delete(id);
				}
				else {
					((ResourceRepository) resourceRepository).delete(id);
				}
				return new JsonApiResponse();
			}
		};
		RepositoryRequestSpec requestSpec =
				RepositoryRequestSpecImpl.forDelete(moduleRegistry, resourceInformation, queryAdapter, id);
		return chain.doFilter(newRepositoryFilterContext(requestSpec));
	}

	public Object getResourceRepository() {
		return resourceRepository;
	}

	public Class<?> getResourceClass() {
		return resourceInformation.getResourceClass();
	}
}
