package io.crnk.core.engine.internal.dispatcher.controller;

import java.io.Serializable;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathIds;
import io.crnk.core.engine.internal.dispatcher.path.RelationshipsPath;
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

public class RelationshipsResourceGet extends ResourceIncludeField {

	public RelationshipsResourceGet(ResourceRegistry resourceRegistry, TypeParser typeParser, DocumentMapper documentMapper) {
		super(resourceRegistry, typeParser, documentMapper);
	}

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String requestType) {
		return !jsonPath.isCollection() && jsonPath instanceof RelationshipsPath && HttpMethod.GET.name().equals(requestType);
	}

	@Override
	public Response handle(JsonPath jsonPath, QueryAdapter queryAdapter, RepositoryMethodParameterProvider parameterProvider, Document requestBody) {
		String resourceName = jsonPath.getResourceType();
		PathIds resourceIds = jsonPath.getIds();
		RegistryEntry registryEntry = resourceRegistry.getEntry(resourceName);

		Serializable castedResourceId = getResourceId(resourceIds, registryEntry);
		String elementName = jsonPath.getElementName();
		ResourceField relationshipField = registryEntry.getResourceInformation().findRelationshipFieldByName(elementName);
		if (relationshipField == null) {
			throw new ResourceFieldNotFoundException(elementName);
		}

		boolean isCollection = Iterable.class.isAssignableFrom(relationshipField.getType());


		RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry.getRelationshipRepositoryForType(relationshipField.getOppositeResourceType(), parameterProvider);
		JsonApiResponse entities;
		if (isCollection) {
			entities = relationshipRepositoryForClass.findManyTargets(castedResourceId, relationshipField, queryAdapter);
		} else {
			entities = relationshipRepositoryForClass.findOneTarget(castedResourceId, relationshipField, queryAdapter);
		}
		Document responseDocument = documentMapper.toDocument(entities, queryAdapter, parameterProvider);

		// FIXME related vs self

		// return explicit { data : null } if values found
		if (!responseDocument.getData().isPresent()) {
			responseDocument.setData(Nullable.nullValue());
		}

		return new Response(responseDocument, 200);
	}

	private Serializable getResourceId(PathIds resourceIds, RegistryEntry registryEntry) {
		String resourceId = resourceIds.getIds().get(0);
		@SuppressWarnings("unchecked")
		Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry.getResourceInformation().getIdField().getType();
		return typeParser.parse(resourceId, idClass);
	}
}
