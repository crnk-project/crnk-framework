package io.crnk.core.engine.internal.jackson.mock.models;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonApiToMany;

import java.util.Collections;
import java.util.List;

@JsonApiResource("classAs")
public class ClassA {

	@JsonApiId
	private long id;

	@JsonApiToMany(lazy = false)
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
