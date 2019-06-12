package io.crnk.core.engine.internal.jackson.mock.models;

import java.util.Collections;
import java.util.List;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.SerializeType;

@JsonApiResource(type = "classBs")
public class ClassB {

	@JsonApiRelation(serialize = SerializeType.EAGER)
	private final List<ClassC> classCs;

	@JsonApiRelation
	private final ClassC classC;

	@JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL)
	private final ClassA classA;

	@JsonApiId
	private Long id;

	public ClassB() {
		this.classCs = null;
		this.classC = null;
		this.classA = null;
	}

	public ClassB(ClassC classCs, ClassC classC) {
		this.classCs = Collections.singletonList(classCs);
		this.classC = classC;
		this.classA = null;
	}

	public ClassB(ClassA classA) {
		this.classA = classA;
		this.classC = null;
		this.classCs = null;
	}

	public ClassB(ClassC classCs, ClassC classC, ClassA classA) {
		this.classCs = Collections.singletonList(classCs);
		this.classC = classC;
		this.classA = classA;
	}

	public Long getId() {
		return id;
	}

	public ClassB setId(Long id) {
		this.id = id;
		return this;
	}

	public List<ClassC> getClassCs() {
		return classCs;
	}

	public ClassC getClassC() {
		return classC;
	}

	public ClassA getClassA() {
		return classA;
	}
}
