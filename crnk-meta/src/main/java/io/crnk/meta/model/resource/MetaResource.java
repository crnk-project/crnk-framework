package io.crnk.meta.model.resource;

import io.crnk.core.resource.annotations.JsonApiResource;

/**
 * A JSON API resource.
 */
@JsonApiResource("meta/resource")
public class MetaResource extends MetaResourceBase { // NOSONAR ignore exception hierarchy

	private String resourceType;

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
}
