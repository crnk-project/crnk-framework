package io.crnk.core.engine.internal.dispatcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.path.FieldPath;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathIds;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.Generics;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.ResourceFieldNotFoundException;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.io.Serializable;
import java.util.Collections;

/**
 * Creates a new post in a similar manner as in {@link ResourcePost}, but additionally adds a relation to a field.
 */
public class FieldResourcePost extends ResourceUpsert {

	public FieldResourcePost(ResourceRegistry resourceRegistry, PropertiesProvider propertiesProvider, TypeParser typeParser, @SuppressWarnings
			("SameParameterValue") ObjectMapper objectMapper, DocumentMapper documentMapper) {
		super(resourceRegistry, propertiesProvider, typeParser, objectMapper, documentMapper);
	}

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String requestType) {
		if (jsonPath == null) {
			throw new IllegalArgumentException();
		}
		return !jsonPath.isCollection()
				&& FieldPath.class.equals(jsonPath.getClass())
				&& HttpMethod.POST.name()
				.equals(requestType);
	}

	@Override
	public Response handle(JsonPath jsonPath, QueryAdapter queryAdapter,
						   RepositoryMethodParameterProvider parameterProvider, Document requestDocument) {


		RegistryEntry endpointRegistryEntry = getRegistryEntry(jsonPath);
		Resource resourceBody = getRequestBody(requestDocument, jsonPath, HttpMethod.POST);
		PathIds resourceIds = jsonPath.getIds();

		Serializable castedResourceId = getResourceId(resourceIds, endpointRegistryEntry);
		ResourceField relationshipField = endpointRegistryEntry.getResourceInformation()
				.findRelationshipFieldByName(jsonPath.getElementName());
		if (relationshipField == null) {
			throw new ResourceFieldNotFoundException(jsonPath.getElementName());
		}

		Class<?> baseRelationshipFieldClass = relationshipField.getType();
		Class<?> relationshipFieldClass = Generics
				.getResourceClass(relationshipField.getGenericType(), baseRelationshipFieldClass);

		RegistryEntry relationshipRegistryEntry = resourceRegistry.findEntry(relationshipFieldClass);
		String relationshipResourceType = relationshipField.getOppositeResourceType();

		Object resource = buildNewResource(relationshipRegistryEntry, resourceBody, relationshipResourceType);
		setAttributes(resourceBody, resource, relationshipRegistryEntry.getResourceInformation());
		ResourceRepositoryAdapter resourceRepository = relationshipRegistryEntry.getResourceRepository(parameterProvider);
		Document savedResourceResponse = documentMapper.toDocument(resourceRepository.create(resource, queryAdapter), queryAdapter, parameterProvider);
		saveRelations(queryAdapter, extractResource(savedResourceResponse), relationshipRegistryEntry, resourceBody, parameterProvider);

		Serializable resourceId = relationshipRegistryEntry.getResourceInformation().parseIdString(savedResourceResponse.getSingleData().get().getId());

		RelationshipRepositoryAdapter relationshipRepositoryForClass = endpointRegistryEntry
				.getRelationshipRepositoryForClass(relationshipFieldClass, parameterProvider);

		@SuppressWarnings("unchecked")
		JsonApiResponse parent = endpointRegistryEntry.getResourceRepository(parameterProvider)
				.findOne(castedResourceId, queryAdapter);
		if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
			//noinspection unchecked
			relationshipRepositoryForClass.addRelations(parent.getEntity(), Collections.singletonList(resourceId), relationshipField, queryAdapter);
		} else {
			//noinspection unchecked
			relationshipRepositoryForClass.setRelation(parent.getEntity(), resourceId, relationshipField, queryAdapter);
		}
		return new Response(savedResourceResponse, HttpStatus.CREATED_201);
	}

	private Serializable getResourceId(PathIds resourceIds, RegistryEntry registryEntry) {
		String resourceId = resourceIds.getIds()
				.get(0);
		@SuppressWarnings("unchecked")
		Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
				.getResourceInformation()
				.getIdField()
				.getType();
		return typeParser.parse(resourceId, idClass);
	}

	@Override
	protected boolean canModifyField(ResourceInformation resourceInformation, String fieldName, ResourceField field) {
		// allow dynamic field where field == null
		return field == null || field.getAccess().isPostable();
	}
}
