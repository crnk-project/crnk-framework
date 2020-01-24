package io.crnk.data.facet.setup;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.data.facet.annotation.Facet;

@JsonApiResource(type = FacetedTask.RESOURCE_TYPE)
public class FacetedTask {

	public static final String RESOURCE_TYPE = "tasks";

	@JsonApiId
	private Long id;

	@Facet
	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
