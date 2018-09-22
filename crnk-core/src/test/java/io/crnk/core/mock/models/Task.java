package io.crnk.core.mock.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.crnk.core.resource.annotations.JsonApiField;
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

import java.util.Collections;
import java.util.List;

@JsonApiResource(type = "tasks")
@JsonPropertyOrder(alphabetic = true)
public class Task {

	@JsonApiId
	private Long id;

	private String name;

	private String category;

	private boolean completed;

	private boolean deleted;

	@JsonIgnore
	private boolean ignoredField;

	@JsonApiToOne(opposite = "tasks")
	@JsonApiIncludeByDefault
	private Project project;

	@JsonApiToMany
	private List<Project> projectsInit = Collections.emptyList();

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

	@JsonApiField(patchable = false, postable = false)
	private String status;

	private String readOnlyValue = "someReadOnlyValue";

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String writeOnlyValue;

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isIgnoredField() {
		return ignoredField;
	}

	public void setIgnoredField(boolean ignoredField) {
		this.ignoredField = ignoredField;
	}

	public String getReadOnlyValue() {
		return readOnlyValue;
	}

	public String getWriteOnlyValue() {
		return writeOnlyValue;
	}

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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getName() {
		return name;
	}

	public void setName(@SuppressWarnings("SameParameterValue") String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		if (category == null) {
			throw new IllegalArgumentException("Category cannot be set to null!");
		}
		this.category = category;
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

	public List<Project> getProjectsInit() {
		return projectsInit;
	}

	public void setProjectsInit(List<Project> projectsInit) {
		this.projectsInit = projectsInit;
	}
}
