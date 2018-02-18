package io.crnk.activiti.internal.repository;


import java.util.HashMap;
import java.util.Map;

import io.crnk.activiti.mapper.ActivitiResourceMapper;
import io.crnk.activiti.resource.FormResource;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.FormService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.TaskFormData;


/**
 * Gives access to a form of a task. PATCH will updated the form. POST will update the form and close the task (i.e. assumes
 * the work is done, not a perfect match to REST, but close enough).
 */
public class FormResourceRepository<F extends FormResource> extends ResourceRepositoryBase<F, String> {


	private final FormService formService;

	private final ActivitiResourceMapper resourceMapper;

	private final TaskService taskService;

	@Deprecated
	public FormResourceRepository(FormService formService, ActivitiResourceMapper resourceMapper,
			Class<F> formClass) {
		this(formService, null, resourceMapper, formClass);
	}

	public FormResourceRepository(FormService formService, TaskService taskService, ActivitiResourceMapper resourceMapper,
			Class<F> formClass) {
		super(formClass);
		this.formService = formService;
		this.resourceMapper = resourceMapper;
		this.taskService = taskService;
	}

	@Override
	public F findOne(String id, QuerySpec querySpec) {
		try {
			TaskFormData taskFormData = formService.getTaskFormData(id);

			FormResource formResource = resourceMapper.mapToResource(getResourceClass(), taskFormData);
			return (F) formResource;
		}
		catch (ActivitiObjectNotFoundException e) {
			// happens after completion when task is returned one last time.
			// FIXME better solution since still log out
			return null;
		}
	}


	@Override
	public <S extends F> S save(S resource) {
		Map<String, Object> objectProperties = resourceMapper.mapToVariables(resource);
		Map<String, String> properties = new HashMap<>();
		for (Map.Entry<String, Object> entry : objectProperties.entrySet()) {
			properties.put(entry.getKey(), entry.getValue().toString());
		}
		formService.saveFormData(resource.getId(), properties);

		return (S) findOne(resource.getId(), null);
	}

	@Override
	public <S extends F> S create(S resource) {
		String taskId = resource.getId();
		S form = save(resource);
		taskService.complete(taskId);
		return form;
	}

	@Override
	public ResourceList<F> findAll(QuerySpec querySpec) {
		throw new UnsupportedOperationException();
	}
}
