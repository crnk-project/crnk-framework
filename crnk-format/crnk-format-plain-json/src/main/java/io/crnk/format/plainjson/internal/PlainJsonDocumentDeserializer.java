package io.crnk.format.plainjson.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.internal.utils.SerializerUtil;
import io.crnk.core.utils.Nullable;

/**
 * Serializes top-level Errors object.
 */
public class PlainJsonDocumentDeserializer extends JsonDeserializer<PlainJsonDocument> {

	private static final List<String> SYSTEM_FIELDS = Arrays.asList("id", "type", "meta", "links");

	private ObjectMapper objectMapper;

	public PlainJsonDocumentDeserializer(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public PlainJsonDocument deserialize(JsonParser jp, DeserializationContext context) throws IOException {
		JsonNode documentNode = jp.readValueAsTree();

		PlainJsonDocument document = new PlainJsonDocument();
		document.setMeta((ObjectNode) documentNode.get("meta"));
		document.setLinks((ObjectNode) documentNode.get("links"));
		document.setJsonapi((ObjectNode) documentNode.get("jsonapi"));

		ArrayNode errors = (ArrayNode) documentNode.get("errors");
		if (errors != null) {
			ObjectReader errorReader = objectMapper.readerFor(ErrorData.class);
			List<ErrorData> errorDataList = new ArrayList<>();
			for (JsonNode error : errors) {
				ErrorData errorData = errorReader.readValue(error);
				errorDataList.add(errorData);
			}
			document.setErrors(errorDataList);
		}

 		Map<ResourceIdentifier, Resource> included = new HashMap<>();

		JsonNode data = documentNode.get("data");
		if (data instanceof ArrayNode) {
			ArrayNode arrayNode = (ArrayNode) data;
			List<Resource> resources = new ArrayList<>();
			for (JsonNode element : arrayNode) {
				resources.add(deserializeResource(element, included));
			}
			document.setData(Nullable.of(resources));
		}
		else if (data instanceof NullNode) {
			document.setData(Nullable.nullValue());
		}
		else if (data != null) {
			Resource resource = deserializeResource(data, included);
			document.setData(Nullable.of(resource));
		}
		else {
			document.setData(Nullable.empty());
		}

		if (!included.isEmpty()) {
			document.setIncluded(new ArrayList<>(included.values()));
		}

		return document;
	}

	private Resource deserializeResource(JsonNode data, Map<ResourceIdentifier, Resource> included) {
		Resource resource = new Resource();

		resource.setId(SerializerUtil.readStringIfExists("id", data));
		resource.setType(SerializerUtil.readStringIfExists("type", data));
		resource.setMeta((ObjectNode) data.get("meta"));
		resource.setLinks((ObjectNode) data.get("links"));

		Iterator<Map.Entry<String, JsonNode>> fields = data.fields();
		while (fields.hasNext()) {
			Map.Entry<String, JsonNode> entry = fields.next();
			String fieldName = entry.getKey();
			if (!SYSTEM_FIELDS.contains(fieldName)) {
				JsonNode fieldValue = entry.getValue();
				deserializeField(fieldName, fieldValue, resource, included);
			}
		}
		return resource;
	}

	private void deserializeField(String fieldName, JsonNode fieldValue, Resource resource,
			Map<ResourceIdentifier, Resource> included) {
		// simple heuristic to detect relationships, should be good enough
		boolean hasLinks = fieldValue instanceof ObjectNode && fieldValue.get("links") != null;
		boolean hasData = fieldValue instanceof ObjectNode && fieldValue.get("data") != null;
		boolean isRelationship = hasLinks || hasData;
		if (isRelationship) {
			Relationship relationship = new Relationship();
			relationship.setMeta((ObjectNode) fieldValue.get("meta"));
			relationship.setLinks((ObjectNode) fieldValue.get("links"));

			JsonNode relationshipData = fieldValue.get("data");

			if (relationshipData instanceof ArrayNode) {
				List<ResourceIdentifier> relationIds = new ArrayList<>();
				for (JsonNode elementNode : relationshipData) {
					ResourceIdentifier relationId = deserializeRelationship(elementNode, included);
					relationIds.add(relationId);
				}
				relationship.setData(Nullable.of(relationIds));
			}
			else if (relationshipData != null) {
				ResourceIdentifier relationId = deserializeRelationship(relationshipData, included);
				relationship.setData(Nullable.of(relationId));
			}
			resource.getRelationships().put(fieldName, relationship);
		}
		else {
			resource.getAttributes().put(fieldName, fieldValue);
		}
	}

	private ResourceIdentifier deserializeRelationship(JsonNode relatedDataNode, Map<ResourceIdentifier, Resource> included) {
		ResourceIdentifier relatedId = toResourceIdentifier(relatedDataNode);
		if (relatedId != null && isResource(relatedDataNode) && !included.containsKey(relatedId)) {
			Resource relatedResource = deserializeResource(relatedDataNode, included);
			included.put(relatedId, relatedResource);
		}
		return relatedId;
	}

	private boolean isResource(JsonNode elementNode) {
		Iterator<String> iterator = elementNode.fieldNames();
		while (iterator.hasNext()) {
			String fieldName = iterator.next();
			if (!SYSTEM_FIELDS.contains(fieldName)) {
				return true;
			}
		}
		return false;
	}

	private ResourceIdentifier toResourceIdentifier(JsonNode elementNode) {
		if (elementNode.isNull()) {
			return null;
		}
		String elementId = SerializerUtil.readStringIfExists("id", elementNode);
		String elementType = SerializerUtil.readStringIfExists("type", elementNode);
		return new ResourceIdentifier(elementId, elementType);
	}

}
