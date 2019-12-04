package io.crnk.core.mock.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

import java.util.HashMap;
import java.util.Map;

public class TaskWithPagingBehaviorRepository extends ResourceRepositoryBase<TaskWithPagingBehavior, String> {

	private static Map<String, TaskWithPagingBehavior> taskWithPagingBehavior = new HashMap<>();

	public TaskWithPagingBehaviorRepository() {
		super(TaskWithPagingBehavior.class);
	}

	public static void clear() {
		taskWithPagingBehavior.clear();
	}

	@Override
	public ResourceList findAll(QuerySpec querySpec) {
		return querySpec.apply(taskWithPagingBehavior.values());
	}

	@Override
	public <S extends TaskWithPagingBehavior> S save(S entity) {
		taskWithPagingBehavior.put(entity.getId(), entity);
		return null;
	}

	@Override
	public void delete(String id) {
		taskWithPagingBehavior.remove(id);
	}
}