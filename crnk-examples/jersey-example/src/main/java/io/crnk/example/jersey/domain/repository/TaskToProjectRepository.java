package io.crnk.example.jersey.domain.repository;

import io.crnk.example.jersey.domain.model.Project;
import io.crnk.example.jersey.domain.model.Task;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.repository.annotations.*;

@JsonApiRelationshipRepository(source = Task.class, target = Project.class)
public class TaskToProjectRepository {

	@JsonApiSetRelation
	public void setRelation(Task task, Long projectId, String fieldName) {

	}

	@JsonApiAddRelations
	public void addRelations(Task source, Iterable<Long> targetIds, String fieldName) {
	}

	@JsonApiRemoveRelations
	public void removeRelations(Task source, Iterable<Long> targetIds, String fieldName) {
	}

	@JsonApiFindOneTarget
	public Project findOneTarget(Long sourceId, String fieldName, QueryParams requestParams) {
		Project project = new Project();
		project.setId(123L);
		project.setName("Request scoped value");
		return project;
	}
}
