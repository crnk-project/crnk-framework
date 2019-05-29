package io.crnk.core.engine.internal.jackson.mock.models;

import java.util.List;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "classCs")
public class ClassC {

	@JsonApiId
	private Long id;

	@JsonApiRelation
	private List<ClassA> classAs;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<ClassA> getClassAs() {
		return classAs;
	}

	public void setClassAs(List<ClassA> classAs) {
		this.classAs = classAs;
	}
}
