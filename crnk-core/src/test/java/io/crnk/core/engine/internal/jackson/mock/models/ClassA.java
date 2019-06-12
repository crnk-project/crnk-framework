package io.crnk.core.engine.internal.jackson.mock.models;

import java.util.Collections;
import java.util.List;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.SerializeType;

@JsonApiResource(type = "classAs")
public class ClassA {

	@JsonApiId
	private long id;

	@JsonApiRelation(serialize = SerializeType.EAGER)
	private List<ClassB> classBs;

	public ClassA(ClassB classBs) {
		this.classBs = Collections.singletonList(classBs);
	}

	public ClassA(long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<ClassB> getClassBs() {
		return classBs;
	}
}
