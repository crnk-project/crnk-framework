package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.internal.repository.RelationshipRepositoryAdapter;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class RelationshipsResourcePost extends RelationshipsResourceUpsert {

	public RelationshipsResourcePost(ResourceRegistry resourceRegistry, TypeParser typeParser) {
		super(resourceRegistry, typeParser);
	}

	@Override
	public HttpMethod method() {
		return HttpMethod.POST;
	}

	@Override
	public void processToManyRelationship(Object resource, Class<? extends Serializable> relationshipIdType, ResourceField resourceField, Iterable<ResourceIdentifier> dataBodies, QueryAdapter queryAdapter,
										  RelationshipRepositoryAdapter relationshipRepositoryForClass) {
		List<Serializable> parsedIds = new LinkedList<>();

		for (ResourceIdentifier dataBody : dataBodies) {
			Serializable parsedId = typeParser.parse(dataBody.getId(), relationshipIdType);
			parsedIds.add(parsedId);
		}

		// noinspection unchecked
		relationshipRepositoryForClass.addRelations(resource, parsedIds, resourceField, queryAdapter);
	}

	@Override
	protected void processToOneRelationship(Object resource, Class<? extends Serializable> relationshipIdType, ResourceField resourceField, ResourceIdentifier dataBody, QueryAdapter queryAdapter,
											RelationshipRepositoryAdapter relationshipRepositoryForClass) {
		Serializable parsedId = null;
		if (dataBody != null) {
			parsedId = typeParser.parse(dataBody.getId(), relationshipIdType);
		}
		// noinspection unchecked
		relationshipRepositoryForClass.setRelation(resource, parsedId, resourceField, queryAdapter);
	}
}
