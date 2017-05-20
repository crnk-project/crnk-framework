package io.crnk.core.engine.internal.jackson.mock.models;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiIncludeByDefault;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiToMany;

import java.util.Collections;
import java.util.List;

@JsonApiResource(type = "classAsWithInclusion")
public class ClassAWithInclusion {
	@JsonApiId
	private Long id;

	@JsonApiToMany(lazy = false)
	@JsonApiIncludeByDefault
	private List<ClassBWithInclusion> classBsWithInclusion;

	public ClassAWithInclusion() {
	}

	public ClassAWithInclusion(ClassBWithInclusion classBsWithInclusion) {
		this.classBsWithInclusion = Collections.singletonList(classBsWithInclusion);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<ClassBWithInclusion> getClassBsWithInclusion() {
		return classBsWithInclusion;
	}

	public void setClassBsWithInclusion(List<ClassBWithInclusion> classBsWithInclusion) {
		this.classBsWithInclusion = classBsWithInclusion;
	}
}
