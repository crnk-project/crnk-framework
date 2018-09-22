package io.crnk.servlet.resource.repository;

import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.RelationshipRepository;
import io.crnk.servlet.resource.model.Project;
import io.crnk.servlet.resource.model.Task;

public class TaskToProjectRepository implements RelationshipRepository<Task, Long, Project, Long> {

	@Override
	public void setRelation(Task task, Long projectId, String fieldName) {

	}

	@Override
	public void setRelations(Task task, Iterable<Long> projectId, String fieldName) {

	}

	@Override
	public void addRelations(Task source, Iterable<Long> targetIds, String fieldName) {
	}

	@Override
	public void removeRelations(Task source, Iterable<Long> targetIds, String fieldName) {
	}

	@Override
	public Project findOneTarget(Long sourceId, String fieldName, QueryParams requestParams) {
		return null;
	}

	@Override
	public Iterable<Project> findManyTargets(Long sourceId, String fieldName, QueryParams requestParams) {
		return null;
	}
}
