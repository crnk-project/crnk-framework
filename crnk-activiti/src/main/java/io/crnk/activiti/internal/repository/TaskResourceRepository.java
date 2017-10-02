package io.crnk.activiti.internal.repository;


import java.util.List;

import io.crnk.activiti.mapper.ActivitiResourceMapper;
import io.crnk.activiti.resource.TaskResource;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;

public class TaskResourceRepository<T extends TaskResource> extends ActivitiRepositoryBase<T> {

	private final TaskService taskService;

	public TaskResourceRepository(TaskService taskService, ActivitiResourceMapper resourceMapper, Class<T> resourceClass,
			List<FilterSpec> baseFilters) {
		super(resourceMapper, resourceClass, baseFilters);
		this.taskService = taskService;
	}

	@Override
	protected TaskQuery createQuery() {
		return taskService.createTaskQuery().includeTaskLocalVariables();
	}

	@Override
	protected T mapResult(Object result) {
		return resourceMapper.mapToResource(getResourceClass(), (Task) result);
	}

	@Override
	public <S extends T> S create(S resource) {
		Task task = taskService.newTask();
		return doSave(task, resource);
	}

	@Override
	public <S extends T> S save(S resource) {
		Task task = taskService.createTaskQuery().taskId(resource.getId()).singleResult();
		checkFilter(task, false);
		return doSave(task, resource);
	}

	private <S extends T> S doSave(Task task, S resource) {
		checkFilter(resource, true);
		resourceMapper.mapFromResource(resource, task);

		taskService.saveTask(task);
		if (resource.isCompleted()) {
			taskService.complete(resource.getId());
		}

		S savedResource = (S) resourceMapper.mapToResource(getResourceClass(), task);
		savedResource.setCompleted(resource.isCompleted());
		return savedResource;
	}


	@Override
	public void delete(String id) {
		T resource = findOne(id, new QuerySpec(getResourceClass()));
		checkFilter(resource, false);

		taskService.deleteTask(id);
	}
}
