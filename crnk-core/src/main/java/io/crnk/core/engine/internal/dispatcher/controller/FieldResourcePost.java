package io.crnk.core.engine.internal.dispatcher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.filter.ResourceRelationshipModificationType;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.dispatcher.path.FieldPath;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathIds;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates a new post in a similar manner as in {@link ResourcePost}, but additionally adds a relation to a field.
 */
public class FieldResourcePost extends ResourceUpsert {

	public FieldResourcePost(ResourceRegistry resourceRegistry, PropertiesProvider propertiesProvider, TypeParser typeParser, @SuppressWarnings
			("SameParameterValue") ObjectMapper objectMapper, DocumentMapper documentMapper,
							 List<ResourceModificationFilter> modificationFilters) {
		super(resourceRegistry, propertiesProvider, typeParser, objectMapper, documentMapper, modificationFilters);
	}

	@Override
	protected HttpMethod getHttpMethod() {
		return HttpMethod.POST;
	}

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String requestType) {
		PreconditionUtil.assertNotNull("path cannot be null", jsonPath);
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
		RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(resourceBody.getType());

		Serializable castedResourceId = getResourceId(resourceIds, endpointRegistryEntry);
		ResourceField relationshipField = endpointRegistryEntry.getResourceInformation()
				.findRelationshipFieldByName(jsonPath.getElementName());
		verifyFieldNotNull(relationshipField, jsonPath.getElementName());

		Class<?> baseRelationshipFieldClass = relationshipField.getType();

		RegistryEntry relationshipRegistryEntry = resourceRegistry.getEntry(relationshipField.getOppositeResourceType());
		String relationshipResourceType = relationshipField.getOppositeResourceType();

		verifyTypes(HttpMethod.POST, relationshipRegistryEntry, bodyRegistryEntry);

		Object newResource = buildNewResource(relationshipRegistryEntry, resourceBody, relationshipResourceType);
		setAttributes(resourceBody, newResource, relationshipRegistryEntry.getResourceInformation());
		ResourceRepositoryAdapter resourceRepository = relationshipRegistryEntry.getResourceRepository(parameterProvider);
		Document savedResourceResponse = documentMapper.toDocument(resourceRepository.create(newResource, queryAdapter), queryAdapter, parameterProvider);
		setRelations(newResource, bodyRegistryEntry, resourceBody, queryAdapter, parameterProvider, false);

		ResourceIdentifier resourceId1 = savedResourceResponse.getSingleData().get();

		RelationshipRepositoryAdapter relationshipRepositoryForClass = endpointRegistryEntry
				.getRelationshipRepositoryForType(relationshipField.getOppositeResourceType(), parameterProvider);

		@SuppressWarnings("unchecked")
		JsonApiResponse parent = endpointRegistryEntry.getResourceRepository(parameterProvider)
				.findOne(castedResourceId, queryAdapter);
		if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
			List<ResourceIdentifier> resourceIdList = Collections.singletonList(resourceId1);
			for (ResourceModificationFilter filter : modificationFilters) {
				resourceIdList = filter.modifyManyRelationship(parent.getEntity(), relationshipField, ResourceRelationshipModificationType.ADD, resourceIdList);
			}
			List<Serializable> parsedIds = new ArrayList<>();
			for (ResourceIdentifier resourceId : resourceIdList) {
				parsedIds.add(relationshipRegistryEntry.getResourceInformation().parseIdString(resourceId.getId()));
			}

			//noinspection unchecked
			relationshipRepositoryForClass.addRelations(parent.getEntity(), parsedIds, relationshipField, queryAdapter);
		} else {
			//noinspection unchecked
			for (ResourceModificationFilter filter : modificationFilters) {
				resourceId1 = filter.modifyOneRelationship(parent.getEntity(), relationshipField, resourceId1);
			}
			Serializable parseId = relationshipRegistryEntry.getResourceInformation().parseIdString(resourceId1.getId());

			relationshipRepositoryForClass.setRelation(parent.getEntity(), parseId, relationshipField, queryAdapter);
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

}
