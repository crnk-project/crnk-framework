package io.crnk.activiti.internal.repository;


import java.util.Date;
import java.util.List;
import java.util.Map;

import io.crnk.activiti.mapper.ActivitiResourceMapper;
import io.crnk.activiti.resource.ProcessInstanceResource;
import io.crnk.core.engine.internal.utils.CompareUtils;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.query.Query;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessInstanceResourceRepository<T extends ProcessInstanceResource> extends ActivitiRepositoryBase<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessInstanceResourceRepository.class);

	private final RuntimeService runtimeService;

	private final HistoryService historyService;

	public ProcessInstanceResourceRepository(RuntimeService runtimeService, HistoryService historyService,
			ActivitiResourceMapper resourceMapper, Class<T>
			resourceClass, List<FilterSpec> baseFilters) {
		super(resourceMapper, resourceClass, baseFilters);
		this.historyService = historyService;
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

		ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId());
		processInstanceQuery.includeProcessVariables();
		ProcessInstance updatedProcessInstance = processInstanceQuery.singleResult();
		if (updatedProcessInstance != null) {
			return (S) mapResult(updatedProcessInstance);
		}

		// process already terminated, unfortunately no shared interfaces
		HistoricProcessInstanceQuery historicQuery = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId());
		historicQuery.includeProcessVariables();
		HistoricProcessInstance historicProcessInstance = historicQuery.singleResult();
		PreconditionUtil.verify(historicProcessInstance != null, "could not found potentially terminated process instance with id %s", processInstance.getId());
		ProcessInstance finishedProcessInstance = new MappedHistoricProcessInstance(historicProcessInstance);
		S result = (S) mapResult(finishedProcessInstance);
		result.setName(resource.getName());
		result.setBusinessKey(resource.getBusinessKey());
		return result;
	}

	private <S extends T> void applyUpdates(ProcessInstance processInstance, S resource) {
		String name = resource.getName();
		try {
			if (!CompareUtils.isEquals(processInstance.getName(), name)) {
				runtimeService.setProcessInstanceName(processInstance.getId(), name);
			}

			if (!CompareUtils.isEquals(processInstance.getBusinessKey(), resource.getBusinessKey())) {
				runtimeService.updateBusinessKey(processInstance.getId(), resource.getBusinessKey());
			}
		}
		catch (ActivitiObjectNotFoundException e) {
			throw new BadRequestException("process already ended, cannot set name and business keys");
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


	public static class MappedHistoricProcessInstance implements ProcessInstance {

		private final HistoricProcessInstance historicProcessInstance;

		public MappedHistoricProcessInstance(HistoricProcessInstance historicProcessInstance) {
			this.historicProcessInstance = historicProcessInstance;
		}

		@Override
		public String getProcessDefinitionId() {
			return historicProcessInstance.getProcessDefinitionId();
		}

		@Override
		public String getProcessDefinitionName() {
			return historicProcessInstance.getProcessDefinitionName();
		}

		@Override
		public String getProcessDefinitionKey() {
			return historicProcessInstance.getProcessDefinitionKey();
		}

		@Override
		public Integer getProcessDefinitionVersion() {
			return historicProcessInstance.getProcessDefinitionVersion();
		}

		@Override
		public String getDeploymentId() {
			return historicProcessInstance.getDeploymentId();
		}

		@Override
		public String getBusinessKey() {
			return historicProcessInstance.getBusinessKey();
		}

		@Override
		public String getId() {
			return historicProcessInstance.getId();
		}

		@Override
		public boolean isSuspended() {
			return false;
		}

		@Override
		public boolean isEnded() {
			return true;
		}

		@Override
		public String getActivityId() {
			return historicProcessInstance.getStartActivityId();
		}

		@Override
		public String getProcessInstanceId() {
			return null;
		}

		@Override
		public String getParentId() {
			return null;
		}

		@Override
		public String getSuperExecutionId() {
			return null;
		}

		@Override
		public String getRootProcessInstanceId() {
			return null;
		}

		@Override
		public Map<String, Object> getProcessVariables() {
			return historicProcessInstance.getProcessVariables();
		}

		@Override
		public String getTenantId() {
			return historicProcessInstance.getTenantId();
		}

		@Override
		public String getName() {
			return historicProcessInstance.getName();
		}

		@Override
		public String getDescription() {
			return historicProcessInstance.getDescription();
		}

		@Override
		public String getLocalizedName() {
			return historicProcessInstance.getName();
		}

		@Override
		public String getLocalizedDescription() {
			return historicProcessInstance.getDescription();
		}

		@Override
		public Date getStartTime() {
			return historicProcessInstance.getStartTime();
		}

		@Override
		public String getStartUserId() {
			return historicProcessInstance.getStartUserId();
		}
	}
}
