package io.crnk.reactive.internal.adapter;

import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.Result;
import io.crnk.core.repository.response.JsonApiResponse;
import reactor.core.scheduler.Scheduler;

import java.util.Collection;

public class WorkerResourceRepositoryAdapter implements ResourceRepositoryAdapter {

	private final Worker worker;

	private ResourceRepositoryAdapter adapter;


	public WorkerResourceRepositoryAdapter(ResourceRepositoryAdapter adapter, Scheduler scheduler, HttpRequestContextProvider requestContextProvider) {
		this.adapter = adapter;
		this.worker = new Worker(requestContextProvider, scheduler);
	}

	@Override
	public Result<JsonApiResponse> findOne(Object id, QueryAdapter queryAdapter) {
		return worker.work(() -> adapter.findOne(id, queryAdapter));
	}

	@Override
	public Result<JsonApiResponse> findAll(QueryAdapter queryAdapter) {
		return worker.work(() -> adapter.findAll(queryAdapter));
	}

	@Override
	public Result<JsonApiResponse> findAll(Collection ids, QueryAdapter queryAdapter) {
		return worker.work(() -> adapter.findAll(ids, queryAdapter));
	}

	@Override
	public Result<JsonApiResponse> update(Object entity, QueryAdapter queryAdapter) {
		return worker.work(() -> adapter.update(entity, queryAdapter));
	}

	@Override
	public Result<JsonApiResponse> create(Object entity, QueryAdapter queryAdapter) {
		return worker.work(() -> adapter.create(entity, queryAdapter));
	}

	@Override
	public Result<JsonApiResponse> delete(Object id, QueryAdapter queryAdapter) {
		return worker.work(() -> adapter.delete(id, queryAdapter));
	}

	@Override
	public Object getImplementation() {
		return adapter.getImplementation();
	}

	@Override
	public ResourceRepositoryInformation getRepositoryInformation() {
		return adapter.getRepositoryInformation();
	}
}
