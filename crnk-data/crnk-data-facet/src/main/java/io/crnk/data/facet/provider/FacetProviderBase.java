package io.crnk.data.facet.provider;

import io.crnk.data.facet.FacetValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class FacetProviderBase implements FacetProvider {

	protected FacetProviderContext context;

	public void init(FacetProviderContext context) {
		this.context = context;
	}

	protected List<FacetValue> toList(Map<Object, FacetValue> facetValueMap) {
		List<FacetValue> facetValues = new ArrayList<>(facetValueMap.values());
		Collections.sort(facetValues);
		return facetValues;
	}
}
