package io.crnk.security.model;

import io.crnk.core.repository.RelationshipRepositoryBase;

public class TaskToProjectRepository extends RelationshipRepositoryBase<Task, Long, Project, Long> {

	public TaskToProjectRepository() {
		super(Task.class, Project.class);
	}

}
