package io.crnk.jpa.model;

import io.crnk.core.resource.annotations.JsonApiResource;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@JsonApiResource(type = "jsonapiResourceTestEntity", resourcePath = "jsonapi-resource-test-entity")
public class JsonapiResourcePathTestEntity {

	@Id
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
