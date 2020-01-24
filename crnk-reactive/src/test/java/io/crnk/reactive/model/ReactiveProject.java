package io.crnk.reactive.model;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;

import java.util.List;

@JsonApiResource(type = "reactive/project")
public class ReactiveProject {

	@JsonApiId
	private Long id;

	private String name;

	@JsonApiRelation(opposite = "project")
	private List<ReactiveTask> tasks;

	public Long getId() {
		return id;
	}

	public ReactiveProject setId(Long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ReactiveTask> getTasks() {
		return tasks;
	}

	public void setTasks(List<ReactiveTask> tasks) {
		this.tasks = tasks;
	}
}
