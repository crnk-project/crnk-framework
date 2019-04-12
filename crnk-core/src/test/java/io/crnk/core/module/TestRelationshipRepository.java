package io.crnk.core.module;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.resource.list.ResourceList;

import java.util.Collection;

class TestRelationshipRepository implements RelationshipRepository<TestResource, Integer, TestResource, Integer> {

	@Override
	public void setRelation(TestResource source, Integer targetId, String fieldName) {
	}

	@Override
	public void setRelations(TestResource source, Collection<Integer> targetIds, String fieldName) {
	}

	@Override
	public void addRelations(TestResource source, Collection<Integer> targetIds, String fieldName) {
	}

	@Override
	public void removeRelations(TestResource source, Collection<Integer> targetIds, String fieldName) {
	}

	@Override
	public TestResource findOneTarget(Integer sourceId, String fieldName, QuerySpec queryParams) {
		return null;
	}

	@Override
	public ResourceList<TestResource> findManyTargets(Integer sourceId, String fieldName, QuerySpec queryParams) {
		return null;
	}

	@Override
	public Class<TestResource> getSourceResourceClass() {
		return null;
	}

	@Override
	public Class<TestResource> getTargetResourceClass() {
		return null;
	}
}