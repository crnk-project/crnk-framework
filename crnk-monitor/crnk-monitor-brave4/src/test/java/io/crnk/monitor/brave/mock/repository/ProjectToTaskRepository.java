package io.crnk.monitor.brave.mock.repository;

import io.crnk.core.repository.RelationshipRepositoryBase;
import io.crnk.monitor.brave.mock.models.Project;
import io.crnk.monitor.brave.mock.models.Task;

public class ProjectToTaskRepository extends RelationshipRepositoryBase<Project, Long, Task, Long> {

	public ProjectToTaskRepository() {
		super(Project.class, Task.class);
	}
}
