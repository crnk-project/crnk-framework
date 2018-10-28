package io.crnk.data.facet.config;

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
		if (this.resource != null) {
			throw new IllegalStateException("already registered to " + this.resource);
		}
		this.resource = resource;
	}
}
