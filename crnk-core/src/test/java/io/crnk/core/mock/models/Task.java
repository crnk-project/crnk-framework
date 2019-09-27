package io.crnk.core.mock.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.crnk.core.resource.ResourceTypeHolder;
import io.crnk.core.resource.annotations.JsonApiField;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiMetaInformation;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

@JsonApiResource(type = "tasks")
@JsonPropertyOrder(alphabetic = true)
public class Task implements ResourceTypeHolder {

	@JsonApiId
	private Long id;

	private String name;

	private String category;

	private boolean completed;

	private boolean deleted;

	@JsonIgnore
	private boolean ignoredField;

	@JsonApiRelation(serialize = SerializeType.EAGER, lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private Project project;

	@JsonApiRelation
	private List<Project> projectsInit = Collections.emptyList();

	@JsonApiRelation(serialize = SerializeType.ONLY_ID)
	private List<Project> projects = new ArrayList<>();

	@JsonApiRelation
	private Project includedProject;

	@JsonApiRelation
	private List<Project> includedProjects = new ArrayList<>();

	@JsonApiMetaInformation
	private MetaInformation metaInformation;

	@JsonApiLinksInformation
	private LinksInformation linksInformation;

	private List<Task> otherTasks;

	@JsonApiField(patchable = false, postable = false, deletable = false)
	private String status;

	@JsonApiRelationId
	private Long statusThingId;

	@JsonApiField(patchable = false, postable = false, deletable = false)
	@JsonApiRelation(serialize = SerializeType.ONLY_ID, repositoryBehavior = RelationshipRepositoryBehavior.FORWARD_OWNER)
	private Thing statusThing;

	private String readOnlyValue = "someReadOnlyValue";

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private String writeOnlyValue;

	private String type;

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public Long getStatusThingId() {
		return statusThingId;
	}

	public void setStatusThingId(Long statusThingId) {
		this.statusThingId = statusThingId;
	}

	public Thing getStatusThing() {
		return statusThing;
	}

	public void setStatusThing(Thing statusThing) {
		this.statusThing = statusThing;
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

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type = type;
	}
}
