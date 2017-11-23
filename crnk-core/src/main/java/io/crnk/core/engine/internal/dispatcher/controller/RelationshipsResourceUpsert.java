package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathIds;
import io.crnk.core.engine.internal.dispatcher.path.RelationshipsPath;
import io.crnk.core.engine.internal.document.mapper.DocumentMapper;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.RequestBodyException;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public abstract class RelationshipsResourceUpsert extends ResourceIncludeField {

	protected final List<ResourceModificationFilter> modificationFilters;

	RelationshipsResourceUpsert(ResourceRegistry resourceRegistry, TypeParser typeParser, DocumentMapper documentMapper, List<ResourceModificationFilter> modificationFilters) {
		super(resourceRegistry, typeParser, documentMapper);
		this.modificationFilters = modificationFilters;
	}

	/**
	 * HTTP method name
	 *
	 * @return HTTP method name
	 */
	protected abstract HttpMethod method();

	/**
	 * Processes To-Many field
	 *
	 * @param resource                       source resource
	 * @param relationshipIdType             {@link Class} class of the relationship's id field
	 * @param elementName                    field's name
	 * @param dataBodies                     Data bodies with relationships
	 * @param queryAdapter                   QueryAdapter
	 * @param relationshipRepositoryForClass Relationship repository
	 */
	protected abstract void processToManyRelationship(Object resource, ResourceInformation targetResourceInformation,
													  ResourceField resourceField, Iterable<ResourceIdentifier> dataBodies, QueryAdapter queryAdapter,
													  RelationshipRepositoryAdapter relationshipRepositoryForClass);

	/**
	 * Processes To-One field
	 *
	 * @param resource                       source resource
	 * @param relationshipIdType             {@link Class} class of the relationship's id field
	 * @param elementName                    field's name
	 * @param dataBody                       Data body with a relationship
	 * @param queryAdapter                   QueryAdapter
	 * @param relationshipRepositoryForClass Relationship repository
	 */
	protected abstract void processToOneRelationship(Object resource, ResourceInformation targetResourceInformation,
													 ResourceField resourceField, ResourceIdentifier dataBody, QueryAdapter queryAdapter,
													 RelationshipRepositoryAdapter relationshipRepositoryForClass);

	@Override
	public final boolean isAcceptable(JsonPath jsonPath, String requestType) {
		PreconditionUtil.assertNotNull("jsonPath cannot be null", jsonPath);
		return !jsonPath.isCollection()
				&& RelationshipsPath.class.equals(jsonPath.getClass())
				&& method().name().equals(requestType);
	}

	@Override
	public final Response handle(JsonPath jsonPath, QueryAdapter queryAdapter,
								 RepositoryMethodParameterProvider parameterProvider, Document requestBody) {
		String resourceName = jsonPath.getResourceType();
		PathIds resourceIds = jsonPath.getIds();
		RegistryEntry registryEntry = getRegistryEntry(resourceName);

		assertRequestDocument(requestBody, HttpMethod.POST, resourceName);

		Serializable castedResourceId = getResourceId(resourceIds, registryEntry);
		ResourceField relationshipField = registryEntry.getResourceInformation().findRelationshipFieldByName(jsonPath
				.getElementName());
		verifyFieldNotNull(relationshipField, jsonPath.getElementName());
		ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository(parameterProvider);
		@SuppressWarnings("unchecked")
		Object resource = resourceRepository.findOne(castedResourceId, queryAdapter).getEntity();

		ResourceInformation targetInformation = getRegistryEntry(relationshipField.getOppositeResourceType()).getResourceInformation();


		@SuppressWarnings("unchecked")
		RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry
				.getRelationshipRepositoryForType(relationshipField.getOppositeResourceType(), parameterProvider);
		if (Iterable.class.isAssignableFrom(relationshipField.getType())) {
			Iterable<ResourceIdentifier> dataBodies = (Iterable<ResourceIdentifier>) (requestBody.isMultiple() ? requestBody.getData().get() : Collections.singletonList(requestBody.getData().get()));
			processToManyRelationship(resource, targetInformation, relationshipField, dataBodies, queryAdapter,
					relationshipRepositoryForClass);
		} else {
			if (requestBody.isMultiple()) {
				throw new RequestBodyException(HttpMethod.POST, resourceName, "Multiple data in body");
			}
			ResourceIdentifier dataBody = (ResourceIdentifier) requestBody.getData().get();
			processToOneRelationship(resource, targetInformation, relationshipField, dataBody, queryAdapter,
					relationshipRepositoryForClass);
		}

		return new Response(new Document(), HttpStatus.NO_CONTENT_204);
	}


	private Serializable getResourceId(PathIds resourceIds, RegistryEntry registryEntry) {
		String resourceId = resourceIds.getIds().get(0);
		@SuppressWarnings("unchecked") Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
				.getResourceInformation()
				.getIdField()
				.getType();
		return typeParser.parse(resourceId, idClass);
	}
}
