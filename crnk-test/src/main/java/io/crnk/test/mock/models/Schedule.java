package io.crnk.test.mock.models;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiToMany;
import io.crnk.core.resource.annotations.JsonApiToOne;

@JsonApiResource(type = "schedules")
public class Schedule {

	@JsonApiId
	private Long id;

	private String name;

	@JsonApiToOne(lazy = false)
	private Task task;

	@JsonApiToOne(lazy = true)
	private Task lazyTask;

	@JsonApiToMany(opposite = "schedule")
	private Set<Task> tasks = Collections.emptySet();

	@JsonApiToMany(opposite = "schedule")
	private List<Task> tasksList = Collections.emptyList();

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

}
