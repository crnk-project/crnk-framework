package io.crnk.data.jpa.model;

import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class TestMappedSuperclassWithGenericsInterface<T extends TestInterfaceWithGenerics<T>>
		implements TestInterfaceWithGenerics<T> {

	@Id
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	private T generic;

	@Override
	public T getGeneric() {
		return generic;
	}

	@Override
	public void setGeneric(T generic) {
		this.generic = generic;
	}

}
