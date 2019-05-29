package io.crnk.core.engine.internal.jackson.mock.models;

import java.util.Collections;
import java.util.List;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;

@JsonApiResource(type = "classAsWithInclusion")
public class ClassAWithInclusion {

	@JsonApiId
	private Long id;

	@JsonApiRelation(serialize = SerializeType.EAGER, lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
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
