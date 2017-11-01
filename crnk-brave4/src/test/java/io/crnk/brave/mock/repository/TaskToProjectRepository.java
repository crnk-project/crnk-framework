package io.crnk.brave.mock.repository;

import io.crnk.brave.mock.models.Project;
import io.crnk.brave.mock.models.Task;
import io.crnk.core.repository.RelationshipRepositoryBase;

public class TaskToProjectRepository extends RelationshipRepositoryBase<Task, Long, Project, Long> {

	public TaskToProjectRepository() {
		super(Task.class, Project.class);
	}
}
