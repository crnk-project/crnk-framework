package io.crnk.data.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.MappedSuperclass;
import java.util.List;

@MappedSuperclass
public class TestMappedSupersuperclassWithGenerics<T, S> {
	@Column
	private T generic;

	@ElementCollection
	private List<S> genericListSuper;

	public TestMappedSupersuperclassWithGenerics() {}

	public T getGeneric() {
		return generic;
	}

	public void setGeneric(T generic) {
		this.generic = generic;
	}

	public List<S> getGenericListSuper() {
		return genericListSuper;
	}

	public void setGenericListSuper(List<S> genericListSuper) {
		this.genericListSuper = genericListSuper;
	}
}
