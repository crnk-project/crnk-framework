package io.crnk.reactive.internal.adapter;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.repository.RelationshipRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.RepositoryRequestSpecImpl;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.Result;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.reactive.repository.ReactiveManyRelationshipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;


public class ReactiveManyRelationshipRepositoryAdapter extends ReactiveRepositoryAdapterBase implements RelationshipRepositoryAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReactiveManyRelationshipRepository.class);

	private final ReactiveManyRelationshipRepository repository;

	private final ResourceField field;


	public ReactiveManyRelationshipRepositoryAdapter(ResourceField field, RelationshipRepositoryInformation repositoryInformation, ModuleRegistry moduleRegistry,
													 ReactiveManyRelationshipRepository repository) {
		super(moduleRegistry);
		this.repository = repository;
		this.field = field;
	}

	@Override
	public Result<JsonApiResponse> setRelation(Object source, Object targetId, ResourceField field, QueryAdapter queryAdapter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Result<JsonApiResponse> setRelations(Object source, Collection targetIds, ResourceField field, QueryAdapter
			queryAdapter) {
		RepositoryRequestSpec requestSpec =
				RepositoryRequestSpecImpl.forRelation(moduleRegistry, HttpMethod.PATCH, source, queryAdapter, targetIds, field);

		LOGGER.debug("setRelations for {} on {} with {}", targetIds, field, repository);
		Mono result = repository.setRelations(source, targetIds, field);
		return toResponse(result, requestSpec);
	}

	@Override
	public Result<JsonApiResponse> addRelations(Object source, Collection targetIds, ResourceField field, QueryAdapter
			queryAdapter) {
		RepositoryRequestSpec requestSpec =
				RepositoryRequestSpecImpl.forRelation(moduleRegistry, HttpMethod.POST, source, queryAdapter, targetIds, field);

		LOGGER.debug("addRelations for {} on {} with {}", targetIds, field, repository);
		Mono result = repository.addRelations(source, targetIds, field);
		return toResponse(result, requestSpec);
	}

	@Override
	public Result<JsonApiResponse> removeRelations(Object source, Collection targetIds, ResourceField field,
												   QueryAdapter queryAdapter) {

		LOGGER.debug("removeRelations for {} on {} with {}", targetIds, field, repository);
		RepositoryRequestSpec requestSpec =
				RepositoryRequestSpecImpl.forRelation(moduleRegistry, HttpMethod.DELETE, source, queryAdapter, targetIds, field);
		Mono result = repository.removeRelations(source, targetIds, field);
		return toResponse(result, requestSpec);
	}

	@Override
	public Result<JsonApiResponse> findOneRelations(Object sourceId, ResourceField field, QueryAdapter queryAdapter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Result<JsonApiResponse> findManyRelations(Object sourceId, ResourceField field, QueryAdapter queryAdapter) {
		LOGGER.debug("findManyRelations for sourceId={} on {} with {}",sourceId, field, repository);
		RepositoryRequestSpec requestSpec =
				RepositoryRequestSpecImpl.forFindTarget(moduleRegistry, queryAdapter, Arrays.asList(sourceId), field);
		QuerySpec querySpec = queryAdapter.toQuerySpec();
		Mono result = repository.findManyTargets(Arrays.asList(sourceId), field, querySpec);
		Result<Map<Object, JsonApiResponse>> responses = toResponses(result, true, queryAdapter, field, HttpMethod.GET, requestSpec);
		return responses.map(this::toSingleResult);
	}

	@Override
	public Result<Map<Object, JsonApiResponse>> findBulkManyTargets(Collection sourceIds, ResourceField field,
																	QueryAdapter queryAdapter) {
		LOGGER.debug("findManyRelations for sourceIds={} on {} with {}",sourceIds, field, repository);
		RepositoryRequestSpec requestSpec = RepositoryRequestSpecImpl.forFindTarget(moduleRegistry, queryAdapter, new ArrayList<>(sourceIds), field);
		QuerySpec querySpec = queryAdapter.toQuerySpec();
		Mono<Map> result = repository.findManyTargets(sourceIds, field, querySpec);
		return toResponses(result, false, queryAdapter, field, HttpMethod.GET, requestSpec);
	}

	@Override
	public Result<Map<Object, JsonApiResponse>> findBulkOneTargets(Collection sourceIds, ResourceField field, QueryAdapter
			queryAdapter) {
		throw new UnsupportedOperationException();
	}


	@Override
	public Object getRelationshipRepository() {
		return repository;
	}

	@Override
	public ResourceField getResourceField() {
		return field;
	}
}
