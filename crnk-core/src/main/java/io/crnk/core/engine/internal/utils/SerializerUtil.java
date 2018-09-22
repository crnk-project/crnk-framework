package io.crnk.core.engine.internal.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

/**
 * Util class taking care of link serialization.<br />
 * Links are either serialized as simple JSON attributes or as JSON object.
 */
public class SerializerUtil {

	public static String HREF = "href";

	private boolean serializeLinksAsObjects;

	/**
	 * Constructor takes a flag to decide whether links should be serialized as JSON objects or not.
	 *
	 * @param serializeLinksAsObjects if set to <code>true</code>, links will be serialized as JSON objects,<br />
	 *                                otherwise as simple JSON attributes.
	 */
	public SerializerUtil(boolean serializeLinksAsObjects) {
		this.serializeLinksAsObjects = serializeLinksAsObjects;
	}

	public ObjectNode serializeLink(ObjectMapper objectMapper, ObjectNode node, String fieldName, String url) {
		if (serializeLinksAsObjects) {
			ObjectNode linkNode = objectMapper.createObjectNode();
			linkNode.put(HREF, url);
			node.set(fieldName, linkNode);
		} else {
			node.put(fieldName, url);
		}
		return node;
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
