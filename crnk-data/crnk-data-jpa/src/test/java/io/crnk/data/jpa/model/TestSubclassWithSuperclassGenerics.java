package io.crnk.data.jpa.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class TestSubclassWithSuperclassGenerics extends TestMappedSuperclassWithGenerics<String> {
	@Id
	private Long id;

	public TestSubclassWithSuperclassGenerics() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
