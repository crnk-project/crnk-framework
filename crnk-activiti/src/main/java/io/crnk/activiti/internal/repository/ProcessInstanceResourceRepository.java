package io.crnk.activiti.internal.repository;


import java.util.List;
import java.util.Map;

import io.crnk.activiti.mapper.ActivitiResourceMapper;
import io.crnk.activiti.resource.ProcessInstanceResource;
import io.crnk.core.engine.internal.utils.CompareUtils;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.query.Query;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;

public class ProcessInstanceResourceRepository<T extends ProcessInstanceResource> extends ActivitiRepositoryBase<T> {

	private final RuntimeService runtimeService;

	public ProcessInstanceResourceRepository(RuntimeService runtimeService, ActivitiResourceMapper resourceMapper, Class<T>
			resourceClass, List<FilterSpec> baseFilters) {
		super(resourceMapper, resourceClass, baseFilters);
		this.runtimeService = runtimeService;
	}

	@Override
	protected Query<ProcessInstanceQuery, ProcessInstance> createQuery() {
		return runtimeService.createProcessInstanceQuery().includeProcessVariables();
	}

	@Override
	protected T mapResult(Object result) {
		return resourceMapper.mapToResource(getResourceClass(), (ProcessInstance) result);
	}

	@Override
	public <S extends T> S create(S resource) {
		checkFilter(resource, true);

		String processDefinitionKey = resource.getProcessDefinitionKey();
		String businessKey = resource.getBusinessKey();
		String tenantId = resource.getTenantId();
		Map<String, Object> variables = resourceMapper.mapToVariables(resource);

		ProcessInstance processInstance =
				runtimeService.startProcessInstanceByKeyAndTenantId(processDefinitionKey, businessKey, variables, tenantId);

		applyUpdates(processInstance, resource);

		return (S) findOne(processInstance.getId(), new QuerySpec(getResourceClass()));
	}

	private <S extends T> void applyUpdates(ProcessInstance processInstance, S resource) {
		String name = resource.getName();
		if (!CompareUtils.isEquals(processInstance.getName(), name)) {
			runtimeService.setProcessInstanceName(processInstance.getId(), name);
		}

		if (!CompareUtils.isEquals(processInstance.getBusinessKey(), resource.getBusinessKey())) {
			runtimeService.updateBusinessKey(processInstance.getId(), resource.getBusinessKey());
		}
		checkSuspend(processInstance, resource);
	}

	private <S extends T> void checkSuspend(ProcessInstance processInstance, S resource) {
		if (!processInstance.isSuspended() && resource.isSuspended()) {
			runtimeService.suspendProcessInstanceById(processInstance.getId());
		}
	}

	private <S extends T> void checkActivate(ProcessInstance processInstance, S resource) {
		if (processInstance.isSuspended() && !resource.isSuspended()) {
			runtimeService.activateProcessInstanceById(processInstance.getId());
		}
	}

	@Override
	public <S extends T> S save(S resource) {
		checkFilter(resource, true);

		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(resource.getId())
				.includeProcessVariables()
				.singleResult();
		if (processInstance == null) {
			throw new ResourceNotFoundException(resource.getId());
		}
		checkFilter(processInstance, false);
		checkActivate(processInstance, resource);

		Map<String, Object> variables = resourceMapper.mapToVariables(resource);
		runtimeService.setVariables(processInstance.getId(), variables);

		applyUpdates(processInstance, resource);
		return (S) findOne(processInstance.getId(), new QuerySpec(getResourceClass()));
	}


	@Override
	public void delete(String id) {
		T resource = findOne(id, new QuerySpec(getResourceClass()));
		checkFilter(resource, false);
		
		runtimeService.deleteProcessInstance(id, null);
	}
}
