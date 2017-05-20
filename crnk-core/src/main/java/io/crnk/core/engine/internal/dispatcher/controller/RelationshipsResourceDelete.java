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

public class RelationshipsResourceDelete extends RelationshipsResourceUpsert {

	public RelationshipsResourceDelete(ResourceRegistry resourceRegistry, TypeParser typeParser) {
		super(resourceRegistry, typeParser);
	}

	@Override
	public HttpMethod method() {
		return HttpMethod.DELETE;
	}

	@Override
	public void processToManyRelationship(Object resource, Class<? extends Serializable> relationshipIdType, ResourceField relationshipField, Iterable<ResourceIdentifier> dataBodies, QueryAdapter queryAdapter,
										  RelationshipRepositoryAdapter relationshipRepositoryForClass) {

		List<Serializable> parsedIds = new LinkedList<>();
		for (ResourceIdentifier dataBody : dataBodies) {
			Serializable parsedId = typeParser.parse(dataBody.getId(), relationshipIdType);
			parsedIds.add(parsedId);
		}
		// noinspection unchecked
		relationshipRepositoryForClass.removeRelations(resource, parsedIds, relationshipField, queryAdapter);
	}

	@Override
	protected void processToOneRelationship(Object resource, Class<? extends Serializable> relationshipIdType, ResourceField relationshipField, ResourceIdentifier dataBody, QueryAdapter queryAdapter,
											RelationshipRepositoryAdapter relationshipRepositoryForClass) {
		// noinspection unchecked
		relationshipRepositoryForClass.setRelation(resource, null, relationshipField, queryAdapter);
	}

}
