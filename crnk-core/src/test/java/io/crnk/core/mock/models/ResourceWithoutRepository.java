package io.crnk.core.mock.models;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "resourceWithoutRepository")
public class ResourceWithoutRepository {

	@JsonApiId
	public String id;

	@JsonApiRelation
	private Project project;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
}
