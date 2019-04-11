package io.crnk.activiti.example.approval;

import java.io.Serializable;
import java.util.Map;

import io.crnk.activiti.example.model.ScheduleApprovalProcessInstance;
import io.crnk.activiti.mapper.ActivitiResourceMapper;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.test.mock.models.Schedule;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApprovalManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(ApprovalManager.class);

	private RuntimeService runtimeService;

	private TaskService taskService;

	private ApprovalMapper approvalMapper;

	private ActivitiResourceMapper resourceMapper;

	private ModuleRegistry moduleRegistry;

	/**
	 * We do not enforce approval within approval threads.
	 */
	private ThreadLocal<Boolean> approverThread = new ThreadLocal<>();


	public void init(RuntimeService runtimeService, TaskService taskService, ActivitiResourceMapper resourceMapper,
					 ApprovalMapper mapper,
					 ModuleRegistry moduleRegistry) {
		this.runtimeService = runtimeService;
		this.taskService = taskService;
		this.resourceMapper = resourceMapper;
		this.approvalMapper = mapper;
		this.moduleRegistry = moduleRegistry;
	}


	public <T> T requestApproval(T entity, HttpMethod method) {
		RegistryEntry entry = moduleRegistry.getResourceRegistry().getEntry(entity.getClass());
		ResourceInformation resourceInformation = entry.getResourceInformation();
		Object id = resourceInformation.getId(entity);

		ApprovalProcessInstance resource = newApprovalProcessInstance(entity.getClass());
		resource.setResourceType(resourceInformation.getResourceType());
		resource.setResourceId(id.toString());
		resource.setNewValues(approvalMapper.mapValues(entity));

		T currentEntity = entity;
		if (method != HttpMethod.POST) {
			// you may need  additional logic here, like detaching entities from an entity manager
			// in case of entities, like detaching from entity manager

			currentEntity = (T) get(entry, id.toString());
			PreconditionUtil.assertFalse("posted resource must not be managed", currentEntity == entity);
			if (currentEntity != null) {
				resource.setPreviousValues(approvalMapper.mapValues(currentEntity));
			}
		}

		Map<String, Object> processVariables = resourceMapper.mapToVariables(resource);
		runtimeService.startProcessInstanceByKey("scheduleChange", processVariables);
		return currentEntity;
	}

	protected ApprovalProcessInstance newApprovalProcessInstance(Class<?> clazz) {
		ScheduleApprovalProcessInstance instance = new ScheduleApprovalProcessInstance();
		instance.setStatus(ScheduleApprovalProcessInstance.ScheduleStatus.SHIPPED);
		return instance;
	}


	/**
	 * Called by Activiti to complete workflow.
	 */
	public void approveOrRejectDoc(boolean approved, String comment, String taskId) {
		LOGGER.debug("Approve Task completion?: " + approved);

		Task task = taskService.createTaskQuery().taskId(taskId).includeProcessVariables().singleResult();
		taskService.setAssignee(task.getId(), "john.doe");
		taskService.addComment(task.getId(), task.getProcessInstanceId(), comment);
		taskService.setVariableLocal(task.getId(), "outcome", approved ? "Approved" : "Rejected");
		taskService.complete(task.getId());
	}

	public void approved(Execution execution) {
		try {
			approverThread.set(true);

			Map<String, Object> variables = runtimeService.getVariables(execution.getId());

			String resourceType = (String) variables.get("resourceType");
			RegistryEntry registryEntry = moduleRegistry.getResourceRegistry().getEntry(resourceType);
			ResourceInformation resourceInformation = registryEntry.getResourceInformation();

			// fetch resource and changes
			ApprovalProcessInstance processResource = newApprovalProcessInstance(resourceInformation.getResourceClass());
			resourceMapper.mapFromVariables(processResource, variables);
			Object resource = get(registryEntry, processResource.getResourceId());

			// apply and save changes
			approvalMapper.unmapValues(processResource.getNewValues(), resource);
			save(registryEntry, resource);

			LOGGER.debug("approval accepted: " + execution.getProcessInstanceId());
		} finally {
			approverThread.remove();
		}
	}

	private void save(RegistryEntry entry, Object resource) {
		ResourceRepository resourceRepository = entry.getResourceRepositoryFacade();
		resourceRepository.save(resource);
	}

	private Object get(RegistryEntry entry, String idString) {
		ResourceInformation resourceInformation = entry.getResourceInformation();
		Object id = resourceInformation.parseIdString(idString);

		ResourceRepository resourceRepository = entry.getResourceRepositoryFacade();
		QuerySpec querySpec = new QuerySpec(resourceInformation.getResourceType());
		return resourceRepository.findOne((Serializable) id, querySpec);
	}

	public void denied(Execution execution) {
		LOGGER.debug("approval denied: " + execution.getProcessInstanceId());
	}

	public boolean needsApproval(Object entity, HttpMethod method) {
		Boolean isApproverThread = approverThread.get();
		return entity instanceof Schedule && method != HttpMethod.POST && isApproverThread != Boolean.TRUE;
	}
}
