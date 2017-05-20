package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.TaskWithLookup;
import io.crnk.legacy.repository.annotations.JsonApiFindOneTarget;
import io.crnk.legacy.repository.annotations.JsonApiRelationshipRepository;

@JsonApiRelationshipRepository(source = TaskWithLookup.class, target = Project.class)
public class TaskWithLookupToProjectRepository {

	@JsonApiFindOneTarget
	public Project findOneTarget(String sourceId, String fieldName) {
		return new Project()
				.setId(1L);
	}
}
