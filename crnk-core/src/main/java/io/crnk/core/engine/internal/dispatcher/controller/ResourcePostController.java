package io.crnk.core.engine.internal.dispatcher.controller;

import java.util.Set;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.http.HttpMethod;
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

public class ResourcePostController extends ResourceUpsert {

	@Override
	protected HttpMethod getHttpMethod() {
		return HttpMethod.POST;
	}

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String method) {
		// singular nested resources can also be POSTed, sole exception next to typical collections
		boolean nestedOne = jsonPath.getParentField() != null && !jsonPath.getParentField().isCollection();

		return (jsonPath.isCollection() || nestedOne) && jsonPath instanceof ResourcePath && HttpMethod.POST.name().equals(method);
	}

	@Override
	public Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter, Document requestDocument) {
		RegistryEntry endpointRegistryEntry = jsonPath.getRootEntry();
		Resource requestResource = getRequestBody(requestDocument, jsonPath, HttpMethod.POST);
		RegistryEntry registryEntry = getRegistryEntry(requestResource.getType());
		logger.debug("using registry entry {}", registryEntry);
		ResourceInformation resourceInformation = registryEntry.getResourceInformation();
		verifyTypes(HttpMethod.POST, endpointRegistryEntry, registryEntry);

		ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository();

		Set<String> loadedRelationshipNames = getLoadedRelationshipNames(requestResource);

		QueryContext queryContext = queryAdapter.getQueryContext();
		Result<JsonApiResponse> response;
		if (Resource.class.equals(resourceInformation.getImplementationClass())) {
			response = resourceRepository.create(requestResource, queryAdapter);
		}
		else {
			Object entity = newEntity(resourceInformation, requestResource);
			setId(requestResource, entity, resourceInformation);
			setType(requestResource, entity);
			setAttributes(requestResource, entity, resourceInformation, queryContext);
			setMeta(requestResource, entity, resourceInformation);
			setLinks(requestResource, entity, resourceInformation);
			Result zipped =
					setRelationsAsync(entity, registryEntry, requestResource, queryAdapter, false);
			response = zipped.merge(it -> resourceRepository.create(entity, queryAdapter));
		}

		DocumentMappingConfig mappingConfig = DocumentMappingConfig.create()
				.setFieldsWithEnforcedIdSerialization(loadedRelationshipNames);
		DocumentMapper documentMapper = this.context.getDocumentMapper();

		return response
				.merge(it -> documentMapper.toDocument(it, queryAdapter, mappingConfig))
				.map(this::toResponse);
	}

	private Response toResponse(Document responseDocument) {
		int status = getStatus(responseDocument, HttpMethod.POST);
		Response response = new Response(responseDocument, status);

		validateCreatedResponse(response);

		logger.debug("set response {}", response);
		return response;
	}

}
