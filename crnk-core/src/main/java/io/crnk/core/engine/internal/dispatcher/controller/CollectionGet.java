package io.crnk.core.engine.internal.dispatcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.io.Serializable;

public class CollectionGet extends ResourceIncludeField {

	public CollectionGet(ResourceRegistry resourceRegistry, TypeParser typeParser, DocumentMapper documentMapper) {
		super(resourceRegistry, typeParser, documentMapper);
	}

	/**
	 * Check if it is a GET request for a collection of resources.
	 */
	@Override
	public boolean isAcceptable(JsonPath jsonPath, String requestType) {
		return jsonPath.isCollection()
				&& jsonPath instanceof ResourcePath
				&& HttpMethod.GET.name().equals(requestType);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Response handle(JsonPath jsonPath, QueryAdapter queryAdapter, RepositoryMethodParameterProvider
			parameterProvider, Document requestBody) {
		String resourceName = jsonPath.getElementName();
		RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
		if (registryEntry == null) {
			throw new ResourceNotFoundException(resourceName);
		}
		Document responseDocument;
		ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository(parameterProvider);
		JsonApiResponse entities;
		if (jsonPath.getIds() == null || jsonPath.getIds().getIds().isEmpty()) {
			entities = resourceRepository.findAll(queryAdapter);
		} else {
			Class<? extends Serializable> idType = (Class<? extends Serializable>) registryEntry
					.getResourceInformation().getIdField().getType();
			Iterable<? extends Serializable> parsedIds = typeParser.parse((Iterable<String>) jsonPath.getIds().getIds(),
					idType);
			entities = resourceRepository.findAll(parsedIds, queryAdapter);
		}
		responseDocument = documentMapper.toDocument(entities, queryAdapter, parameterProvider);

		return new Response(responseDocument, 200);
	}
}
