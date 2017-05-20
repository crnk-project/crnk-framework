package io.crnk.security.model;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

import java.util.HashMap;
import java.util.Map;

public class TaskRepository extends ResourceRepositoryBase<Task, Long> {

	private static final Map<Long, Task> TASKS = new HashMap<>();

	public TaskRepository() {
		super(Task.class);
	}

	public static void clear() {
		TASKS.clear();
	}

	@Override
	public <S extends Task> S save(S entity) {
		TASKS.put(entity.getId(), entity);
		return entity;
	}

	@Override
	public ResourceList<Task> findAll(QuerySpec querySpec) {
		return querySpec.apply(TASKS.values());
	}

	@Override
	public void delete(Long id) {
		TASKS.remove(id);
	}
}
