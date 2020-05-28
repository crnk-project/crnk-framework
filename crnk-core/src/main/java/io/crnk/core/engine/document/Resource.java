package io.crnk.core.engine.document;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.resource.ResourceTypeHolder;
import io.crnk.core.resource.list.LinksContainer;
import io.crnk.core.resource.meta.MetaContainer;

/**
 * Resource objects appear in a JSON API document to represent resources.
 * <p/>
 * http://jsonapi.org/format/#document-resource-objects
 */
public class Resource extends ResourceIdentifier implements MetaContainer, LinksContainer, ResourceTypeHolder {

	@JsonInclude(Include.NON_EMPTY)
	private ObjectNode links;

	@JsonInclude(Include.NON_EMPTY)
	private ObjectNode meta;

	@JsonInclude(Include.NON_EMPTY)
	private Map<String, JsonNode> attributes = new LinkedHashMap<>();

	@JsonInclude(Include.NON_EMPTY)
	private Map<String, Relationship> relationships = new HashMap<>();

	@Override
	public ObjectNode getLinks() {
		return links;
	}

	@Override
	public void setLinks(ObjectNode links) {
		this.links = links;
	}

	@Override
	public ObjectNode getMeta() {
		return meta;
	}

	@Override
	public void setMeta(ObjectNode meta) {
		this.meta = meta;
	}

	public Map<String, JsonNode> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, JsonNode> attributes) {
		this.attributes = attributes;
	}

	public Map<String, Relationship> getRelationships() {
		return relationships;
	}

	public void setRelationships(Map<String, Relationship> relationships) {
		this.relationships = relationships;
	}

	public void setAttribute(String name, JsonNode value) {
		attributes.put(name, value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, type, attributes, relationships, links, meta);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != Resource.class) {
			return false;
		}
		Resource other = (Resource) obj;
		return Objects.equals(attributes, other.attributes) && Objects.equals(relationships, other.relationships) && Objects
				.equals(meta, other.meta) && Objects.equals(links, other.links)
				&& Objects.equals(id, other.id) && Objects.equals(type, other.type);
	}

	@Override
	public ResourceIdentifier toIdentifier() {
		return new ResourceIdentifier(getId(), getType());
	}

}