package io.crnk.test.mock.repository.nested;

import java.io.Serializable;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.nested.PostComment;
import io.crnk.test.mock.models.nested.NestedRelatedResource;

public class NestedManyRelationshipRepository implements RelationshipRepositoryV2 {

	@Override
	public Class getSourceResourceClass() {
		return PostComment.class;
	}

	@Override
	public Class getTargetResourceClass() {
		return NestedRelatedResource.class;
	}

	@Override
	public void setRelation(Object source, Serializable targetId, String fieldName) {

	}

	@Override
	public void setRelations(Object source, Iterable targetIds, String fieldName) {

	}

	@Override
	public void addRelations(Object source, Iterable targetIds, String fieldName) {

	}

	@Override
	public void removeRelations(Object source, Iterable targetIds, String fieldName) {

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