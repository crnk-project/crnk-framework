package io.crnk.core.engine.internal.dispatcher.controller;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.RelationshipsPath;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.result.Result;
import io.crnk.core.exception.RequestBodyException;
import io.crnk.core.repository.response.JsonApiResponse;

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
	 * @param resource source resource
	 * @param targetResourceInformation {@link ResourceInformation} of the relationship target.
	 * @param resourceField field
	 * @param dataBodies Data bodies with relationships
	 * @param queryAdapter QueryAdapter
	 * @param relationshipRepositoryForClass Relationship repository
	 */
	protected abstract Result processToManyRelationship(Result<Object> resource, ResourceInformation targetResourceInformation,
			ResourceField resourceField, Collection<ResourceIdentifier> dataBodies, QueryAdapter queryAdapter,
			RelationshipRepositoryAdapter relationshipRepositoryForClass);

	/**
	 * Processes To-One field
	 *
	 * @param resource source resource
	 * @param targetResourceInformation {@link ResourceInformation} of the relationship target.
	 * @param resourceField field
	 * @param dataBody Data body with a relationship
	 * @param queryAdapter QueryAdapter
	 * @param relationshipRepositoryForClass Relationship repository
	 */
	protected abstract Result processToOneRelationship(Result<Object> resource, ResourceInformation targetResourceInformation,
			ResourceField resourceField, ResourceIdentifier dataBody, QueryAdapter queryAdapter,
			RelationshipRepositoryAdapter relationshipRepositoryForClass);

	@Override
	public final boolean isAcceptable(JsonPath jsonPath, String method) {
		return !jsonPath.isCollection()
				&& RelationshipsPath.class.equals(jsonPath.getClass())
				&& method().name().equals(method);
	}

	@Override
	public final Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter, Document requestBody) {
		RelationshipsPath relationshipsPath = (RelationshipsPath) jsonPath;
		RegistryEntry registryEntry = relationshipsPath.getRootEntry();
		logger.debug("using registry entry {}", registryEntry);

		HttpMethod method = method();
		QueryContext queryContext = queryAdapter.getQueryContext();

		String resourceType = registryEntry.getResourceInformation().getResourceType();
		assertRequestDocument(requestBody, method, resourceType);

		Serializable resourceId = jsonPath.getId();
		ResourceField relationshipField = relationshipsPath.getRelationship();

		// early security check, not strictly necessary as repository adapter access is checked as well
		ResourceFilterDirectory filterDirectory = context.getResourceFilterDirectory();
		boolean canAccess = filterDirectory.canAccess(relationshipField, method, queryContext, false);
		PreconditionUtil.verify(canAccess, "should be able to access or have an exception earlier, ignoring does not make sense here");

		ResourceRepositoryAdapter resourceRepository = registryEntry.getResourceRepository();
		ResourceInformation targetInformation = getRegistryEntry(relationshipField.getOppositeResourceType()).getResourceInformation();
		RelationshipRepositoryAdapter relationshipRepositoryForClass = registryEntry.getRelationshipRepository(relationshipField);

		Result<Object> resource = resourceRepository.findOne(resourceId, queryAdapter).map(JsonApiResponse::getEntity);

		Result result;
		if (Collection.class.isAssignableFrom(relationshipField.getType())) {
			Collection<ResourceIdentifier> dataBodies =
					(Collection<ResourceIdentifier>) (requestBody.isMultiple() ? requestBody.getData().get() : Collections.singletonList(requestBody.getData().get()));
			result = processToManyRelationship(resource, targetInformation, relationshipField, dataBodies, queryAdapter,
					relationshipRepositoryForClass);
		}
		else {
			if (requestBody.isMultiple()) {
				throw new RequestBodyException(method, resourceType, "Multiple data in body");
			}
			ResourceIdentifier dataBody = (ResourceIdentifier) requestBody.getData().get();
			result = processToOneRelationship(resource, targetInformation, relationshipField, dataBody, queryAdapter,
					relationshipRepositoryForClass);
		}

		Document document = null;
		int status = getStatus(document, method);
		return result.map(it -> new Response(document, status));
	}
}
