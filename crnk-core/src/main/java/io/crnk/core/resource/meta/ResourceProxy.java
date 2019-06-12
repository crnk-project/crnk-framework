package io.crnk.core.resource.meta;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public interface ResourceProxy {

	Map<String, JsonNode> getAttributes();

	void setAttributes(Map<String, JsonNode> attributes);
}
