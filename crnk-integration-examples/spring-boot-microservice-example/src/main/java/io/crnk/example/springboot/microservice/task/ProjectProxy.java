package io.crnk.example.springboot.microservice.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiLinksInformation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.links.DefaultSelfLinksInformation;
import io.crnk.core.resource.meta.ResourceProxy;

import java.util.HashMap;
import java.util.Map;

@JsonApiResource(type = "project")
public class ProjectProxy implements ResourceProxy {

	@JsonApiId
	private Long id;

	private Map<String, JsonNode> attributes = new HashMap<>();

	@JsonApiLinksInformation
	private DefaultSelfLinksInformation links = new DefaultSelfLinksInformation();

	public Long getId() {
		return id;
	}

	public DefaultSelfLinksInformation getLinks() {
		return links;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setLinks(DefaultSelfLinksInformation links) {
		this.links = links;
	}

	public Map<String, JsonNode> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, JsonNode> attributes) {
		this.attributes = attributes;
	}
}
