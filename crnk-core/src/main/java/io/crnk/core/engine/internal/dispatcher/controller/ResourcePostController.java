package io.crnk.core.engine.internal.dispatcher.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import io.crnk.core.engine.result.ResultFactory;
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
		List<Resource> resourceBodies = getRequestBodys(requestDocument, jsonPath, HttpMethod.POST);
		ResultFactory resultFactory = context.getResultFactory();

		List<Result<Object>> entityResults = new ArrayList<>();
		Set<String> loadedRelationshipNames = new HashSet<>();
		for (Resource resourceBody : resourceBodies) {
			RegistryEntry registryEntry = getRegistryEntry(resourceBody.getType());
			logger.debug("using registry entry {}", registryEntry);
			ResourceInformation resourceInformation = registryEntry.getResourceInformation();
			verifyTypes(HttpMethod.POST, endpointRegistryEntry, registryEntry);

			loadedRelationshipNames.addAll(getLoadedRelationshipNames(resourceBody));

			QueryContext queryContext = queryAdapter.getQueryContext();
			if (Resource.class.equals(resourceInformation.getImplementationClass())) {
				entityResults.add(resultFactory.just(resourceBody));
			}
			else {
				Object entity = newEntity(resourceInformation, resourceBody);
				setId(resourceBody, entity, resourceInformation);
				setType(resourceBody, entity);
				setAttributes(resourceBody, entity, resourceInformation, queryContext);
				setMeta(resourceBody, entity, resourceInformation);
				setLinks(resourceBody, entity, resourceInformation);
				Result zipped = setRelationsAsync(entity, registryEntry, resourceBody, queryAdapter, false);
				entityResults.add(zipped.map(it -> entity));
			}
		}

		ResourceRepositoryAdapter resourceRepository = endpointRegistryEntry.getResourceRepository();
		Result<List<Object>> result = resultFactory.all(entityResults);

		Result<JsonApiResponse> response = result.merge(entities -> resourceRepository.create(collectionOrSingleton(entities), queryAdapter));
		DocumentMappingConfig mappingConfig = context.getMappingConfig().clone()
				.setFieldsWithEnforcedIdSerialization(loadedRelationshipNames);
		DocumentMapper documentMapper = this.context.getDocumentMapper();

		return response
				.merge(it -> documentMapper.toDocument(it, queryAdapter, mappingConfig))
				.map(this::toResponse);
	}

	private Object collectionOrSingleton(List<Object> entities) {
		return entities.size() == 1 ? entities.get(0) : entities;
	}

	private Response toResponse(Document responseDocument) {
		int status = getStatus(responseDocument, HttpMethod.POST);
		Response response = new Response(responseDocument, status);

		validateCreatedResponse(response);

		logger.debug("set response {}", response);
		return response;
	}

}
