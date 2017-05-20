package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.HierarchicalTask;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.DefaultResourceList;

import java.util.HashMap;
import java.util.Map;

public class HierarchicalTaskRepository extends ResourceRepositoryBase<HierarchicalTask, Long> {

	private static Map<Long, HierarchicalTask> hierarchicalTasks = new HashMap<>();

	public HierarchicalTaskRepository() {
		super(HierarchicalTask.class);
	}

	public static void clear() {
		hierarchicalTasks.clear();
	}

	@Override
	public DefaultResourceList<HierarchicalTask> findAll(QuerySpec querySpec) {
		return querySpec.apply(hierarchicalTasks.values());
	}

	@Override
	public <S extends HierarchicalTask> S save(S entity) {
		hierarchicalTasks.put(entity.getId(), entity);
		return null;
	}

	@Override
	public void delete(Long id) {
		hierarchicalTasks.remove(id);
	}
}