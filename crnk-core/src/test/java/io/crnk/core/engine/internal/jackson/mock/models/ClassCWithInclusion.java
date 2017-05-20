package io.crnk.core.engine.internal.jackson.mock.models;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiIncludeByDefault;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiToMany;

import java.util.Collections;
import java.util.List;

@JsonApiResource(type = "classCsWithInclusion")
public class ClassCWithInclusion {
	@JsonApiId
	private Long id;

	@JsonApiToMany(lazy = false)
	@JsonApiIncludeByDefault
	private List<ClassCWithInclusion> classCsWithInclusion;

	public ClassCWithInclusion() {
	}

	public ClassCWithInclusion(ClassCWithInclusion classCsWithInclusion) {
		this.classCsWithInclusion = Collections.singletonList(classCsWithInclusion);
	}

	public Long getId() {
		return id;
	}

	public ClassCWithInclusion setId(Long id) {
		this.id = id;
		return this;
	}

	public List<ClassCWithInclusion> getClassCsWithInclusion() {
		return classCsWithInclusion;
	}

	public void setClassCsWithInclusion(List<ClassCWithInclusion> classCsWithInclusion) {
		this.classCsWithInclusion = classCsWithInclusion;
	}
}
