package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathIds;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.io.Serializable;

public class ResourceDelete extends BaseController {

	private final ResourceRegistry resourceRegistry;
	private final TypeParser typeParser;

	public ResourceDelete(ResourceRegistry resourceRegistry, TypeParser typeParser) {
		this.resourceRegistry = resourceRegistry;
		this.typeParser = typeParser;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Checks if requested resource method is acceptable - is a DELETE request for a resource.
	 */
	@Override
	public boolean isAcceptable(JsonPath jsonPath, String requestType) {
		return !jsonPath.isCollection()
				&& jsonPath instanceof ResourcePath
				&& HttpMethod.DELETE.name().equals(requestType);
	}

	@Override
	public Response handle(JsonPath jsonPath, QueryAdapter queryAdapter,
						   RepositoryMethodParameterProvider parameterProvider, Document requestBody) {
		String resourceName = jsonPath.getElementName();
		PathIds resourceIds = jsonPath.getIds();
		RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
		if (registryEntry == null) {
			//TODO: Add JsonPath toString and provide to exception?
			throw new ResourceNotFoundException(resourceName);
		}
		for (String id : resourceIds.getIds()) {
			@SuppressWarnings("unchecked") Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
					.getResourceInformation()
					.getIdField()
					.getType();
			Serializable castedId = registryEntry.getResourceInformation().parseIdString(id);
			//noinspection unchecked
			registryEntry.getResourceRepository(parameterProvider).delete(castedId, queryAdapter);
		}

		return new Response(null, 204);
	}
}
