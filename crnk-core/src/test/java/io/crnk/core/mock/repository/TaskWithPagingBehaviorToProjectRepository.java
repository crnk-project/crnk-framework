package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.repository.RelationshipRepositoryBase;

public class TaskWithPagingBehaviorToProjectRepository
		extends RelationshipRepositoryBase<TaskWithPagingBehavior, String, Project, String> {

	public TaskWithPagingBehaviorToProjectRepository() {
		super(TaskWithPagingBehavior.class, Project.class);
	}
}