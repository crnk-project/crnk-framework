package io.crnk.core.queryspec.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.repository.RelationshipRepositoryBase;

public class TaskWithPagingBehaviorToProjectRelationshipRepository
		extends RelationshipRepositoryBase<TaskWithPagingBehavior, String, Project, String> {

	public TaskWithPagingBehaviorToProjectRelationshipRepository() {
		super(TaskWithPagingBehavior.class, Project.class);
	}
}