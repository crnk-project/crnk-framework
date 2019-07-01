package io.crnk.core.engine.internal.dispatcher.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.filter.ResourceRelationshipModificationType;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.path.FieldPath;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.document.mapper.DocumentMappingConfig;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.repository.response.JsonApiResponse;

/**
 * Creates a new post in a similar manner as in {@link ResourcePostController}, but additionally adds a relation to a field.
 */
public class FieldResourcePost extends ResourceUpsert {

	@Override
	protected HttpMethod getHttpMethod() {
		return HttpMethod.POST;
	}

	@Override
	public boolean isAcceptable(JsonPath jsonPath, String method) {
		return !jsonPath.isCollection()
				&& FieldPath.class.equals(jsonPath.getClass())
				&& HttpMethod.POST.name()
				.equals(method);
	}

	@Override
	public Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter, Document requestDocument) {

		FieldPath fieldPath = (FieldPath) jsonPath;
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		RegistryEntry registryEntry = jsonPath.getRootEntry();
		logger.debug("using registry entry {}", registryEntry);
		Resource resourceBody = getRequestBody(requestDocument, jsonPath, HttpMethod.POST);
		RegistryEntry bodyRegistryEntry = resourceRegistry.getEntry(resourceBody.getType());
		ResourceInformation bodyResourceInformation = bodyRegistryEntry.getResourceInformation();


		Serializable id = jsonPath.getId();
		ResourceField relationshipField = fieldPath.getField();

		RegistryEntry relationshipRegistryEntry = resourceRegistry.getEntry(relationshipField.getOppositeResourceType());
		ResourceInformation relationshipResourceInformation = relationshipRegistryEntry.getResourceInformation();
		ResourceRepositoryAdapter resourceRepository = relationshipRegistryEntry.getResourceRepository();
		String relationshipResourceType = relationshipField.getOppositeResourceType();
		verifyTypes(HttpMethod.POST, relationshipRegistryEntry, bodyRegistryEntry);

		DocumentMappingConfig mappingConfig = DocumentMappingConfig.create();
		DocumentMapper documentMapper = context.getDocumentMapper();

		QueryContext queryContext = queryAdapter.getQueryContext();
		Object entity = buildNewResource(relationshipRegistryEntry, resourceBody, relationshipResourceType);

		setId(resourceBody, entity, relationshipResourceInformation);
		setAttributes(resourceBody, entity, relationshipResourceInformation, queryContext);
		setMeta(resourceBody, entity, relationshipResourceInformation);
		setLinks(resourceBody, entity, relationshipResourceInformation);

		Result<JsonApiResponse> createdResource = setRelationsAsync(entity, bodyRegistryEntry, resourceBody, queryAdapter, false)
				.merge(it -> resourceRepository.create(entity, queryAdapter).doWork(created -> validateCreatedResponse(bodyResourceInformation, created)));

		Result<Document> createdDocument = createdResource.merge(it -> documentMapper.toDocument(it, queryAdapter, mappingConfig));

		if (relationshipResourceInformation.isNested()) {
			// nested resource repositories are assumed to handle attachment to parent by themeselves
			return createdDocument.map(this::toResponse);
		}
		else {
			Result<JsonApiResponse> parentResource = registryEntry.getResourceRepository().findOne(id, queryAdapter);
			return createdDocument.zipWith(parentResource,
					(created, parent) -> attachToParent(parent, registryEntry, relationshipField, created, queryAdapter))
					.merge(it -> it)
					.map(this::toResponse);
		}
	}


	public Response toResponse(Document document) {
		int status = getStatus(document, HttpMethod.POST);
		return new Response(document, status);
	}

	private Result<Document> attachToParent(JsonApiResponse parent, RegistryEntry endpointRegistryEntry, ResourceField relationshipField, Document createdDocument, QueryAdapter queryAdapter) {
		ResourceIdentifier resourceId1 = createdDocument.getSingleData().get();

		RelationshipRepositoryAdapter relationshipRepositoryForClass = endpointRegistryEntry.getRelationshipRepository(relationshipField);

		Class<?> baseRelationshipFieldClass = relationshipField.getType();
		ResourceRegistry resourceRegistry = context.getResourceRegistry();
		RegistryEntry relationshipRegistryEntry = resourceRegistry.getEntry(relationshipField.getOppositeResourceType());

		List<ResourceModificationFilter> modificationFilters = context.getModificationFilters();
		Result<JsonApiResponse> result;
		if (Iterable.class.isAssignableFrom(baseRelationshipFieldClass)) {
			List<ResourceIdentifier> resourceIdList = Collections.singletonList(resourceId1);
			for (ResourceModificationFilter filter : modificationFilters) {
				resourceIdList = filter.modifyManyRelationship(parent.getEntity(), relationshipField,
						ResourceRelationshipModificationType.ADD, resourceIdList);
			}
			List<Serializable> parsedIds = new ArrayList<>();
			for (ResourceIdentifier resourceId : resourceIdList) {
				parsedIds.add(relationshipRegistryEntry.getResourceInformation().parseIdString(resourceId.getId()));
			}

			//noinspection unchecked
			result = relationshipRepositoryForClass.addRelations(parent.getEntity(), parsedIds, relationshipField, queryAdapter);
		}
		else {
			//noinspection unchecked
			for (ResourceModificationFilter filter : modificationFilters) {
				resourceId1 = filter.modifyOneRelationship(parent.getEntity(), relationshipField, resourceId1);
			}
			Serializable parseId = relationshipRegistryEntry.getResourceInformation().parseIdString(resourceId1.getId());

			result = relationshipRepositoryForClass.setRelation(parent.getEntity(), parseId, relationshipField, queryAdapter);
		}
		return result.map((it) -> createdDocument);
	}


}
