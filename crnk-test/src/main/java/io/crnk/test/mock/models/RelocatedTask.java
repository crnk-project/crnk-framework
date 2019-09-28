package io.crnk.test.mock.models;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;

/**
 * {@link io.crnk.test.mock.TestNamingStrategy} will change path of this resource.
 */
@JsonApiResource(type = "relocatedTask", resourcePath = "taskOldPath")
public class RelocatedTask {

	@JsonApiId
	private Long id;

	private String name;

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
}
