package io.crnk.core.mock.repository;

import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.TaskWithLookup;
import io.crnk.legacy.repository.annotations.JsonApiFindOne;
import io.crnk.legacy.repository.annotations.JsonApiResourceRepository;

@JsonApiResourceRepository(TaskWithLookup.class)
public class TaskWithLookupRepository {

	@JsonApiFindOne
	public TaskWithLookup findOne(String id) {
		return new TaskWithLookup()
				.setId(id)
				.setProject(new Project().setId(42L))
				.setProjectOverridden(new Project().setId(42L));
	}
}
