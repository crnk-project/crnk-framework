package io.crnk.core.queryspec.repository;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.crnk.core.mock.models.Project;
import io.crnk.core.queryspec.pagingspec.CustomOffsetLimitPagingBehavior;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;

@JsonApiResource(type = "task-with-paging", pagingBehavior = CustomOffsetLimitPagingBehavior.class)
@JsonPropertyOrder(alphabetic = true)
public class TaskWithPagingBehavior {

	@JsonApiId
	private String id;

	private String name;

	@JsonApiRelation(opposite = "tasks", lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private Project project;

	public String getId() {
		return id;
	}

	public TaskWithPagingBehavior setId(String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(@SuppressWarnings("SameParameterValue") String name) {
		this.name = name;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
}
