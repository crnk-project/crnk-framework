package io.crnk.spring.domain.repository;

import io.crnk.core.repository.RelationshipRepositoryBase;
import io.crnk.spring.domain.model.Project;
import io.crnk.spring.domain.model.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskToProjectRepository extends RelationshipRepositoryBase<Task, Long, Project, Long> {

	protected TaskToProjectRepository() {
		super(Task.class, Project.class);
	}

}
