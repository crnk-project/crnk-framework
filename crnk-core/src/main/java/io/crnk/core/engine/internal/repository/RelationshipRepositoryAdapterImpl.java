package io.crnk.core.engine.internal.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.filter.RepositoryFilterContext;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ImmediateResult;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.BulkRelationshipRepositoryV2;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.legacy.repository.RelationshipRepository;

/**
 * A repository adapter for relationship repository.
 */
@SuppressWarnings("unchecked")
public class RelationshipRepositoryAdapterImpl extends ResponseRepositoryAdapter implements RelationshipRepositoryAdapter {

	private final Object relationshipRepository;

	private final ResourceField field;

	public RelationshipRepositoryAdapterImpl(ResourceField field, ModuleRegistry moduleRegistry,
											 Object relationshipRepository) {
		super(moduleRegistry);
		this.field = field;
		this.relationshipRepository = relationshipRepository;
	}

	@SuppressWarnings("rawtypes")
	public Result<JsonApiResponse> setRelation(Object source, Object targetId, ResourceField field, QueryAdapter queryAdapter) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Object source = request.getEntity();
				Serializable targetId = request.getId();
				ResourceField field = request.getRelationshipField();
				if (relationshipRepository instanceof RelationshipRepositoryV2) {
					((RelationshipRepositoryV2) relationshipRepository).setRelation(source, targetId, field.getUnderlyingName());
				} else {
					((RelationshipRepository) relationshipRepository).setRelation(source, targetId, field.getUnderlyingName());
				}
				return new JsonApiResponse();
			}
		};
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl
				.forRelation(moduleRegistry, HttpMethod.PATCH, source, queryAdapter, Arrays.asList(targetId), field);
		return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
	}

	@SuppressWarnings("rawtypes")
	public Result<JsonApiResponse> setRelations(Object source, Collection targetIds, ResourceField field, QueryAdapter
			queryAdapter) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Object source = request.getEntity();
				Iterable<?> targetIds = request.getIds();
				ResourceField field = request.getRelationshipField();
				QueryAdapter queryAdapter = request.getQueryAdapter();
				if (relationshipRepository instanceof RelationshipRepositoryV2) {
					((RelationshipRepositoryV2) relationshipRepository)
							.setRelations(source, targetIds, field.getUnderlyingName());
				} else {
					((RelationshipRepository) relationshipRepository).setRelations(source, targetIds, field.getUnderlyingName());
				}
				return new JsonApiResponse();
			}
		};
		RepositoryRequestSpec requestSpec =
				RepositoryRequestSpecImpl.forRelation(moduleRegistry, HttpMethod.PATCH, source, queryAdapter, targetIds, field);
		return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
	}

	@SuppressWarnings("rawtypes")
	public Result<JsonApiResponse> addRelations(Object source, Collection targetIds, ResourceField field, QueryAdapter
			queryAdapter) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Object source = request.getEntity();
				Iterable<?> targetIds = request.getIds();
				ResourceField field = request.getRelationshipField();
				QueryAdapter queryAdapter = request.getQueryAdapter();
				if (relationshipRepository instanceof RelationshipRepositoryV2) {
					((RelationshipRepositoryV2) relationshipRepository)
							.addRelations(source, targetIds, field.getUnderlyingName());
				} else {
					((RelationshipRepository) relationshipRepository).addRelations(source, targetIds, field.getUnderlyingName());
				}
				return new JsonApiResponse();
			}
		};
		RepositoryRequestSpec requestSpec =
				RepositoryRequestSpecImpl.forRelation(moduleRegistry, HttpMethod.POST, source, queryAdapter, targetIds, field);
		return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
	}

	@SuppressWarnings("rawtypes")
	public Result<JsonApiResponse> removeRelations(Object source, Collection targetIds, ResourceField field,
												   QueryAdapter queryAdapter) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Object source = request.getEntity();
				Iterable<?> targetIds = request.getIds();
				ResourceField field = request.getRelationshipField();
				QueryAdapter queryAdapter = request.getQueryAdapter();
				if (relationshipRepository instanceof RelationshipRepositoryV2) {
					((RelationshipRepositoryV2) relationshipRepository)
							.removeRelations(source, targetIds, field.getUnderlyingName());
				} else {
					((RelationshipRepository) relationshipRepository)
							.removeRelations(source, targetIds, field.getUnderlyingName());
				}
				return new JsonApiResponse();
			}
		};
		RepositoryRequestSpec requestSpec =
				RepositoryRequestSpecImpl.forRelation(moduleRegistry, HttpMethod.DELETE, source, queryAdapter, targetIds, field);
		return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
	}

	@SuppressWarnings("rawtypes")
	public Result<JsonApiResponse> findOneTarget(Object sourceId, ResourceField field, QueryAdapter queryAdapter) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Serializable sourceId = request.getId();
				ResourceField field = request.getRelationshipField();
				QueryAdapter queryAdapter = request.getQueryAdapter();

				Object resource;
				if (relationshipRepository instanceof RelationshipRepositoryV2) {
					RelationshipRepositoryV2 querySpecRepository = (RelationshipRepositoryV2) relationshipRepository;
					ResourceInformation targetResourceInformation =
							moduleRegistry.getResourceRegistry().getEntry(field.getOppositeResourceType())
									.getResourceInformation();
					resource = querySpecRepository
							.findOneTarget(sourceId, field.getUnderlyingName(), request.getQuerySpec(targetResourceInformation));
				} else {
					resource = ((RelationshipRepository) relationshipRepository)
							.findOneTarget(sourceId, field.getUnderlyingName(), request.getQueryParams());
				}
				return getResponse(relationshipRepository, resource, request);
			}
		};
		RepositoryRequestSpec requestSpec =
				RepositoryRequestSpecImpl.forFindTarget(moduleRegistry, queryAdapter, Arrays.asList(sourceId), field);
		return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
	}

	public Result<JsonApiResponse> findManyTargets(Object sourceId, ResourceField field, QueryAdapter queryAdapter) {
		RepositoryRequestFilterChainImpl chain = new RepositoryRequestFilterChainImpl() {

			@Override
			protected JsonApiResponse invoke(RepositoryFilterContext context) {
				RepositoryRequestSpec request = context.getRequest();
				Serializable sourceId = request.getId();
				ResourceField field = request.getRelationshipField();

				Object resources;
				if (relationshipRepository instanceof RelationshipRepositoryV2) {
					RelationshipRepositoryV2 querySpecRepository = (RelationshipRepositoryV2) relationshipRepository;
					ResourceInformation targetResourceInformation =
							moduleRegistry.getResourceRegistry().getEntry(field.getOppositeResourceType())
									.getResourceInformation();
					resources = querySpecRepository.findManyTargets(sourceId, field.getUnderlyingName(),
							request.getQuerySpec(targetResourceInformation));
				} else {
					resources = ((RelationshipRepository) relationshipRepository)
							.findManyTargets(sourceId, field.getUnderlyingName(), request.getQueryParams());
				}
				return getResponse(relationshipRepository, resources, request);
			}
		};
		RepositoryRequestSpec requestSpec =
				RepositoryRequestSpecImpl.forFindTarget(moduleRegistry, queryAdapter, Arrays.asList(sourceId), field);
		return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
	}

	@SuppressWarnings("rawtypes")
	public Result<Map<Object, JsonApiResponse>> findBulkManyTargets(Collection sourceIds, ResourceField field,
																	QueryAdapter queryAdapter) {
		if (relationshipRepository instanceof BulkRelationshipRepositoryV2) {
			RepositoryBulkRequestFilterChainImpl chain = new RepositoryBulkRequestFilterChainImpl() {

				@Override
				protected Map<Object, JsonApiResponse> invoke(RepositoryFilterContext context) {
					RepositoryRequestSpec request = context.getRequest();
					Iterable sourceIds = request.getIds();
					ResourceField field = request.getRelationshipField();
					QueryAdapter queryAdapter = request.getQueryAdapter();

					BulkRelationshipRepositoryV2 bulkRepository = (BulkRelationshipRepositoryV2) relationshipRepository;
					ResourceInformation targetResourceInformation =
							moduleRegistry.getResourceRegistry().getEntry(field.getOppositeResourceType()).getResourceInformation();
					QuerySpec querySpec = request.getQuerySpec(targetResourceInformation);
					MultivaluedMap targetsMap = bulkRepository.findTargets(sourceIds, field.getUnderlyingName(), querySpec);
					return toResponses(targetsMap, true, queryAdapter, field, HttpMethod.GET);
				}
			};
			RepositoryRequestSpec requestSpec =
					RepositoryRequestSpecImpl.forFindTarget(moduleRegistry, queryAdapter, new ArrayList<>(sourceIds), field);
			return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
		} else {
			// fallback to non-bulk operation
			Map<Object, JsonApiResponse> responseMap = new HashMap<>();
			for (Object sourceId : sourceIds) {
				JsonApiResponse response = findManyTargets(sourceId, field, queryAdapter).get();
				responseMap.put(sourceId, response);
			}
			return new ImmediateResult<>(responseMap);
		}
	}

	@SuppressWarnings("rawtypes")
	public Result<Map<Object, JsonApiResponse>> findBulkOneTargets(Collection sourceIds, ResourceField field, QueryAdapter
			queryAdapter) {

		if (relationshipRepository instanceof BulkRelationshipRepositoryV2) {

			RepositoryBulkRequestFilterChainImpl chain = new RepositoryBulkRequestFilterChainImpl() {

				@Override
				protected Map<Object, JsonApiResponse> invoke(RepositoryFilterContext context) {
					RepositoryRequestSpec request = context.getRequest();
					Iterable<?> sourceIds = request.getIds();
					ResourceField field = request.getRelationshipField();
					QueryAdapter queryAdapter = request.getQueryAdapter();

					BulkRelationshipRepositoryV2 bulkRepository = (BulkRelationshipRepositoryV2) relationshipRepository;
					ResourceInformation targetResourceInformation =
							moduleRegistry.getResourceRegistry().getEntry(field.getOppositeResourceType())
									.getResourceInformation();
					MultivaluedMap targetsMap = bulkRepository
							.findTargets(sourceIds, field.getUnderlyingName(), request.getQuerySpec(targetResourceInformation));
					return toResponses(targetsMap, false, queryAdapter, field, HttpMethod.GET);
				}
			};
			RepositoryRequestSpec requestSpec =
					RepositoryRequestSpecImpl.forFindTarget(moduleRegistry, queryAdapter, new ArrayList<>(sourceIds), field);
			return new ImmediateResult<>(chain.doFilter(newRepositoryFilterContext(requestSpec)));
		} else {
			// fallback to non-bulk operation
			Map<Object, JsonApiResponse> responseMap = new HashMap<>();
			for (Object sourceId : sourceIds) {
				JsonApiResponse response = findOneTarget(sourceId, field, queryAdapter).get();
				responseMap.put(sourceId, response);
			}
			return new ImmediateResult<>(responseMap);
		}
	}


	private Map<Object, JsonApiResponse> toResponses(MultivaluedMap targetsMap, boolean isMany, QueryAdapter queryAdapter,
													 ResourceField field, HttpMethod method) {
		Map<Object, JsonApiResponse> responseMap = new HashMap<>();
		for (Object sourceId : targetsMap.keySet()) {
			Object targets;
			if (isMany) {
				targets = targetsMap.getList(sourceId);
			} else {
				targets = targetsMap.getUnique(sourceId, true);
			}
			RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl
					.forRelation(moduleRegistry, method, null, queryAdapter, Collections.singleton(sourceId), field);
			JsonApiResponse response = getResponse(relationshipRepository, targets, requestSpec);
			responseMap.put(sourceId, response);
		}
		return responseMap;
	}

	public Object getRelationshipRepository() {
		return relationshipRepository;
	}

	@Override
	public ResourceField getResourceField() {
		return field;
	}
}
