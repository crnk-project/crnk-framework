package io.crnk.data.facet.config;

import io.crnk.core.engine.internal.utils.PreconditionUtil;

public class FacetInformation {

	private String name;

	private FacetResourceInformation resource;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FacetResourceInformation getResource() {
		return resource;
	}

	protected void setResource(FacetResourceInformation resource) {
		PreconditionUtil.verify(this.resource == null, "already registered to %s", this.resource);
		this.resource = resource;
	}
}
