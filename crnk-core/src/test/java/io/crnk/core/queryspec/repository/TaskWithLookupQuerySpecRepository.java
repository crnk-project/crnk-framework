package io.crnk.core.queryspec.repository;

import io.crnk.core.mock.models.TaskWithLookup;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

import java.util.HashMap;
import java.util.Map;

public class TaskWithLookupQuerySpecRepository extends ResourceRepositoryBase<TaskWithLookup, String> {

	private static Map<String, TaskWithLookup> taskWithLookups = new HashMap<>();

	public TaskWithLookupQuerySpecRepository() {
		super(TaskWithLookup.class);
	}

	public static void clear() {
		taskWithLookups.clear();
	}

	@Override
	public ResourceList findAll(QuerySpec querySpec) {
		return querySpec.apply(taskWithLookups.values());
	}

	@Override
	public <S extends TaskWithLookup> S save(S entity) {
		taskWithLookups.put(entity.getId(), entity);
		return null;
	}

	@Override
	public void delete(String id) {
		taskWithLookups.remove(id);
	}
}