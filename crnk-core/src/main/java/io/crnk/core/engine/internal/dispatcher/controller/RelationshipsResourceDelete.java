package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.filter.ResourceModificationFilter;
import io.crnk.core.engine.filter.ResourceRelationshipModificationType;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.Result;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RelationshipsResourceDelete extends RelationshipsResourceUpsert {

	@Override
	public HttpMethod method() {
		return HttpMethod.DELETE;
	}

	@Override
	public Result processToManyRelationship(Result<Object> resourceResult, ResourceInformation targetResourceInformation, ResourceField relationshipField, Iterable<ResourceIdentifier> dataBodies, QueryAdapter queryAdapter,
											RelationshipRepositoryAdapter relationshipRepositoryForClass) {
		return resourceResult.merge(resource -> {
			List<ResourceIdentifier> resourceIds = new ArrayList<>();
			for (ResourceIdentifier dataBody : dataBodies) {
				resourceIds.add(dataBody);
			}
			List<ResourceModificationFilter> modificationFilters = context.getModificationFilters();
			for (ResourceModificationFilter filter : modificationFilters) {
				resourceIds = filter.modifyManyRelationship(resource, relationshipField, ResourceRelationshipModificationType.REMOVE, resourceIds);
			}

			List<Serializable> parsedIds = new LinkedList<>();
			for (ResourceIdentifier resourceId : resourceIds) {
				Serializable parsedId = targetResourceInformation.parseIdString(resourceId.getId());
				parsedIds.add(parsedId);
			}
			// noinspection unchecked
			return relationshipRepositoryForClass.removeRelations(resource, parsedIds, relationshipField, queryAdapter);
		});
	}

	@Override
	protected Result processToOneRelationship(Result<Object> resourceResult, ResourceInformation targetResourceInformation, ResourceField relationshipField, ResourceIdentifier dataBody, QueryAdapter queryAdapter,
											  RelationshipRepositoryAdapter relationshipRepositoryForClass) {
		return resourceResult.merge(resource -> {
			ResourceIdentifier resourceId = null;
			List<ResourceModificationFilter> modificationFilters = context.getModificationFilters();
			for (ResourceModificationFilter filter : modificationFilters) {
				resourceId = filter.modifyOneRelationship(resource, relationshipField, resourceId);
			}

			Serializable parsedId = resourceId != null ? targetResourceInformation.parseIdString(resourceId.getId()) : null;

			// noinspection unchecked
			return relationshipRepositoryForClass.setRelation(resource, parsedId, relationshipField, queryAdapter);
		});
	}

}
