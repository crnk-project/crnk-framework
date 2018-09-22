package io.crnk.core.engine.internal.dispatcher.controller;

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
import io.crnk.core.utils.Nullable;

import java.io.Serializable;

public class ResourceGetController extends ResourceIncludeField {

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
	public Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter, Document requestBody) {
		Serializable id = jsonPath.getId();

		RegistryEntry rootEntry = jsonPath.getRootEntry();
		ResourceRepositoryAdapter resourceRepository = rootEntry.getResourceRepository();

		DocumentMappingConfig docummentMapperConfig = DocumentMappingConfig.create();
		DocumentMapper documentMapper = context.getDocumentMapper();

		return resourceRepository.findOne(id, queryAdapter)
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
