package io.crnk.core.resource.paging.total;

import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TotalResourceCountTestRepository implements ResourceRepository<Task, Long> {

	private static List<Task> tasks = new ArrayList<>();

	public static void clear() {
		tasks.clear();
	}

	@Override
	public Class<Task> getResourceClass() {
		return Task.class;
	}

	@Override
	public Task findOne(Long id, QuerySpec querySpec) {
		for (Task task : tasks) {
			if (task.getId().equals(id)) {
				return task;
			}
		}
		return null;
	}

	@Override
	public ResourceList<Task> findAll(QuerySpec querySpec) {
		return querySpec.apply(tasks);
	}

	@Override
	public ResourceList<Task> findAll(Iterable<Long> ids, QuerySpec querySpec) {
		return querySpec.apply(tasks);
	}

	@Override
	public <S extends Task> S save(S entity) {
		tasks.add(entity);
		return null;
	}

	@Override
	public void delete(Long id) {
		Iterator<Task> iterator = tasks.iterator();
		while (iterator.hasNext()) {
			Task next = iterator.next();
			if (next.getId().equals(id)) {
				iterator.remove();
			}
		}
	}

	@Override
	public <S extends Task> S create(S entity) {
		return save(entity);
	}
}