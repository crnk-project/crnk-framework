package io.crnk.example.springboot.microservice.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.resource.links.DefaultSelfLinksInformation;
import io.crnk.example.springboot.microservice.project.Project;

@JsonApiResource(type = "task")
public class Task {

	@JsonApiId
	private Long id;

	@JsonProperty
	private String name;

	@JsonApiRelationId
	private Long projectId;

	@JsonApiRelation(serialize = SerializeType.EAGER, lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private ProjectProxy project;

	@JsonApiLinksInformation
	private DefaultSelfLinksInformation links = new DefaultSelfLinksInformation();

	public Task() {
	}

	public Task(Long id, String name, Long projectId) {
		this.id = id;
		this.name = name;
		this.projectId = projectId;
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

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public ProjectProxy getProject() {
		return project;
	}

	public void setProject(ProjectProxy project) {
		this.project = project;
	}

	public DefaultSelfLinksInformation getLinks() {
		return links;
	}

	public void setLinks(DefaultSelfLinksInformation links) {
		this.links = links;
	}
}
