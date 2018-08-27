package io.crnk.validation.mock.models;

import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiIncludeByDefault;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiLookupIncludeAutomatically;
import io.crnk.core.resource.annotations.JsonApiMetaInformation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiToMany;
import io.crnk.core.resource.annotations.JsonApiToOne;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.validation.mock.ComplexValid;

@JsonApiResource(type = "tasks")
@ComplexValid
public class Task {

	@JsonApiId
	private Long id;

	@NotNull
	private String name;

	@JsonApiToOne
	@JsonApiIncludeByDefault
	private Project project;

	@JsonApiToMany(lazy = false)
	private List<Project> projects = Collections.emptyList();

	@JsonApiToOne
	@JsonApiLookupIncludeAutomatically
	private Project includedProject;

	@JsonApiToMany
	@JsonApiLookupIncludeAutomatically
	private List<Project> includedProjects;

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

	public void setProject(Project project) {
		this.project = project;
	}

	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;
	}

	public Project getIncludedProject() {
		return includedProject;
	}

	public void setIncludedProject(Project includedProject) {
		this.includedProject = includedProject;
	}

	public List<Project> getIncludedProjects() {
		return includedProjects;
	}

	public void setIncludedProjects(List<Project> includedProjects) {
		this.includedProjects = includedProjects;
	}

	public MetaInformation getMetaInformation() {
		return metaInformation;
	}

	public Task setMetaInformation(MetaInformation metaInformation) {
		this.metaInformation = metaInformation;
		return this;
	}

	public LinksInformation getLinksInformation() {
		return linksInformation;
	}

	public Task setLinksInformation(LinksInformation linksInformation) {
		this.linksInformation = linksInformation;
		return this;
	}
}
