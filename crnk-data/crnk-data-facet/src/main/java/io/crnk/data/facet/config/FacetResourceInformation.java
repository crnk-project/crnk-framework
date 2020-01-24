package io.crnk.data.facet.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.crnk.data.facet.provider.FacetProvider;

public class FacetResourceInformation {

	private String resourceType;

	private FacetProvider provider;

	private Map<String, FacetInformation> facets = new HashMap<>();

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}

	public FacetProvider getProvider() {
		return provider;
	}

	public void setProvider(FacetProvider provider) {
		this.provider = provider;
	}

	public Map<String, FacetInformation> getFacets() {
		return Collections.unmodifiableMap(facets);
	}

	public void addFacet(FacetInformation facetInformation) {
		facetInformation.setResource(this);
		facets.put(facetInformation.getName(), facetInformation);
	}
}
