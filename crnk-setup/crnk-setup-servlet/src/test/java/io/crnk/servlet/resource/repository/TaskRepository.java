package io.crnk.servlet.resource.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.servlet.resource.model.Task;

public class TaskRepository implements ResourceRepositoryV2<Task, Long> {

	public <S extends Task> S save(S entity) {
		entity.setId(1L);
		return entity;
	}

	public <S extends Task> S create(S entity) {
		entity.setId(1L);
		return entity;
	}

	@Override
	public Class<Task> getResourceClass() {
		return Task.class;
	}

	public Task findOne(Long aLong, QuerySpec querySpec) {
		Task task = new Task(aLong, "Some task");
		return task;
	}

	public ResourceList<Task> findAll(QuerySpec querySpec) {
		return findAll(null, querySpec);
	}

	@Override
	public ResourceList<Task> findAll(Iterable<Long> ids, QuerySpec querySpec) {
		DefaultResourceList list = new DefaultResourceList();
		list.add(new Task(1L, "First task"));
		return list;
	}

	public void delete(Long aLong) {
	}
}

