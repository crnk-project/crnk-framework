package io.crnk.core.mock.repository;

import java.util.HashMap;
import java.util.Map;

import io.crnk.core.mock.models.TopTask;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

public class TopTaskRepository extends ResourceRepositoryBase<TopTask, Long> {

	private Map<Long, TopTask> tasks = new HashMap<>();

	public TopTaskRepository() {
		super(TopTask.class);
	}

	@Override
	public ResourceList<TopTask> findAll(final QuerySpec querySpec) {
		return querySpec.apply(tasks.values());
	}

	@Override
	public <S extends TopTask> S save(S entity) {
		tasks.put(entity.getId(), entity);
		return entity;
	}

	@Override
	public void delete(Long id) {
		tasks.remove(id);
	}
}