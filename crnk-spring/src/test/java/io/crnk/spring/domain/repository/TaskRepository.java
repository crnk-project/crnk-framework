package io.crnk.spring.domain.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.spring.domain.model.Task;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TaskRepository extends ResourceRepositoryBase<Task, String> {

	private Map<Long, Task> tasks = new HashMap<>();

	public TaskRepository() {
		super(Task.class);
		save(new Task(1L, "John"));
	}

	@Override
	public synchronized void delete(String id) {
		tasks.remove(id);
	}

	@Override
	public synchronized <S extends Task> S save(S task) {
		tasks.put(task.getId(), task);
		return task;
	}

	@Override
	public synchronized ResourceList<Task> findAll(QuerySpec querySpec) {
		DefaultResourceList<Task> list = querySpec.apply(tasks.values());
		list.setMeta(new MetaInformation() {

			public String name = "meta information";
		});
		return list;
	}
}
