package io.crnk.brave.mock.repository;

import io.crnk.brave.mock.models.Project;
import io.crnk.brave.mock.models.Task;
import io.crnk.core.repository.RelationshipRepositoryBase;

public class ProjectToTaskRepository extends RelationshipRepositoryBase<Project, Long, Task, Long> {

	public ProjectToTaskRepository() {
		super(Project.class, Task.class);
	}
}
