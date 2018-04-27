package io.crnk.test.mock.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiToMany;
import io.crnk.core.resource.annotations.JsonApiToOne;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;

@JsonApiResource(type = "schedules")
public class Schedule {

	@JsonApiId
	private Long id;

	private String name;

	@JsonProperty("description")
	private String desc;

	@JsonApiToOne(lazy = false)
	private Task task;

	@JsonApiToOne(lazy = true)
	private Task lazyTask;

	@JsonApiToMany(opposite = "schedule")
	private Set<Task> tasks = Collections.emptySet();

	@JsonApiToMany(opposite = "schedule")
	private List<Task> tasksList = Collections.emptyList();

	@JsonApiRelationId
	private Long projectId;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, serialize = SerializeType.ONLY_ID)
	private Project project;

	@JsonApiRelationId
	private List<Long> projectIds = new ArrayList<>();

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL, serialize = SerializeType.ONLY_ID)
	private List<Project> projects = new ArrayList<>();


	private boolean delayed;

	public boolean isDelayed() {
		return delayed;
	}

	public void setDelayed(boolean delayed) {
		this.delayed = delayed;
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

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Task getLazyTask() {
		return lazyTask;
	}

	public void setLazyTask(Task lazyTask) {
		this.lazyTask = lazyTask;
	}

	public Set<Task> getTasks() {
		return tasks;
	}

	public void setTasks(Set<Task> tasks) {
		this.tasks = tasks;
	}

	public List<Task> getTasksList() {
		return tasksList;
	}

	public void setTasksList(List<Task> tasksList) {
		this.tasksList = tasksList;
	}

	public Long getProjectId() {
		return projectId;
	}

	public void setProjectId(Long projectId) {
		this.projectId = projectId;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public List<Long> getProjectIds() {
		return projectIds;
	}

	public void setProjectIds(List<Long> projectIds) {
		this.projectIds = projectIds;
		this.projects = null;
	}

	public List<Project> getProjects() {
		return projects;
	}

	public void setProjects(List<Project> projects) {
		this.projects = projects;

		if (projects != null) {
			List<Long> ids = new ArrayList<>();
			for (Project project : projects) {
				ids.add(project.getId());
			}
			this.projectIds = ids;
		}
		else {
			projectIds = null;
		}
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
