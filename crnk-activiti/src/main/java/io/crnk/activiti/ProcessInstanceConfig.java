package io.crnk.activiti;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.crnk.activiti.resource.HistoricProcessInstanceResource;
import io.crnk.activiti.resource.ProcessInstanceResource;
import io.crnk.activiti.resource.TaskResource;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;

public class ProcessInstanceConfig {


	private List<FilterSpec> baseFilters = new ArrayList<>();

	private final Class<? extends ProcessInstanceResource> processInstanceClass;

	private final Map<String, TaskRelationshipConfig> taskRelationships = new HashMap<>();

	private Class<? extends HistoricProcessInstanceResource> historyClass;

	public ProcessInstanceConfig(Class<? extends ProcessInstanceResource> processInstanceClass) {
		this.processInstanceClass = processInstanceClass;
	}

	public void historic(Class<? extends HistoricProcessInstanceResource> historyClass) {
		this.historyClass = historyClass;
	}

	protected Class<? extends HistoricProcessInstanceResource> getHistoryClass() {
		return historyClass;
	}

	public void addTaskRelationship(String relationshipName, Class<? extends TaskResource> taskClass, String taskDefinitionKey) {
		taskRelationships.put(relationshipName, new TaskRelationshipConfig(relationshipName, taskClass, taskDefinitionKey));
	}

	protected Class<? extends ProcessInstanceResource> getProcessInstanceClass() {
		return processInstanceClass;
	}

	protected Map<String, TaskRelationshipConfig> getTaskRelationships() {
		return taskRelationships;
	}

	public void filterByProcessDefinitionKey(String testProcessDefinition) {
		filterBy("processDefinitionKey", testProcessDefinition);
	}

	protected List<FilterSpec> getBaseFilters() {
		return baseFilters;
	}

	public void filterBy(String attribute, String value) {
		baseFilters.add(new FilterSpec(Arrays.asList(attribute), FilterOperator.EQ, value));
	}


	protected static class TaskRelationshipConfig {

		private final Class<? extends TaskResource> taskClass;

		private final String taskDefinitionKey;

		private final String relationshipName;

		public TaskRelationshipConfig(String relationshipName, Class<? extends TaskResource> taskClass, String
				taskDefinitionKey) {
			this.relationshipName = relationshipName;
			this.taskClass = taskClass;
			this.taskDefinitionKey = taskDefinitionKey;
		}

		public String getRelationshipName() {
			return relationshipName;
		}

		public String getTaskDefinitionKey() {
			return taskDefinitionKey;
		}

		public Class<? extends TaskResource> getTaskClass() {
			return taskClass;
		}
	}
}
