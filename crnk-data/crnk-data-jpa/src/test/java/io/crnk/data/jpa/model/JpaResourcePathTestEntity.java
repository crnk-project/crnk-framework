package io.crnk.data.jpa.model;

import io.crnk.data.jpa.annotations.JpaResource;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@JpaResource(type = "jpaResourceTestEntity", resourcePath = "jpa-resource-test-entity")
public class JpaResourcePathTestEntity {

	@Id
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
