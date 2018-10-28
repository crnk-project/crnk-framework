package io.crnk.data.facet;

import io.crnk.data.facet.config.FacetResourceInformation;

import java.util.HashMap;
import java.util.Map;

public class FacetModuleConfig {

	private Map<String, FacetResourceInformation> resources = new HashMap<>();

	public Map<String, FacetResourceInformation> getResources() {
		return resources;
	}

	public void addResource(FacetResourceInformation facetResesourceInformation) {
		resources.put(facetResesourceInformation.getType(), facetResesourceInformation);
	}
}
