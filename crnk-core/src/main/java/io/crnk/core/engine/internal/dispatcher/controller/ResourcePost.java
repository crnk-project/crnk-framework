package io.crnk.core.engine.internal.dispatcher.controller;

import java.util.Set;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

public class ResourcePost extends ResourceUpsert {

	@Override
	protected HttpMethod getHttpMethod() {
		return HttpMethod.POST;
	}

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String method) {
		return jsonPath.isCollection() && jsonPath instanceof ResourcePath && HttpMethod.POST.name().equals(method);
	}

	@Override
	public Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter,
										RepositoryMethodParameterProvider parameterProvider, Document requestDocument) {

		RegistryEntry endpointRegistryEntry = getRegistryEntry(jsonPath);
		Resource requestResource = getRequestBody(requestDocument, jsonPath, HttpMethod.POST);
		RegistryEntry registryEntry = context.getResourceRegistry().getEntry(requestResource.getType());
		logger.debug("using registry entry {}", registryEntry);
		ResourceInformation resourceInformation = registryEntry.getResourceInformation();
		verifyTypes(HttpMethod.POST, endpointRegistryEntry, registryEntry);

		ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository(parameterProvider);

		Set<String> loadedRelationshipNames = getLoadedRelationshipNames(requestResource);

		QueryContext queryContext = queryAdapter.getQueryContext();
		Result<JsonApiResponse> response;
		if (Resource.class.equals(resourceInformation.getResourceClass())) {
			response = resourceRepository.create(requestResource, queryAdapter);
		} else {
			Object newResource = newResource(registryEntry.getResourceInformation(), requestResource);
			setId(requestResource, newResource, registryEntry.getResourceInformation());
			setAttributes(requestResource, newResource, registryEntry.getResourceInformation(), queryContext);
			Result zipped = setRelationsAsync(newResource, registryEntry, requestResource, queryAdapter, parameterProvider, false);
			response = zipped.merge(it -> resourceRepository.create(newResource, queryAdapter));
		}

		DocumentMappingConfig mappingConfig = DocumentMappingConfig.create()
				.setParameterProvider(parameterProvider)
				.setFieldsWithEnforcedIdSerialization(loadedRelationshipNames);
		DocumentMapper documentMapper = this.context.getDocumentMapper();

		return response.doWork(it -> validateResponse(resourceInformation, it))
				.merge(it -> documentMapper.toDocument(it, queryAdapter, mappingConfig))
				.map(this::toResponse);
	}

	private Response toResponse(Document document) {
		Response response = new Response(document, HttpStatus.CREATED_201);
		logger.debug("set response {}", response);
		return response;
	}

	private void validateResponse(ResourceInformation resourceInformation, JsonApiResponse response) {
		Object entity = response.getEntity();
		logger.debug("posted resource {}", entity);
		if (entity == null) {
			throw new IllegalStateException("repository did not return the created resource");
		}

		Object id = resourceInformation.getId(entity);
		if (id == null) {
			throw new IllegalStateException("created resource must have an id");
		}
	}

}
