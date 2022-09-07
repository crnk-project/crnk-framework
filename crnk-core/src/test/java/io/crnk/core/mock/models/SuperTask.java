package io.crnk.core.mock.models;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "superTasks", subTypes = SpecialTask.class)
public abstract class SuperTask {

	@JsonApiId
	private Long id;

	private String name;

	private String category;

	public Long getId() {
		return id;
	}

	public SuperTask setId(Long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(@SuppressWarnings("SameParameterValue") String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(final String category) {
		this.category = category;
	}
}
