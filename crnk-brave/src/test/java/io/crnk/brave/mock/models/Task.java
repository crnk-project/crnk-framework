package io.crnk.brave.mock.models;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiToOne;

import javax.validation.constraints.NotNull;

@JsonApiResource("tasks")
public class Task {

	@JsonApiId
	private Long id;

	@NotNull
	private String name;

	@JsonApiToOne(opposite = "tasks")
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
