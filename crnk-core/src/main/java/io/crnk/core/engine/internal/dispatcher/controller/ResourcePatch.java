package io.crnk.core.engine.internal.dispatcher.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.ExceptionUtil;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Callable;

public class ResourcePatch extends ResourceUpsert {

	public ResourcePatch(ResourceRegistry resourceRegistry, PropertiesProvider propertiesProvider, TypeParser typeParser,
						 @SuppressWarnings("SameParameterValue") ObjectMapper objectMapper, DocumentMapper documentMapper) {
		super(resourceRegistry, propertiesProvider, typeParser, objectMapper, documentMapper);
	}

	@Override
	protected HttpMethod getHttpMethod() {
		return HttpMethod.PATCH;
	}

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String requestType) {
		return !jsonPath.isCollection() &&
				jsonPath instanceof ResourcePath &&
				HttpMethod.PATCH.name().equals(requestType);
	}

	@Override
	public Response handle(JsonPath jsonPath, QueryAdapter queryAdapter,
						   RepositoryMethodParameterProvider parameterProvider, Document requestDocument) {

		RegistryEntry endpointRegistryEntry = getRegistryEntry(jsonPath);
		final Resource resourceBody = getRequestBody(requestDocument, jsonPath, HttpMethod.PATCH);
		RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(resourceBody.getType());

		String idString = jsonPath.getIds().getIds().get(0);

		ResourceInformation resourceInformation = bodyRegistryEntry.getResourceInformation();
		Serializable resourceId = resourceInformation.parseIdString(idString);

		verifyTypes(HttpMethod.PATCH, endpointRegistryEntry, bodyRegistryEntry);

		ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository(parameterProvider);
		JsonApiResponse resourceFindResponse = resourceRepository.findOne(resourceId, queryAdapter);
		Object resource = resourceFindResponse.getEntity();
		if (resource == null) {
			throw new ResourceNotFoundException(jsonPath.toString());
		}
		final Resource resourceFindData =
				documentMapper.toDocument(resourceFindResponse, queryAdapter, parameterProvider).getSingleData().get();

		resourceInformation.verify(resource, requestDocument);

		// extract current attributes from findOne without any manipulation by query params (such as sparse fieldsets)
		ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				String attributesFromFindOne = extractAttributesFromResourceAsJson(resourceFindData);
				Map<String, Object> attributesToUpdate =
						new HashMap<>(emptyIfNull(objectMapper.readValue(attributesFromFindOne, Map.class)));

				// deserialize the request JSON's attributes object into a map
				String attributesAsJson = objectMapper.writeValueAsString(resourceBody.getAttributes());
				Map<String, Object> attributesFromRequest = emptyIfNull(objectMapper.readValue(attributesAsJson, Map.class));

				// remove attributes that were omitted in the request
				Iterator<String> it = attributesToUpdate.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					if (!attributesFromRequest.containsKey(key)) {
						it.remove();
					}
				}

				// walk the source map and apply target values from request
				updateValues(attributesToUpdate, attributesFromRequest);
				Map<String, JsonNode> upsertedAttributes = new HashMap<>();
				for (Map.Entry<String, Object> entry : attributesToUpdate.entrySet()) {
					JsonNode value = objectMapper.valueToTree(entry.getValue());
					upsertedAttributes.put(entry.getKey(), value);
				}

				resourceBody.setAttributes(upsertedAttributes);
				return null;
			}
		}, "failed to merge patched attributes");

		JsonApiResponse updatedResource;
		Set<String> loadedRelationshipNames;
		if (resourceInformation.getResourceClass() == Resource.class) {
			loadedRelationshipNames = getLoadedRelationshipNames(resourceBody);
			updatedResource = resourceRepository.update(resourceBody, queryAdapter);
		} else {
			setAttributes(resourceBody, resource, bodyRegistryEntry.getResourceInformation());
			setRelations(resource, bodyRegistryEntry, resourceBody, queryAdapter, parameterProvider, false);
			loadedRelationshipNames = getLoadedRelationshipNames(resourceBody);
			updatedResource = resourceRepository.update(resource, queryAdapter);
		}
		Document responseDocument =
				documentMapper.toDocument(updatedResource, queryAdapter, parameterProvider, loadedRelationshipNames);

		return new Response(responseDocument, 200);
	}


	private <K, V> Map<K, V> emptyIfNull(Map<K, V> value) {
		return (Map<K, V>) (value != null ? value : Collections.emptyMap());
	}

	private String extractAttributesFromResourceAsJson(Resource resource) throws IOException {

		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(resource);
		// deserialize using the objectMapper so it becomes json-api
		String newRequestBody = objectMapper.writeValueAsString(resource);
		JsonNode node = objectMapper.readTree(newRequestBody);
		JsonNode attributes = node.findValue("attributes");
		return objectMapper.writeValueAsString(attributes);

	}

	private void updateValues(Map<String, Object> source, Map<String, Object> updates) {

		for (Map.Entry<String, Object> entry : updates.entrySet()) {
			String fieldName = entry.getKey();
			Object updatedValue = entry.getValue();

			// updating an embedded object
			if (updatedValue instanceof Map) {

				// source may lack the whole entry yet
				if (source.get(fieldName) == null) {
					source.put(fieldName, new HashMap<>());
				}

				Object sourceMap = source.get(fieldName);
				updateValues((Map<String, Object>) sourceMap, (Map<String, Object>) updatedValue);
				continue;
			}

			// updating a simple value
			source.put(fieldName, updatedValue);
		}
	}
}
