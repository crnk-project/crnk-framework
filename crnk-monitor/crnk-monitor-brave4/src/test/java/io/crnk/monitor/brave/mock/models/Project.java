package io.crnk.monitor.brave.mock.models;

import java.util.List;
import jakarta.validation.constraints.NotNull;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "projects")
public class Project {

	@JsonApiId
	private Long id;

	@NotNull
	private String name;

	@JsonApiRelation(opposite = "project")
	private List<Task> tasks;

	public Project() {

	}

	public Project(long id, String name) {
		this.id = id;
		this.name = name;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
