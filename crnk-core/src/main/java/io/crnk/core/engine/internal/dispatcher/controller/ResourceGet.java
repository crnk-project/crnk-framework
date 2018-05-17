package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathIds;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.utils.Nullable;
import io.crnk.core.utils.Optional;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.io.Serializable;

public class ResourceGet extends ResourceIncludeField {

	/**
	 * {@inheritDoc}
	 * <p>
	 * Checks if requested resource method is acceptable - is a GET request for
	 * a resource.
	 */
	@Override
	public boolean isAcceptable(JsonPath jsonPath, String method) {
		return !jsonPath.isCollection() && jsonPath instanceof ResourcePath && HttpMethod.GET.name().equals(method);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Passes the request to controller method.
	 */
	@Override
	public Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider, Document requestBody) {
		String resourcePath = jsonPath.getResourcePath();
		PathIds resourceIds = jsonPath.getIds();
		RegistryEntry registryEntry = getRegistryEntryByPath(resourcePath);
		logger.debug("using registry entry {}", registryEntry);

		String id = resourceIds.getIds().get(0);

		@SuppressWarnings("unchecked")
		Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry.getResourceInformation().getIdField().getType();
		Serializable castedId = context.getTypeParser().parse(id, idClass);
		ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository(parameterProvider);

		DocumentMappingConfig docummentMapperConfig = DocumentMappingConfig.create().setParameterProvider(parameterProvider);
		DocumentMapper documentMapper = context.getDocumentMapper();

		return resourceRepository.findOne(castedId, queryAdapter)
				.merge(it -> documentMapper.toDocument(it, queryAdapter, docummentMapperConfig))
				.map(this::toResponse);
	}

	public Response toResponse(Document document) {
		// return explicit { data : null } if values found
		if (!document.getData().isPresent()) {
			document.setData(Nullable.nullValue());
		}
		return new Response(document, 200);
	}
}
