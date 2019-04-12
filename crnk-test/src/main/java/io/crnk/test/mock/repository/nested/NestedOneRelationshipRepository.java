package io.crnk.test.mock.repository.nested;

import java.io.Serializable;
import java.util.Collection;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.nested.NestedRelatedResource;

public class NestedOneRelationshipRepository implements RelationshipRepository {

	@Override
	public Class getSourceResourceClass() {
		return OneNestedRepository.class;
	}

	@Override
	public Class getTargetResourceClass() {
		return NestedRelatedResource.class;
	}

	@Override
	public void setRelation(Object source, Serializable targetId, String fieldName) {

	}

	@Override
	public void setRelations(Object source, Collection targetIds, String fieldName) {

	}

	@Override
	public void addRelations(Object source, Collection targetIds, String fieldName) {

	}

	@Override
	public void removeRelations(Object source, Collection targetIds, String fieldName) {

	}

	@Override
	public Object findOneTarget(Serializable sourceId, String fieldName, QuerySpec querySpec) {
		NestedRelatedResource related = new NestedRelatedResource();
		related.setId("related1");
		return related;
	}

	@Override
	public ResourceList findManyTargets(Serializable sourceId, String fieldName, QuerySpec querySpec) {
		return null;
	}
}