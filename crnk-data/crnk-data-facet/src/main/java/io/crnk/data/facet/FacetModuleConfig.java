package io.crnk.data.facet;

import java.util.HashMap;
import java.util.Map;

import io.crnk.data.facet.config.FacetResourceInformation;

public class FacetModuleConfig {

	private Map<String, FacetResourceInformation> resources = new HashMap<>();

	public Map<String, FacetResourceInformation> getResources() {
		return resources;
	}

	public void addResource(FacetResourceInformation facetResesourceInformation) {
		resources.put(facetResesourceInformation.getResourceType(), facetResesourceInformation);
	}
}
