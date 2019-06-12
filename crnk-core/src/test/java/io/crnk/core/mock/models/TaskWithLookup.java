package io.crnk.core.mock.models;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;

@JsonApiResource(type = "task-with-lookup")
public class TaskWithLookup {

	@JsonApiId
	private String id;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private Project project;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private Project projectNull;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
	private Project projectOverridden;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
	private Project projectOverriddenNull;

	public String getId() {
		return id;
	}

	public TaskWithLookup setId(String id) {
		this.id = id;
		return this;
	}

	public Project getProject() {
		return project;
	}

	public TaskWithLookup setProject(Project project) {
		this.project = project;
		return this;
	}

	public Project getProjectNull() {
		return projectNull;
	}

	public TaskWithLookup setProjectNull(Project projectNull) {
		this.projectNull = projectNull;
		return this;
	}

	public Project getProjectOverridden() {
		return projectOverridden;
	}

	public TaskWithLookup setProjectOverridden(Project projectOverridden) {
		this.projectOverridden = projectOverridden;
		return this;
	}

	public Project getProjectOverriddenNull() {
		return projectOverriddenNull;
	}

	public TaskWithLookup setProjectOverriddenNull(Project projectOverriddenNull) {
		this.projectOverriddenNull = projectOverriddenNull;
		return this;
	}
}
