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
import org.activiti.engine.form.TaskFormData;


public class FormResourceRepository<F extends FormResource> extends ResourceRepositoryBase<F,
		String> {


	private final FormService formService;

	private final ActivitiResourceMapper resourceMapper;

	public FormResourceRepository(FormService formatService, ActivitiResourceMapper resourceMapper, Class<F> formClass) {
		super(formClass);
		this.formService = formatService;
		this.resourceMapper = resourceMapper;
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
		// FIXME support form-based process creation
		throw new UnsupportedOperationException();
	}

	@Override
	public ResourceList<F> findAll(QuerySpec querySpec) {
		throw new UnsupportedOperationException();
	}
}
