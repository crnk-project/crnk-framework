package io.crnk.data.facet.config;

import io.crnk.data.facet.provider.FacetProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FacetResourceInformation {

	private String type;

	private FacetProvider provider;

	private Map<String, FacetInformation> facets = new HashMap<>();

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
