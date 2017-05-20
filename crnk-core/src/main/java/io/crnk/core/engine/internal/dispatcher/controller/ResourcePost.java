package io.crnk.core.engine.internal.dispatcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.RequestBodyException;
import io.crnk.core.exception.RequestBodyNotFoundException;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.util.Collection;
import java.util.Set;

public class ResourcePost extends ResourceUpsert {

	public ResourcePost(ResourceRegistry resourceRegistry, PropertiesProvider propertiesProvider, TypeParser typeParser, ObjectMapper objectMapper, DocumentMapper documentMapper) {
		super(resourceRegistry, propertiesProvider, typeParser, objectMapper, documentMapper);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Check if it is a POST request for a resource.
	 */
	@Override
	public boolean isAcceptable(JsonPath jsonPath, String requestType) {
		return jsonPath.isCollection() &&
				jsonPath instanceof ResourcePath &&
				HttpMethod.POST.name()
						.equals(requestType);
	}

	@Override
	public Response handle(JsonPath jsonPath, QueryAdapter queryAdapter,
						   RepositoryMethodParameterProvider parameterProvider, Document document) {
		String resourceEndpointName = jsonPath.getResourceName();
		RegistryEntry endpointRegistryEntry = resourceRegistry.getEntry(resourceEndpointName);
		if (endpointRegistryEntry == null) {
			throw new ResourceNotFoundException(resourceEndpointName);
		}
		if (document == null) {
			throw new RequestBodyNotFoundException(HttpMethod.POST, resourceEndpointName);
		}
		if (document.getData() instanceof Collection) {
			throw new RequestBodyException(HttpMethod.POST, resourceEndpointName, "Multiple data in body");
		}

		Resource resourceBody = (Resource) document.getData().get();
		if (resourceBody == null) {
			throw new RequestBodyException(HttpMethod.POST, resourceEndpointName, "No data field in the body.");
		}
		RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(resourceBody.getType());
		verifyTypes(HttpMethod.POST, resourceEndpointName, endpointRegistryEntry, bodyRegistryEntry);
		Object newResource = newResource(bodyRegistryEntry.getResourceInformation(), resourceBody);

		setId(resourceBody, newResource, bodyRegistryEntry.getResourceInformation());
		setAttributes(resourceBody, newResource, bodyRegistryEntry.getResourceInformation());
		ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository(parameterProvider);
		setRelations(newResource, bodyRegistryEntry, resourceBody, queryAdapter, parameterProvider);

		JsonApiResponse apiResponse = resourceRepository.create(newResource, queryAdapter);
		if (apiResponse.getEntity() == null) {
			throw new IllegalStateException("repository did not return the created resource");
		}
		Set<String> loadedRelationshipNames = getLoadedRelationshipNames(resourceBody);
		Document responseDocument = documentMapper.toDocument(apiResponse, queryAdapter, parameterProvider, loadedRelationshipNames);

		return new Response(responseDocument, HttpStatus.CREATED_201);
	}

	@Override
	protected boolean canModifyField(ResourceInformation resourceInformation, String fieldName, ResourceField field) {
		// allow dynamic field where field == null
		return field == null || field.getAccess().isPostable();
	}
}
