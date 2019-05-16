package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.TaskWithLookup;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.resource.list.ResourceList;

import java.util.Collection;

public class TaskWithLookupToProjectRepository implements RelationshipRepository<TaskWithLookup, String, Project, Long> {


	@Override
	public Class<TaskWithLookup> getSourceResourceClass() {
		return TaskWithLookup.class;
	}

	@Override
	public Class<Project> getTargetResourceClass() {
		return Project.class;
	}

	@Override
	public void setRelation(TaskWithLookup source, Long targetId, String fieldName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRelations(TaskWithLookup source, Collection<Long> targetIds, String fieldName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addRelations(TaskWithLookup source, Collection<Long> targetIds, String fieldName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeRelations(TaskWithLookup source, Collection<Long> targetIds, String fieldName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Project findOneTarget(String sourceId, String fieldName, QuerySpec querySpec) {
		return new Project()
				.setId(1L);
	}

	@Override
	public ResourceList<Project> findManyTargets(String sourceId, String fieldName, QuerySpec querySpec) {
		throw new UnsupportedOperationException();
	}
}
