package io.crnk.monitor.brave.mock.models;

import jakarta.validation.constraints.NotNull;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "tasks")
public class Task {

	@JsonApiId
	private Long id;

	@NotNull
	private String name;

	@JsonApiRelation(opposite = "tasks")
	private Project project;

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

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
}
