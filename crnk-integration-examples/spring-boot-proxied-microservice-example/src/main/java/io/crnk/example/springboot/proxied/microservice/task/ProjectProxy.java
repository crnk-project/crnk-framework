package io.crnk.example.springboot.proxied.microservice.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.resource.annotations.*;
import io.crnk.core.resource.links.DefaultSelfLinksInformation;
import io.crnk.core.resource.proxy.ProxyResource;
import io.crnk.example.springboot.proxied.microservice.project.Project;

import java.util.HashMap;
import java.util.Map;

@JsonApiResource(type = "project")
public class ProjectProxy implements ProxyResource {

	@JsonApiId
	private Long id;

	@JsonProperty
	private String name;

	@JsonProperty
	private Map<String, String> attributes = new HashMap<>();

	/**
	 * Links information necessary on objects to maintain urls from original microservice.
	 */
	@JsonApiLinksInformation
	private DefaultSelfLinksInformation links = new DefaultSelfLinksInformation();


	@Override
	public Map<String, String> getAttributes() {
		return attributes;
	}

	@Override
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public DefaultSelfLinksInformation getLinks() {
		return links;
	}

	public void setLinks(DefaultSelfLinksInformation links) {
		this.links = links;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
