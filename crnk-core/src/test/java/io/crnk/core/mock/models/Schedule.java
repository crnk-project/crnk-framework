package io.crnk.core.mock.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;

@JsonApiResource(type = "schedules")
public class Schedule {

	@JsonApiId
	private Long id;

	private String name;

	@JsonApiRelationId
	private Long projectId;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, serialize = SerializeType.ONLY_ID)
	private Project project;

	@JsonProperty("description")
	private String desc;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.NONE)
	@JsonProperty("followup")
	private Project followupProject;

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

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Project getFollowupProject() {
		return followupProject;
	}

	public void setFollowupProject(Project followupProject) {
		this.followupProject = followupProject;
	}
}
