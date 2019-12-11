package io.crnk.format.plainjson.internal;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.document.Relationship;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.utils.Nullable;

/**
 * Serializes top-level Errors object.
 */
public class PlainJsonDocumentSerializer extends JsonSerializer<PlainJsonDocument> {


	@Override
	public void serialize(PlainJsonDocument document, JsonGenerator gen, SerializerProvider serializerProvider)
			throws IOException {
		gen.writeStartObject();

		writeLinks(gen, document.getLinks(), serializerProvider);
		writeMeta(gen, document.getMeta(), serializerProvider);
		writeData(gen, document.getData(), document.getIncluded(), serializerProvider);
		writeErrors(gen, document.getErrors());
		writeJsonApi(gen, document.getJsonapi(), serializerProvider);

		gen.writeEndObject();
	}

	private void writeData(JsonGenerator gen, Nullable<Object> nullableData, List<Resource> included,
			SerializerProvider serializerProvider)
			throws IOException {
		if (nullableData.isPresent()) {


			Stack<ResourceIdentifier> inclusionStack = new Stack<>();

			Map<ResourceIdentifier, Resource> resourceMap = new HashMap<>();
			if (included != null) {
				included.stream().forEach(resource -> resourceMap.put(resource.toIdentifier(), resource));
			}

			gen.writeFieldName("data");
			Object data = nullableData.get();
			if (data instanceof Collection) {
				Collection<ResourceIdentifier> resources = ((Collection<ResourceIdentifier>) data);
				resources.stream().forEach(resource -> {
					if (resource instanceof Resource) {
						resourceMap.put(resource.toIdentifier(), (Resource) resource);
					}
				});

				writeResources(gen, (Collection<ResourceIdentifier>) data, resourceMap, inclusionStack, serializerProvider);
			}
			else if (data == null) {
				gen.writeNull();
			}
			else {
				ResourceIdentifier resource = (ResourceIdentifier) data;
				if (resource.getId() != null && resource instanceof Resource) {
					// newly created resources do not have an ID
					ResourceIdentifier resourceId = resource.toIdentifier();
					resourceMap.put(resourceId, (Resource) resource);
				}
				writeResource(gen, resource, resourceMap, inclusionStack, serializerProvider);
			}
		}
	}

	private void writeJsonApi(JsonGenerator gen, ObjectNode jsonapi, SerializerProvider serializerProvider) throws IOException {
		if (jsonapi != null && !jsonapi.isEmpty(serializerProvider)) {
			gen.writeObjectField("jsonapi", jsonapi);
		}
	}

	private void writeErrors(JsonGenerator gen, List<ErrorData> errors) throws IOException {
		if (errors != null && !errors.isEmpty()) {
			gen.writeObjectField("errors", errors);
		}
	}


	private void writeResources(JsonGenerator gen, Collection<ResourceIdentifier> resources,
			Map<ResourceIdentifier, Resource> resourceMap,
			Stack<ResourceIdentifier> inclusionStack,
			SerializerProvider serializerProvider) throws IOException {


		gen.writeStartArray();
		for (ResourceIdentifier resource : resources) {
			writeResource(gen, resource, resourceMap, inclusionStack, serializerProvider);
		}
		gen.writeEndArray();
	}


	private void writeResource(JsonGenerator gen, ResourceIdentifier resourceId,
			Map<ResourceIdentifier, Resource> resourceMap, Stack<ResourceIdentifier> inclusionStack,
			SerializerProvider serializerProvider) throws IOException {

		if (resourceId.getId() != null) {
			// new resources do not have an ID
			inclusionStack.add(resourceId.toIdentifier());
		}

		gen.writeStartObject();
		if (resourceId.getId() != null) {
			gen.writeStringField("id", resourceId.getId());
		}
		gen.writeStringField("type", resourceId.getType());

		if (resourceId instanceof Resource) {
			Resource resource = (Resource) resourceId;
			writeLinks(gen, resource.getLinks(), serializerProvider);
			writeMeta(gen, resource.getMeta(), serializerProvider);
			writeAttributes(gen, resource.getAttributes(), serializerProvider);
			writeRelationships(gen, resource.getRelationships(), resourceMap, inclusionStack, serializerProvider);
		}
		gen.writeEndObject();
		if (resourceId != null) {
			inclusionStack.remove(resourceId);
		}
	}

	private void writeRelationships(JsonGenerator gen, Map<String, Relationship> relationships,
			Map<ResourceIdentifier, Resource> resourceMap,
			Stack<ResourceIdentifier> inclusionStack,
			SerializerProvider serializerProvider) throws IOException {

		for (Map.Entry<String, Relationship> entry : relationships.entrySet()) {
			writeRelationship(gen, entry.getKey(), entry.getValue(), resourceMap, inclusionStack, serializerProvider);
		}
	}

	private void writeRelationship(JsonGenerator gen, String name, Relationship relationship,
			Map<ResourceIdentifier, Resource> resourceMap,
			Stack<ResourceIdentifier> inclusionStack,
			SerializerProvider serializerProvider) throws IOException {
		gen.writeObjectFieldStart(name);

		writeLinks(gen, relationship.getLinks(), serializerProvider);
		writeMeta(gen, relationship.getMeta(), serializerProvider);

		if (relationship.getData().isPresent()) {
			gen.writeFieldName("data");
			if (relationship.getData().get() instanceof Collection) {
				writeRelationshipData(gen, relationship.getCollectionData().get(), resourceMap, inclusionStack,
						serializerProvider);
			}
			else {
				writeRelationshipData(gen, relationship.getSingleData().get(), resourceMap, inclusionStack, serializerProvider);
			}
		}
		gen.writeEndObject();
	}

	private void writeRelationshipData(JsonGenerator gen, ResourceIdentifier resourceId,
			Map<ResourceIdentifier, Resource> resourceMap,
			Stack<ResourceIdentifier> inclusionStack,
			SerializerProvider serializerProvider) throws IOException {

		Resource resource = resourceMap.get(resourceId);
		if (resource != null && !inclusionStack.contains(resourceId)) {
			writeResource(gen, resource, resourceMap, inclusionStack, serializerProvider);
		}
		else {
			gen.writeObject(resourceId);
		}
	}

	private void writeRelationshipData(JsonGenerator gen, Collection<ResourceIdentifier> resourceIdentifiers,
			Map<ResourceIdentifier, Resource> resourceMap, Stack<ResourceIdentifier> inclusionStack,
			SerializerProvider serializerProvider) throws IOException {

		gen.writeStartArray();
		for (ResourceIdentifier resourceIdentifier : resourceIdentifiers) {
			writeRelationshipData(gen, resourceIdentifier, resourceMap, inclusionStack, serializerProvider);
		}
		gen.writeEndArray();
	}

	private void writeAttributes(JsonGenerator gen, Map<String, JsonNode> attributes, SerializerProvider serializerProvider)
			throws IOException {
		for (Map.Entry<String, JsonNode> entry : attributes.entrySet()) {
			writeAttribute(gen, entry.getKey(), entry.getValue());
		}
	}

	private void writeAttribute(JsonGenerator gen, String key, JsonNode value) throws IOException {
		gen.writeObjectField(key, value);
	}

	private void writeLinks(JsonGenerator gen, ObjectNode links, SerializerProvider serializerProvider) throws IOException {
		if (links != null && !links.isEmpty(serializerProvider)) {
			gen.writeObjectField("links", links);
		}
	}

	private void writeMeta(JsonGenerator gen, ObjectNode meta, SerializerProvider serializerProvider) throws IOException {
		if (meta != null && !meta.isEmpty(serializerProvider)) {
			gen.writeObjectField("meta", meta);
		}
	}

	@Override
	public Class<PlainJsonDocument> handledType() {
		return PlainJsonDocument.class;
	}

}
