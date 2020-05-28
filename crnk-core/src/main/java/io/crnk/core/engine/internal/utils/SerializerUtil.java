package io.crnk.core.engine.internal.utils;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.resource.links.DefaultLink;
import io.crnk.core.resource.links.Link;

/**
 * Util class taking care of link serialization.<br />
 * Links are either serialized as simple JSON attributes or as JSON object.
 */
public class SerializerUtil {

	public static String HREF = "href";

	public static String REL = "rel";

	public static String ANCHOR = "anchor";

	public static String PARAMS = "params";

	public static String DESCRIBEDBY = "describedby";

	public static String META = "meta";

	private boolean serializeLinksAsObjects;

	/**
	 * Constructor takes a flag to decide whether links should be serialized as JSON objects or not.
	 *
	 * @param serializeLinksAsObjects if set to <code>true</code>, links will be serialized as JSON objects,<br />
	 * otherwise as simple JSON attributes.
	 */
	public SerializerUtil(boolean serializeLinksAsObjects) {
		this.serializeLinksAsObjects = serializeLinksAsObjects;
	}

	public ObjectNode serializeLink(ObjectMapper objectMapper, ObjectNode node, String fieldName, Link link) {
		Boolean shouldSerializeLinksAsObjects = serializeLinksAsObjects;
		if (!shouldSerializeLinksAsObjects) {
			shouldSerializeLinksAsObjects = link.getRel() != null || link.getAnchor() != null || link.getParams() != null || link.getDescribedby() != null || link.getMeta() != null;
		}
		if (shouldSerializeLinksAsObjects) {
			ObjectNode linkNode = objectMapper.createObjectNode();
			linkNode.put(HREF, link.getHref());
			if (link.getRel() != null) {
				linkNode.put(REL, link.getRel());
			}
			if (link.getAnchor() != null) {
				linkNode.put(ANCHOR, link.getAnchor());
			}
			//linkNode.put(PARAMS, link.getParams());
			if (link.getDescribedby() != null) {
				linkNode.put(DESCRIBEDBY, link.getDescribedby());
			}
			//linkNode.put(META, link.getMeta());
			node.set(fieldName, linkNode);
		} else {
			node.put(fieldName, link.getHref());
		}
		return node;
	}


	public Link getLinks(ObjectNode node, String fieldName) {
		JsonNode linkNode = node != null ? node.get(fieldName) : null;
		if (linkNode != null) {
			if (linkNode.isTextual()) { // The link is a string
				return new DefaultLink(linkNode.textValue());
			} else { // The link is an object
				JsonNode hrefNode = linkNode.get(HREF);
				JsonNode relNode = linkNode.get(REL);
				JsonNode anchorNode = linkNode.get(ANCHOR);
				JsonNode paramsNode = linkNode.get(PARAMS);
				JsonNode describedbyNode = linkNode.get(DESCRIBEDBY);
				JsonNode metaNode = linkNode.get(META);
				if (hrefNode == null) {
					return null;
				}
				Link link = new DefaultLink(hrefNode.textValue());
				if (relNode != null) {
					link.setRel(relNode.textValue());
				}
				if (anchorNode != null) {
					link.setAnchor(anchorNode.textValue());
				}
				if (describedbyNode != null) {
					link.setDescribedby(describedbyNode.textValue());
				}
				return link;
			}
		}
		return null;
	}


	public void serializeLink(JsonGenerator gen, String fieldName, String url) throws IOException {
		if (serializeLinksAsObjects) {
			gen.writeObjectFieldStart(fieldName);
			gen.writeStringField(HREF, url);
			gen.writeEndObject();
		} else {
			gen.writeStringField(fieldName, url);
		}
	}

	public static String deserializeLink(String fieldName, JsonNode jsonNode) throws IOException {
		JsonNode linkNode = jsonNode.get(fieldName);
		if (linkNode != null && linkNode.has(HREF)) {
			return readStringIfExists(HREF, linkNode);
		}
		return readStringIfExists(fieldName, jsonNode);
	}

	public static String readStringIfExists(String fieldName, JsonNode jsonNode) {
		JsonNode node = jsonNode.get(fieldName);
		if (node != null) {
			return node.asText();
		} else {
			return null;
		}
	}

	public static void writeStringIfExists(String fieldName, String value, JsonGenerator gen) throws IOException {
		if (value != null) {
			gen.writeStringField(fieldName, value);
		}
	}
}
