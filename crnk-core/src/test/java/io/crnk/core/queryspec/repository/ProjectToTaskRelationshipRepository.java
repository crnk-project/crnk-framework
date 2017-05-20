package io.crnk.core.queryspec.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.repository.RelationshipRepositoryBase;

public class ProjectToTaskRelationshipRepository extends RelationshipRepositoryBase<Project, Long, Task, Long> {

	public ProjectToTaskRelationshipRepository() {
		super(Project.class, Task.class);
	}

}