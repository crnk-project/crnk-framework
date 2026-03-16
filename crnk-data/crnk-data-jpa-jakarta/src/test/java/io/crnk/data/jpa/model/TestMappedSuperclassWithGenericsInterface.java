package io.crnk.data.jpa.model;

import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

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
