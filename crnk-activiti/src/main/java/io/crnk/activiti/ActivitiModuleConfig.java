package io.crnk.activiti;

import io.crnk.activiti.mapper.DateTimeMapper;
import io.crnk.activiti.mapper.DefaultDateTimeMapper;
import io.crnk.activiti.resource.ProcessInstanceResource;
import io.crnk.activiti.resource.TaskResource;

import java.util.HashMap;
import java.util.Map;

public class ActivitiModuleConfig {

	private DateTimeMapper dateTimeMapper = new DefaultDateTimeMapper();

	private Map<Class<? extends ProcessInstanceResource>, ProcessInstanceConfig> processInstances = new HashMap<>();

	private Map<Class<? extends TaskResource>, TaskRepositoryConfig> tasks = new HashMap<>();

	public TaskRepositoryConfig addTask(Class<? extends TaskResource> taskClass) {
		TaskRepositoryConfig taskConfig = new TaskRepositoryConfig(taskClass);
		tasks.put(taskClass, taskConfig);
		return taskConfig;
	}


	public ProcessInstanceConfig addProcessInstance(Class<? extends ProcessInstanceResource> processInstanceClass) {
		ProcessInstanceConfig processInstanceConfig = new ProcessInstanceConfig(processInstanceClass);
		processInstances.put(processInstanceClass, processInstanceConfig);
		return processInstanceConfig;
	}

	protected Map<Class<? extends ProcessInstanceResource>, ProcessInstanceConfig> getProcessInstances() {
		return processInstances;
	}

	protected Map<Class<? extends TaskResource>, TaskRepositoryConfig> getTasks() {
		return tasks;
	}

	protected DateTimeMapper getDateTimeMapper() {
		return dateTimeMapper;
	}

	public void setDateTimeMapper(DateTimeMapper dateTimeMapper) {
		this.dateTimeMapper = dateTimeMapper;
	}
}
