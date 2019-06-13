package io.crnk.data.facet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;

@JsonApiResource(type = "facet")
public class FacetResource {

	public static final String ATTR_RESOURCE_TYPE = "resourceType";

	public static final String ATTR_NAME = "name";

	public static final String ATTR_VALUES = "values";

	@JsonApiId
	private String id;

	private String resourceType;

	private String name;

	private List<String> labels;

	/**
	 * Mapping of label to its value.
	 */
	private Map<String, FacetValue> values = new HashMap<>();

	/**
	 * Mapping of facet name to label of grouped value.
	 */
	private Map<String, String> groups = new HashMap<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getResourceType() {
		return resourceType;
	}

	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
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

	public Map<String, String> getGroups() {
		return groups;
	}

	public void setGroups(Map<String, String> groups) {
		this.groups = groups;
	}
}
