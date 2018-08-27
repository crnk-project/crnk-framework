package io.crnk.validation.mock.models;

import javax.validation.constraints.NotNull;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiIncludeByDefault;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiToOne;
import io.crnk.validation.mock.ComplexValid;

@JsonApiResource(type = "schedules")
@ComplexValid
public class Schedule {

	@JsonApiId
	private Long id;

	@NotNull
	private String name;

	@NotNull
	@JsonApiRelationId
	private Long projectId;

	@JsonApiToOne
	@JsonApiIncludeByDefault
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

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
}
