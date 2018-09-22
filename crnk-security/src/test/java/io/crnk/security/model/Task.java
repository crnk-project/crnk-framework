package io.crnk.security.model;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiIncludeByDefault;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiMetaInformation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiToOne;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

import javax.validation.constraints.NotNull;
import java.util.List;

@JsonApiResource(type = "tasks")
public class Task {

	@JsonApiId
	private Long id;

	@NotNull
	private String name;

	@JsonApiToOne(opposite = "tasks")
	@JsonApiIncludeByDefault
	private Project project;

	@JsonApiMetaInformation
	private MetaInformation metaInformation;

	@JsonApiLinksInformation
	private LinksInformation linksInformation;

	private List<Task> otherTasks;

	public List<Task> getOtherTasks() {
		return otherTasks;
	}

	public Task setOtherTasks(List<Task> otherTasks) {
		this.otherTasks = otherTasks;
		return this;
	}

	public Long getId() {
		return id;
	}

	public Task setId(Long id) {
		this.id = id;
		return this;
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

	public void setProject(Project newProject) {
		// update bidirectional association
		if (project != newProject) {
			if (project != null) {
				project.getTasks().remove(this);
			}
			if (newProject != null) {
				newProject.getTasks().add(this);
			}
			project = newProject;
		}
	}

	public MetaInformation getMetaInformation() {
		return metaInformation;
	}

	public void setMetaInformation(MetaInformation metaInformation) {
		this.metaInformation = metaInformation;
	}

	public LinksInformation getLinksInformation() {
		return linksInformation;
	}

	public void setLinksInformation(LinksInformation linksInformation) {
		this.linksInformation = linksInformation;
	}
}
