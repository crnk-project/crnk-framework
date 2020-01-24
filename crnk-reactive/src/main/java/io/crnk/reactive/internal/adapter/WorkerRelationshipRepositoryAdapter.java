package io.crnk.reactive.internal.adapter;

import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.Result;
import io.crnk.core.repository.response.JsonApiResponse;
import reactor.core.scheduler.Scheduler;

import java.util.Collection;
import java.util.Map;

public class WorkerRelationshipRepositoryAdapter implements RelationshipRepositoryAdapter {

	private final Worker worker;

	private RelationshipRepositoryAdapter adapter;


	public WorkerRelationshipRepositoryAdapter(RelationshipRepositoryAdapter adapter, Scheduler scheduler, HttpRequestContextProvider requestContextProvider) {
		this.adapter = adapter;
		this.worker = new Worker(requestContextProvider, scheduler);
	}

	@Override
	public Result<JsonApiResponse> setRelation(Object source, Object targetId, ResourceField field, QueryAdapter queryAdapter) {
		return worker.work(() -> adapter.setRelation(source, targetId, field, queryAdapter));
	}

	@Override
	public Result<JsonApiResponse> setRelations(Object source, Collection targetIds, ResourceField field, QueryAdapter queryAdapter) {
		return worker.work(() -> adapter.setRelations(source, targetIds, field, queryAdapter));
	}

	@Override
	public Result<JsonApiResponse> addRelations(Object source, Collection targetIds, ResourceField field, QueryAdapter queryAdapter) {
		return worker.work(() -> adapter.addRelations(source, targetIds, field, queryAdapter));
	}

	@Override
	public Result<JsonApiResponse> removeRelations(Object source, Collection targetIds, ResourceField field, QueryAdapter queryAdapter) {
		return worker.work(() -> adapter.removeRelations(source, targetIds, field, queryAdapter));
	}

	@Override
	public Result<JsonApiResponse> findOneRelations(Object sourceId, ResourceField field, QueryAdapter queryAdapter) {
		return worker.work(() -> adapter.findOneRelations(sourceId, field, queryAdapter));
	}

	@Override
	public Result<JsonApiResponse> findManyRelations(Object sourceId, ResourceField field, QueryAdapter queryAdapter) {
		return worker.work(() -> adapter.findManyRelations(sourceId, field, queryAdapter));
	}

	@Override
	public Result<Map<Object, JsonApiResponse>> findBulkManyTargets(Collection sourceIds, ResourceField field, QueryAdapter queryAdapter) {
		return worker.work(() -> adapter.findBulkManyTargets(sourceIds, field, queryAdapter));
	}

	@Override
	public Result<Map<Object, JsonApiResponse>> findBulkOneTargets(Collection sourceIds, ResourceField field, QueryAdapter queryAdapter) {
		return worker.work(() -> adapter.findBulkOneTargets(sourceIds, field, queryAdapter));
	}

	@Override
	public Object getImplementation() {
		return adapter.getImplementation();
	}

	@Override
	public ResourceField getResourceField() {
		return adapter.getResourceField();
	}
}
