package io.crnk.activiti;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.crnk.activiti.resource.FormResource;
import io.crnk.activiti.resource.TaskResource;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;

public class TaskRepositoryConfig {

	private final Class<? extends TaskResource> taskClass;

	private Class<? extends FormResource> formClass;

	private List<FilterSpec> baseFilters = new ArrayList<>();

	public TaskRepositoryConfig(Class<? extends TaskResource> taskClass) {
		this.taskClass = taskClass;
	}

	public void setForm(Class<? extends FormResource> formClass) {
		this.formClass = formClass;
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
