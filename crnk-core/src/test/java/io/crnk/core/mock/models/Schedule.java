package io.crnk.core.mock.models;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;

@JsonApiResource(type = "schedules")
public class Schedule {

	@JsonApiId
	private Long id;

	private String name;

	@JsonApiRelationId
	private Long projectId;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private Project project;

	public Long getId() {
		return id;
	}

	public Schedule setId(Long id) {
		this.id = id;
		return this;
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
		this.project = null;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.projectId = project != null ? project.getId() : null;
		this.project = project;
	}
}
