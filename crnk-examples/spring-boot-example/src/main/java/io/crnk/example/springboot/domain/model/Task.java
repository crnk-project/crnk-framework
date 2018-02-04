package io.crnk.example.springboot.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import javax.validation.constraints.Size;

// tag::doc1[]
@JsonApiResource(type = "tasks")
public class Task {

	@JsonApiId
	private Long id;

	@JsonProperty("name")
	private String name;

	@Size(max = 20, message = "Description may not exceed {max} characters.")
	private String description;

	@JsonApiRelationId
	private Long projectId;

	@JsonApiRelation(opposite = "tasks", lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private Project project;

	// end::doc1[]

	public Task() {
	}

	public Task(Long id, String name) {
		this.id = id;
		this.name = name;
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

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
		this.projectId = project != null ? project.getId() : null;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
		this.project = null;
	}
	// tag::doc2[]
}
// end::doc2[]