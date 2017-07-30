package io.crnk.core.engine.internal.dispatcher.controller;

import java.io.Serializable;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.dispatcher.path.FieldPath;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathIds;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.ResourceFieldNotFoundException;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

public class FieldResourceGet extends ResourceIncludeField {

	public FieldResourceGet(ResourceRegistry resourceRegistry, TypeParser typeParser, DocumentMapper documentMapper) {
		super(resourceRegistry, typeParser, documentMapper);
	}

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String requestType) {
		return !jsonPath.isCollection()
				&& FieldPath.class.equals(jsonPath.getClass())
				&& HttpMethod.GET.name().equals(requestType);
	}

	@Override
	public Response handle(JsonPath jsonPath, QueryAdapter queryAdapter, RepositoryMethodParameterProvider
			parameterProvider, Document requestBody) {
		PathIds resourceIds = jsonPath.getIds();
		String resourceName = jsonPath.getResourceType();
		String elementName = jsonPath.getElementName();

		RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);
		Serializable castedResourceId = getResourceId(resourceIds, registryEntry);
		ResourceField relationshipField = registryEntry.getResourceInformation().findRelationshipFieldByName(elementName);
		verifyFieldNotNull(relationshipField, elementName);

		// TODO remove Class usage and replace by resourceId
		Class<?> baseRelationshipFieldClass = relationshipField.getType();

		RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry
				.getRelationshipRepositoryForType(relationshipField.getOppositeResourceType(), parameterProvider);
		JsonApiResponse entities;
		if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
			entities = relationshipRepositoryForClass.findManyTargets(castedResourceId, relationshipField, queryAdapter);
		} else {
			entities = relationshipRepositoryForClass.findOneTarget(castedResourceId, relationshipField, queryAdapter);
		}
		Document responseDocument = documentMapper.toDocument(entities, queryAdapter, parameterProvider);

		// return explicit { data : null } if values found
		if (!responseDocument.getData().isPresent()) {
			responseDocument.setData(Nullable.nullValue());
		}

		return new Response(responseDocument, 200);
	}

	private Serializable getResourceId(PathIds resourceIds, RegistryEntry registryEntry) {
		String resourceId = resourceIds.getIds().get(0);
		@SuppressWarnings("unchecked")
		Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
				.getResourceInformation()
				.getIdField()
				.getType();
		return typeParser.parse(resourceId, idClass);
	}
}
