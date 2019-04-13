package io.crnk.data.activiti;

import io.crnk.data.activiti.resource.FormResource;
import io.crnk.data.activiti.resource.HistoricTaskResource;
import io.crnk.data.activiti.resource.TaskResource;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskRepositoryConfig {

	private final Class<? extends TaskResource> taskClass;

	private Class<? extends FormResource> formClass;

	private List<FilterSpec> baseFilters = new ArrayList<>();

	private Class<? extends HistoricTaskResource> historyClass;

	public TaskRepositoryConfig(Class<? extends TaskResource> taskClass) {
		this.taskClass = taskClass;
	}

	public TaskRepositoryConfig historic(Class<? extends HistoricTaskResource> historyClass) {
		this.historyClass = historyClass;
		return this;
	}

	public void setForm(Class<? extends FormResource> formClass) {
		this.formClass = formClass;
	}

	protected Class<? extends HistoricTaskResource> getHistoryClass() {
		return historyClass;
	}

	protected Class<? extends TaskResource> getTaskClass() {
		return taskClass;
	}

	protected Class<? extends FormResource> getFormClass() {
		return formClass;
	}

	public void filterByTaskDefinitionKey(String name) {
		filterBy("taskDefinitionKey", name);
	}

	protected List<FilterSpec> getBaseFilters() {
		return baseFilters;
	}

	public void filterBy(String attribute, String value) {
		baseFilters.add(new FilterSpec(Arrays.asList(attribute), FilterOperator.EQ, value));
	}
}
