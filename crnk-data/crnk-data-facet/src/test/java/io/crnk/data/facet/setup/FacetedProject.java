package io.crnk.data.facet.setup;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.data.facet.annotation.Facet;

// tag::docs[]
@JsonApiResource(type = "projects")
public class FacetedProject {

	@JsonApiId
	private Long id;

	@Facet
	private String name;

	@Facet
	private int priority;

	// end::docs[]

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

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	// tag::docs[]
}
// end::docs[]
