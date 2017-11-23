package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.LazyTask;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

import java.util.HashMap;
import java.util.Map;

public class LazyTaskRepository extends ResourceRepositoryBase<LazyTask, Long> {

	private static Map<Long, LazyTask> LazyTasks = new HashMap<>();

	public LazyTaskRepository() {
		super(LazyTask.class);
	}

	public static void clear() {
		LazyTasks.clear();
	}

	@Override
	public ResourceList<LazyTask> findAll(QuerySpec querySpec) {
		return querySpec.apply(LazyTasks.values());
	}

	@Override
	public <S extends LazyTask> S save(S entity) {
		LazyTasks.put(entity.getId(), entity);
		return null;
	}

	@Override
	public void delete(Long id) {
		LazyTasks.remove(id);
	}
}