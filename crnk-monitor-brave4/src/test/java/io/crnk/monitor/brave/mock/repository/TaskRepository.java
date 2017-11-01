package io.crnk.monitor.brave.mock.repository;

import io.crnk.monitor.brave.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class TaskRepository extends ResourceRepositoryBase<Task, Long> {

	private static final AtomicLong ID_GENERATOR = new AtomicLong(124);

	private static Map<Long, Task> resources = new HashMap<>();

	public TaskRepository() {
		super(Task.class);
	}

	public static void clear() {
		resources.clear();
	}

	@Override
	public synchronized void delete(Long id) {
		resources.remove(id);
	}

	@Override
	public synchronized <S extends Task> S save(S task) {
		if (task.getName() == null) {
			throw new IllegalStateException("no name available");
		}
		if (task.getId() == null) {
			task.setId(ID_GENERATOR.getAndIncrement());
		}
		resources.put(task.getId(), task);
		return task;
	}

	@Override
	public synchronized ResourceList<Task> findAll(QuerySpec querySpec) {
		return querySpec.apply(resources.values());
	}
}
