package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.filter.ResourceRelationshipModificationType;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RelationshipsResourcePatch extends RelationshipsResourceUpsert {

	public RelationshipsResourcePatch(ResourceRegistry resourceRegistry, TypeParser typeParser, List<ResourceModificationFilter> modificationFilters) {
		super(resourceRegistry, typeParser, null, modificationFilters);
	}

	@Override
	public HttpMethod method() {
		return HttpMethod.PATCH;
	}

	@Override
	public void processToManyRelationship(Object resource, ResourceInformation targetResourceInformation,
										  ResourceField relationshipField, Iterable<ResourceIdentifier> dataBodies, QueryAdapter queryAdapter,
										  RelationshipRepositoryAdapter relationshipRepositoryForClass) {

		List<ResourceIdentifier> resourceIds = new ArrayList<>();
		for (ResourceIdentifier dataBody : dataBodies) {
			resourceIds.add(dataBody);
		}
		for (ResourceModificationFilter filter : modificationFilters) {
			resourceIds = filter.modifyManyRelationship(resource, relationshipField, ResourceRelationshipModificationType.SET, resourceIds);
		}

		List<Serializable> parsedIds = new LinkedList<>();
		for (ResourceIdentifier resourceId : resourceIds) {
			Serializable parsedId = targetResourceInformation.parseIdString(resourceId.getId());
			parsedIds.add(parsedId);
		}
		//noinspection unchecked
		relationshipRepositoryForClass.setRelations(resource, parsedIds, relationshipField, queryAdapter);
	}

	@Override
	protected void processToOneRelationship(Object resource, ResourceInformation targetResourceInformation,
											ResourceField relationshipField, ResourceIdentifier resourceId, QueryAdapter queryAdapter,
											RelationshipRepositoryAdapter relationshipRepositoryForClass) {

		for (ResourceModificationFilter filter : modificationFilters) {
			resourceId = filter.modifyOneRelationship(resource, relationshipField, resourceId);
		}

		Serializable parsedId = null;
		if (resourceId != null) {
			parsedId = targetResourceInformation.parseIdString(resourceId.getId());
		}

		//noinspection unchecked
		relationshipRepositoryForClass.setRelation(resource, parsedId, relationshipField, queryAdapter);
	}
}
