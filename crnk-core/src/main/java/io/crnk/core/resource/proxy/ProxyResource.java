package io.crnk.core.resource.proxy;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public interface ProxyResource {
	Map<String, String> getAttributes();

	void setAttributes(Map<String, String> attributes);
}
