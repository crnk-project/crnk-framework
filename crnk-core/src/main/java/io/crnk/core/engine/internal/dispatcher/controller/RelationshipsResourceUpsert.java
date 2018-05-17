package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.PathIds;
import io.crnk.core.engine.internal.dispatcher.path.RelationshipsPath;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.exception.RepositoryNotFoundException;
import io.crnk.core.exception.RequestBodyException;
import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.io.Serializable;
import java.util.Collections;

public abstract class RelationshipsResourceUpsert extends ResourceIncludeField {

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
	 * @param targetResourceInformation      {@link ResourceInformation} of the relationship target.
	 * @param resourceField                  field
	 * @param dataBodies                     Data bodies with relationships
	 * @param queryAdapter                   QueryAdapter
	 * @param relationshipRepositoryForClass Relationship repository
	 */
	protected abstract Result processToManyRelationship(Result<Object> resource, ResourceInformation targetResourceInformation,
														ResourceField resourceField, Iterable<ResourceIdentifier> dataBodies, QueryAdapter queryAdapter,
														RelationshipRepositoryAdapter relationshipRepositoryForClass);

	/**
	 * Processes To-One field
	 *
	 * @param resource                       source resource
	 * @param targetResourceInformation      {@link ResourceInformation} of the relationship target.
	 * @param resourceField                  field
	 * @param dataBody                       Data body with a relationship
	 * @param queryAdapter                   QueryAdapter
	 * @param relationshipRepositoryForClass Relationship repository
	 */
	protected abstract Result processToOneRelationship(Result<Object> resource, ResourceInformation targetResourceInformation,
													   ResourceField resourceField, ResourceIdentifier dataBody, QueryAdapter queryAdapter,
													   RelationshipRepositoryAdapter relationshipRepositoryForClass);

	@Override
	public final boolean isAcceptable(JsonPath jsonPath, String method) {
		PreconditionUtil.assertNotNull("jsonPath cannot be null", jsonPath);
		return !jsonPath.isCollection()
				&& RelationshipsPath.class.equals(jsonPath.getClass())
				&& method().name().equals(method);
	}

	@Override
	public final Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter,
											  RepositoryMethodParameterProvider parameterProvider, Document requestBody) {
		String resourceName = jsonPath.getResourceType();
		PathIds strResourceIds = jsonPath.getIds();
		io.crnk.core.utils.Optional<RegistryEntry> optionalRegistryEntry = getRegistryEntry(resourceName);
		if (!optionalRegistryEntry.isPresent()) {
			throw new RepositoryNotFoundException(resourceName);
		}

		RegistryEntry registryEntry = optionalRegistryEntry.get();
		logger.debug("using registry entry {}", registryEntry);

		assertRequestDocument(requestBody, HttpMethod.POST, resourceName);

		Serializable resourceId = getResourceId(strResourceIds, registryEntry);
		ResourceField relationshipField = registryEntry.getResourceInformation().findRelationshipFieldByName(jsonPath
				.getElementName());
		verifyFieldNotNull(relationshipField, jsonPath.getElementName());
		ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository(parameterProvider);
		ResourceInformation targetInformation = getRegistryEntry(relationshipField.getOppositeResourceType()).get().getResourceInformation();
		RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry.getRelationshipRepository(relationshipField, parameterProvider);

		Result<Object> resource = resourceRepository.findOne(resourceId, queryAdapter).map(JsonApiResponse::getEntity);

		Result result;
		if (Iterable.class.isAssignableFrom(relationshipField.getType())) {
			Iterable<ResourceIdentifier> dataBodies = (Iterable<ResourceIdentifier>) (requestBody.isMultiple() ? requestBody.getData().get() : Collections.singletonList(requestBody.getData().get()));
			result = processToManyRelationship(resource, targetInformation, relationshipField, dataBodies, queryAdapter,
					relationshipRepositoryForClass);
		} else {
			if (requestBody.isMultiple()) {
				throw new RequestBodyException(HttpMethod.POST, resourceName, "Multiple data in body");
			}
			ResourceIdentifier dataBody = (ResourceIdentifier) requestBody.getData().get();
			result = processToOneRelationship(resource, targetInformation, relationshipField, dataBody, queryAdapter,
					relationshipRepositoryForClass);
		}

		return result.map(it -> new Response(new Document(), HttpStatus.NO_CONTENT_204));
	}


	private Serializable getResourceId(PathIds resourceIds, RegistryEntry registryEntry) {
		String resourceId = resourceIds.getIds().get(0);
		@SuppressWarnings("unchecked") Class<? extends Serializable> idClass = (Class<? extends Serializable>) registryEntry
				.getResourceInformation()
				.getIdField()
				.getType();
		TypeParser typeParser = context.getTypeParser();
		return typeParser.parse(resourceId, idClass);
	}
}
