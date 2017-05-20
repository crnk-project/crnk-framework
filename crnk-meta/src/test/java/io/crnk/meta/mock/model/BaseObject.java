package io.crnk.meta.mock.model;

import io.crnk.core.resource.annotations.JsonApiId;

public class BaseObject {

	@JsonApiId
	private Long id;

	private String name;

	private String baseName;

	public Long getId() {
		return id;
	}

	public BaseObject setId(Long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}
}
