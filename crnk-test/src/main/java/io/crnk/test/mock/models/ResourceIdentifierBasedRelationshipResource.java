package io.crnk.test.mock.models;

import java.util.Collection;
import java.util.List;

import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "resourceIdentifierRelationshipResource")
public class ResourceIdentifierBasedRelationshipResource {

	@JsonApiId
	private long id;

	@JsonApiRelationId
	private ResourceIdentifier taskId;

	@JsonApiRelationId
	private List<ResourceIdentifier> taskIds;

	@JsonApiRelation
	private Task task;

	@JsonApiRelation(idField = "taskIds")
	private Collection<Task> tasks;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public ResourceIdentifier getTaskId() {
		return taskId;
	}

	public void setTaskId(ResourceIdentifier taskId) {
		this.taskId = taskId;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public List<ResourceIdentifier> getTaskIds() {
		return taskIds;
	}

	public void setTaskIds(List<ResourceIdentifier> taskIds) {
		this.taskIds = taskIds;
	}

	public Collection<Task> getTasks() {
		return tasks;
	}

	public void setTasks(Collection<Task> tasks) {
		this.tasks = tasks;
	}
}
