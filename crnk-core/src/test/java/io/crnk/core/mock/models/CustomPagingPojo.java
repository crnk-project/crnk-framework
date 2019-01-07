package io.crnk.core.mock.models;

import io.crnk.core.queryspec.pagingspec.NumberSizePagingSpec;
import io.crnk.core.resource.annotations.*;

@JsonApiResource(type = "custom-paging", pagingSpec = NumberSizePagingSpec.class)
public class CustomPagingPojo {

	@JsonApiId
	private Long id;

	private String name;

	private String description;

	@JsonApiToOne
	@JsonApiRelation(serialize = SerializeType.LAZY)
	private Task task;

	public Long getId() {
		return id;
	}

	public CustomPagingPojo setId(Long id) {
		this.id = id;
		return this;
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

	public void setTask(Task task) {
		this.task = task;
	}

	public Task getTask() {
		return task;
	}
}
