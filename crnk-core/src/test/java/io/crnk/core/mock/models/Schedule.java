package io.crnk.core.mock.models;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonInclude;
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
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String desc;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.NONE)
	@JsonProperty("followup")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private Project followupProject;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<String> keywords;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.NONE)
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<Task> tasks;

	private Optional<OffsetDateTime> dueDate = Optional.empty();

	public Long getId() {
		return id;
	}

	public Schedule setId(Long id) {
		this.id = id;
		return this;
	}

	public Optional<OffsetDateTime> getDueDate() {
		return dueDate;
	}

	public void setDueDate(Optional<OffsetDateTime> dueDate) {
		this.dueDate = dueDate;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
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
