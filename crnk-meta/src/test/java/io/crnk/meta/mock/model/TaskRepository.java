package io.crnk.meta.mock.model;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

public class TaskRepository extends ResourceRepositoryBase<Task, Long> {

	public TaskRepository() {
		super(Task.class);
	}

	@Override
	public ResourceList<Task> findAll(QuerySpec querySpec) {
		return null;
	}
}
