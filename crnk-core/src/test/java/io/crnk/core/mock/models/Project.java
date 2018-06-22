package io.crnk.core.mock.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiIncludeByDefault;
import io.crnk.core.resource.annotations.JsonApiLookupIncludeAutomatically;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiToMany;
import io.crnk.core.resource.annotations.JsonApiToOne;

@JsonApiResource(type = "projects")
public class Project {

	@JsonApiId
	private Long id;

	private String name;

	private String description;

	private ProjectData data;

	@JsonApiToMany(opposite = "project")
	private List<Task> tasks = new ArrayList<>();

	@JsonApiToOne
	private Task task;

	@JsonApiToOne
	@JsonApiIncludeByDefault
	private ProjectEager projectEager;

	@JsonApiToMany
	@JsonApiIncludeByDefault
	private List<ProjectEager> projectEagerList = new ArrayList<>();

	@JsonApiToOne
	@JsonApiLookupIncludeAutomatically
	private Task includedTask;

	@JsonApiRelation(opposite = "project")
	private Collection<Schedule> schedules;

	public Long getId() {
		return id;
	}

	public Project setId(Long id) {
		this.id = id;
		return this;
	}

	public Collection<Schedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(Collection<Schedule> schedules) {
		this.schedules = schedules;
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

	public void setDescription(@SuppressWarnings("SameParameterValue") String description) {
		this.description = description;
	}

	public ProjectData getData() {
		return data;
	}

	public void setData(ProjectData data) {
		this.data = data;
	}

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasks) {
		this.tasks = tasks;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public ProjectEager getProjectEager() {
		return projectEager;
	}

	public void setProjectEager(ProjectEager projectEager) {
		this.projectEager = projectEager;
	}

	public List<ProjectEager> getProjectEagerList() {
		return projectEagerList;
	}

	public void setProjectEagerList(List<ProjectEager> projectEagerList) {
		this.projectEagerList = projectEagerList;
	}

	public Task getIncludedTask() {
		return includedTask;
	}

	public void setIncludedTask(Task includedTask) {
		this.includedTask = includedTask;
	}
}
