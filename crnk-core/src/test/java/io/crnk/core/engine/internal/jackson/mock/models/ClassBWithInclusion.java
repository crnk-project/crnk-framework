package io.crnk.core.engine.internal.jackson.mock.models;

import java.util.Collections;
import java.util.List;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;

@JsonApiResource(type = "classBsWithInclusion")
public class ClassBWithInclusion {
	@JsonApiId
	private Long id;

	@JsonApiRelation(serialize = SerializeType.EAGER, lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private List<ClassCWithInclusion> classCsWithInclusion;

	public ClassBWithInclusion() {
	}

	public ClassBWithInclusion(ClassCWithInclusion classCsWithInclusion) {
		this.classCsWithInclusion = Collections.singletonList(classCsWithInclusion);
	}

	public Long getId() {
		return id;
	}

	public ClassBWithInclusion setId(Long id) {
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
