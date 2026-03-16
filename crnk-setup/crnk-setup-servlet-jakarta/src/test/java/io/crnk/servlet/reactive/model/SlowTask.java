package io.crnk.servlet.reactive.model;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "slow/task")
public class SlowTask {

	@JsonApiId
	private Long id;

	private String name;


	public Long getId() {
		return id;
	}

	public SlowTask setId(Long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
