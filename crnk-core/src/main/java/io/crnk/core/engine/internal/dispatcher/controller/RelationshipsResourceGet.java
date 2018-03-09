package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathIds;
import io.crnk.core.engine.internal.dispatcher.path.RelationshipsPath;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.io.Serializable;

public class RelationshipsResourceGet extends ResourceIncludeField {

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String requestType) {
		return !jsonPath.isCollection() && jsonPath instanceof RelationshipsPath && HttpMethod.GET.name().equals(requestType);
	}

	@Override
	public Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider, Document requestBody) {
		String resourceName = jsonPath.getResourceType();
		PathIds resourceIds = jsonPath.getIds();
		RegistryEntry registryEntry = context.getResourceRegistry().getEntry(resourceName);

		Serializable castedResourceId = getResourceId(resourceIds, registryEntry);
		String elementName = jsonPath.getElementName();
		ResourceField relationshipField = registryEntry.getResourceInformation().findRelationshipFieldByName(elementName);
		verifyFieldNotNull(relationshipField, elementName);

		DocumentMappingConfig documentMapperConfig = DocumentMappingConfig.create().setParameterProvider(parameterProvider);
		DocumentMapper documentMapper = context.getDocumentMapper();

		boolean isCollection = Iterable.class.isAssignableFrom(relationshipField.getType());
		RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry.getRelationshipRepository(relationshipField, parameterProvider);
		Result<JsonApiResponse> response;
		if (isCollection) {
			response = relationshipRepositoryForClass.findManyTargets(castedResourceId, relationshipField, queryAdapter);
		} else {
			response = relationshipRepositoryForClass.findOneTarget(castedResourceId, relationshipField, queryAdapter);
		}
		return response.merge(it -> documentMapper.toDocument(it, queryAdapter, documentMapperConfig)).map(this::toResponse);
	}


	public Response toResponse(Document document) {
		// return explicit { data : null } if values found
		if (!document.getData().isPresent()) {
			document.setData(Nullable.nullValue());
		}
		return new Response(document, 200);
	}


	private Serializable getResourceId(PathIds resourceIds, RegistryEntry registryEntry) {
		String resourceId = resourceIds.getIds().get(0);
		@SuppressWarnings("unchecked")
		Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry.getResourceInformation().getIdField().getType();
		TypeParser typeParser = context.getTypeParser();
		return typeParser.parse(resourceId, idClass);
	}
}
