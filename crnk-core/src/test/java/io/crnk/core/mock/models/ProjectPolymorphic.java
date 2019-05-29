package io.crnk.core.mock.models;

import java.util.List;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;


@JsonApiResource(type = "projects-polymorphic")
public class ProjectPolymorphic {

	@JsonApiId
	private Long id;
	@JsonApiRelation
	private Object task;
	@JsonApiRelation
	private List<Object> tasks;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Object getTask() {
		return task;
	}

	public void setTask(Object task) {
		this.task = task;
	}

	public List<Object> getTasks() {
		return tasks;
	}

	public void setTasks(List<Object> tasks) {
		this.tasks = tasks;
	}
}
