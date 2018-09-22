package io.crnk.test.mock.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.TaskSubType;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskSubtypeRepository extends ResourceRepositoryBase<TaskSubType, Long> {

	private TaskRepository repo = new TaskRepository();

	public TaskSubtypeRepository() {
		super(TaskSubType.class);
	}


	@Override
	public ResourceList<TaskSubType> findAll(QuerySpec querySpec) {
		return querySpec.apply(repo.findAll(querySpec).stream().filter(it -> it instanceof TaskSubType).map(it -> (TaskSubType) it).collect(Collectors.toList()));
	}

	@Override
	public <S extends TaskSubType> S save(S entity) {
		repo.save(entity);
		return null;
	}

	@Override
	public void delete(Long id) {
		repo.delete(id);
	}
}