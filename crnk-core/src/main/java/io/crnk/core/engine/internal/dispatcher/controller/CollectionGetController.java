package io.crnk.core.engine.internal.dispatcher.controller;

import java.util.Collection;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.utils.Nullable;

public class CollectionGetController extends ResourceIncludeField {

	/**
	 * Check if it is a GET request for a collection of resources.
	 */
	@Override
	public boolean isAcceptable(JsonPath jsonPath, String method) {
		return jsonPath.isCollection()
				&& jsonPath instanceof ResourcePath
				&& HttpMethod.GET.name().equals(method);
	}

	@Override
	public Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter, Document requestBody) {
		RegistryEntry registryEntry = jsonPath.getRootEntry();

		DocumentMappingConfig mappingConfig = context.getMappingConfig();
		DocumentMapper documentMapper = context.getDocumentMapper();

		ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository();
		Result<JsonApiResponse> response;
		if (jsonPath.getIds() == null || jsonPath.getIds().isEmpty()) {
			logger.debug("finding {}", queryAdapter);
			response = resourceRepository.findAll(queryAdapter);
		} else {
			Collection<? > parsedIds = jsonPath.getIds();
			logger.debug("finding {} with ids {}", queryAdapter, parsedIds);
			response = resourceRepository.findAll(parsedIds, queryAdapter);
		}

		return response.merge(it -> documentMapper.toDocument(it, queryAdapter, mappingConfig)).map(this::toResponse);
	}


	public Response toResponse(Document document) {
		// return explicit { data : null } if values found
		if (!document.getData().isPresent()) {
			document.setData(Nullable.nullValue());
		}
		logger.debug("mapping {} to response");
		int status = getStatus(document, HttpMethod.GET);
		return new Response(document, status);
	}
}
