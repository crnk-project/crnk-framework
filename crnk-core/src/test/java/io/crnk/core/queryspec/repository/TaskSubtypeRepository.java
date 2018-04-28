package io.crnk.core.queryspec.repository;

import io.crnk.core.mock.models.TaskSubType;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

import java.util.HashMap;
import java.util.Map;

public class TaskSubtypeRepository extends ResourceRepositoryBase<TaskSubType, Long> {

	private static Map<Long, TaskSubType> resources = new HashMap<>();

	public TaskSubtypeRepository() {
		super(TaskSubType.class);
	}

	public static void clear() {
		resources.clear();
	}

	@Override
	public ResourceList<TaskSubType> findAll(QuerySpec querySpec) {
		return querySpec.apply(resources.values());
	}

	@Override
	public <S extends TaskSubType> S save(S entity) {
		resources.put(entity.getId(), entity);
		return null;
	}

	@Override
	public void delete(Long id) {
		resources.remove(id);
	}
}