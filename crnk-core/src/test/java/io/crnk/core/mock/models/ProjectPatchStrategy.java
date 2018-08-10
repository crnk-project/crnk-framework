package io.crnk.core.mock.models;

import io.crnk.core.resource.annotations.JsonApiField;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.PatchStrategy;

@JsonApiResource(type = "projects-patch-strategy")
public class ProjectPatchStrategy {

	@JsonApiId
	private Long id;

	private String name;

	@JsonApiField(patchStrategy = PatchStrategy.SET)
	private ProjectData data;

	public Long getId() {
		return id;
	}

	public ProjectPatchStrategy setId(Long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProjectData getData() {
		return data;
	}

	public void setData(ProjectData data) {
		this.data = data;
	}
}
