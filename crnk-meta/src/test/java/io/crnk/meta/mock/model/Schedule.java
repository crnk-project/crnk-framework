package io.crnk.meta.mock.model;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiToMany;
import io.crnk.core.resource.annotations.JsonApiToOne;

import java.util.List;

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
	private List<Task> tasks;

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

	public List<Task> getTasks() {
		return tasks;
	}

	public void setTasks(List<Task> tasksList) {
		this.tasks = tasksList;
	}

}
