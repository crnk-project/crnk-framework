package io.crnk.jpa.model;

import io.crnk.core.resource.annotations.JsonApiResource;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@JsonApiResource(type = "json-api-resource-type", resourcePath = "json-api-resource-path")
public class JsonApiResourcePathTestEntity {

	@Id
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
