package io.crnk.core.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;

public class RelationshipRepositoryV2Test {


	private RelationshipRepositoryV2 untypedRepo = new UntypedRelationshipRepository() {

		@Override
		public Class getSourceResourceClass() {
			return Task.class;
		}

		@Override
		public Class getTargetResourceClass() {
			return Project.class;
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
			return null;
		}

		@Override
		public ResourceList findManyTargets(Serializable sourceId, String fieldName, QuerySpec querySpec) {
			return null;
		}

		@Override
		public String getSourceResourceType() {
			return "tasks";
		}

		@Override
		public String getTargetResourceType() {
			return "projects";
		}
	};

	private RelationshipRepositoryV2 nullRepo = new RelationshipRepositoryV2() {

		@Override
		public Class getSourceResourceClass() {
			return null;
		}

		@Override
		public Class getTargetResourceClass() {
			return null;
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
			return null;
		}

		@Override
		public ResourceList findManyTargets(Serializable sourceId, String fieldName, QuerySpec querySpec) {
			return null;
		}
	};

	@Test
	public void getMatcherForUntyped() {
		RelationshipMatcher matcher = untypedRepo.getMatcher();

		Assert.assertEquals(1, matcher.rules.size());
		RelationshipMatcherRule rule = matcher.rules.get(0);
		Assert.assertEquals("tasks", rule.sourceResourceType);
		Assert.assertEquals("projects", rule.targetResourceType);
	}

	@Test(expected = IllegalStateException.class)
	public void getMatcherThrowsExceptionWithoutTypes() {
		nullRepo.getMatcher();
	}

}
