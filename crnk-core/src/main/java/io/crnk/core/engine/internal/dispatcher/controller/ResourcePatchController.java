package io.crnk.core.engine.internal.dispatcher.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

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
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.ExceptionUtil;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.annotations.PatchStrategy;

public class ResourcePatchController extends ResourceUpsert {

	@Override
	protected HttpMethod getHttpMethod() {
		return HttpMethod.PATCH;
	}

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String method) {
		return !jsonPath.isCollection() &&
				jsonPath instanceof ResourcePath &&
				HttpMethod.PATCH.name().equals(method);
	}

	@Override
	public Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter, Document requestDocument) {

		RegistryEntry endpointRegistryEntry = jsonPath.getRootEntry();
		final Resource requestResource = getRequestBody(requestDocument, jsonPath, HttpMethod.PATCH);
		RegistryEntry registryEntry = context.getResourceRegistry().getEntry(requestResource.getType());
		logger.debug("using registry entry {}", registryEntry);

		Serializable resourceId = jsonPath.getId();

		ResourceInformation resourceInformation = registryEntry.getResourceInformation();
		verifyTypes(HttpMethod.PATCH, endpointRegistryEntry, registryEntry);
		DocumentMappingConfig mappingConfig = context.getMappingConfig();
		DocumentMapper documentMapper = context.getDocumentMapper();

		ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository();
		return resourceRepository
				.findOne(resourceId, queryAdapter)
				.merge(existingResponse -> {
					Object existingEntity = existingResponse.getEntity();
					checkNotNull(existingEntity, jsonPath);
					resourceInformation.verify(existingEntity, requestDocument);
					return documentMapper.toDocument(existingResponse, queryAdapter, mappingConfig)
							.map(it -> it.getSingleData().get())
							.doWork(existing -> mergeNestedAttribute(existing, requestResource, resourceInformation))
							.map(it -> existingEntity);
				})
				.merge(existingEntity -> applyChanges(registryEntry, existingEntity, requestResource, queryAdapter))
				.map(this::toResponse);
	}

	private Response toResponse(Document updatedDocument) {
		if (!updatedDocument.getData().isPresent() && (updatedDocument.getErrors() == null || updatedDocument.getErrors().isEmpty())) {
			updatedDocument = null;
		}
		int status = getStatus(updatedDocument, HttpMethod.PATCH);
		return new Response(updatedDocument, status);
	}

	private Result<Document> applyChanges(RegistryEntry registryEntry, Object entity, Resource requestResource,
			QueryAdapter queryAdapter) {
		ResourceInformation resourceInformation = registryEntry.getResourceInformation();
		ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository();

		Set<String> loadedRelationshipNames;
		Result<JsonApiResponse> updatedResource;
		if (resourceInformation.getImplementationClass() == Resource.class) {
			loadedRelationshipNames = getLoadedRelationshipNames(requestResource);
			updatedResource = resourceRepository.update(requestResource, queryAdapter);
		}
		else {
			QueryContext queryContext = queryAdapter.getQueryContext();
			setAttributes(requestResource, entity, resourceInformation, queryContext);
			setMeta(requestResource, entity, resourceInformation);
			setLinks(requestResource, entity, resourceInformation);

			loadedRelationshipNames = getLoadedRelationshipNames(requestResource);

			Result<List> relationsResult =
					setRelationsAsync(entity, registryEntry, requestResource, queryAdapter, false);
			updatedResource = relationsResult.merge(it -> resourceRepository.update(entity, queryAdapter));
		}

		DocumentMappingConfig mappingConfig = context.getMappingConfig().clone();
		mappingConfig.setFieldsWithEnforcedIdSerialization(loadedRelationshipNames);
		DocumentMapper documentMapper = context.getDocumentMapper();

		return updatedResource
				.doWork(it -> logger.debug("patched resource {}", it))
				.merge(it -> documentMapper.toDocument(it, queryAdapter, mappingConfig));
	}

	private void mergeNestedAttribute(Resource existingResource, Resource requestResource, ResourceInformation resourceInformation) {
		// extract current attributes from findOne without any manipulation by query params (such as sparse fieldsets)
		ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				ObjectMapper objectMapper = context.getObjectMapper();

				String attributesFromFindOne = extractAttributesFromResourceAsJson(existingResource);
				Map<String, Object> attributesToUpdate =
						new HashMap<>(emptyIfNull(objectMapper.readValue(attributesFromFindOne, Map.class)));

				// deserialize the request JSON's attributes object into a map
				String attributesAsJson = objectMapper.writeValueAsString(requestResource.getAttributes());
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
				updateValues(attributesToUpdate, attributesFromRequest, resourceInformation, null);
				Map<String, JsonNode> upsertedAttributes = new HashMap<>();
				for (Map.Entry<String, Object> entry : attributesToUpdate.entrySet()) {
					JsonNode value = objectMapper.valueToTree(entry.getValue());
					upsertedAttributes.put(entry.getKey(), value);
				}

				requestResource.setAttributes(upsertedAttributes);
				return null;
			}
		}, "failed to merge patched attributes");
	}

	private void checkNotNull(Object resource, JsonPath jsonPath) {
		if (resource == null) {
			throw new ResourceNotFoundException(jsonPath.toString());
		}
	}


	private <K, V> Map<K, V> emptyIfNull(Map<K, V> value) {
		return (Map<K, V>) (value != null ? value : Collections.emptyMap());
	}

	private String extractAttributesFromResourceAsJson(Resource resource) throws IOException {

		JsonApiResponse response = new JsonApiResponse();
		response.setEntity(resource);
		// deserialize using the objectMapper so it becomes json-api
		ObjectMapper objectMapper = context.getObjectMapper();
		String newRequestBody = objectMapper.writeValueAsString(resource);
		JsonNode node = objectMapper.readTree(newRequestBody);
		JsonNode attributes = node.findValue("attributes");
		return objectMapper.writeValueAsString(attributes);

	}

	private void updateValues(Map<String, Object> source, Map<String, Object> updates, ResourceInformation resourceInformation, PatchStrategy patchStrategy) {

		for (Map.Entry<String, Object> entry : updates.entrySet()) {
			String fieldName = entry.getKey();
			Object updatedValue = entry.getValue();

			// updating an embedded object
			if (updatedValue instanceof Map) {

				// we don't have yet ResourceFields for nested resource, make use of root patchStrategy instead
				if (patchStrategy == null) {
					ResourceField field = resourceInformation.findFieldByName(fieldName);
					PreconditionUtil.verify(field != null, "field %s not found", fieldName);
					patchStrategy = field.getPatchStrategy();
				}

				// source may lack the whole entry yet
				if (source.get(fieldName) == null) {
					source.put(fieldName, new HashMap<>());
				}

				if (patchStrategy == PatchStrategy.SET) {
					source.put(fieldName, updatedValue);
				}
				else {
					Object sourceMap = source.get(fieldName);
					updateValues((Map<String, Object>) sourceMap, (Map<String, Object>) updatedValue, resourceInformation, patchStrategy);
				}
				continue;
			}

			// updating a simple value
			source.put(fieldName, updatedValue);
		}
	}
}
