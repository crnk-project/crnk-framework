package io.crnk.data.facet;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonApiResource(type = "facet")
public class FacetResource {

	public static final String ATTR_TYPE = "type";

	public static final String ATTR_NAME = "name";

	public static final String ATTR_VALUES = "values";

	@JsonApiId
	private String id;

	private String type;

	private String name;

	private Map<String, FacetValue> values = new HashMap<>();

	private List<String> labels;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, FacetValue> getValues() {
		return values;
	}

	public void setValues(Map<String, FacetValue> values) {
		this.values = values;
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}
}
