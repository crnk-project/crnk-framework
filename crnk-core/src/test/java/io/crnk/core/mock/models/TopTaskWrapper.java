package io.crnk.core.mock.models;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiRelationId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;

@JsonApiResource(type = "topTaskWrapper")
public class TopTaskWrapper {

	@JsonApiId
	private Long id;

	@JsonApiRelationId
	private Long taskId;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private TopTask task;

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(final Long taskId) {
		this.taskId = taskId;
		this.task = null;
	}

	public TopTask getTask() {
		return task;
	}

	public void setTask(final TopTask task) {
		this.task = task;
		this.taskId = task != null ? task.getId() : null;
	}

	public Long getId() {
		return id;
	}

	public void setId(final Long id) {
		this.id = id;
	}
}
