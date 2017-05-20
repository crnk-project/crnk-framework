package io.crnk.test.mock.repository;

import io.crnk.core.repository.RelationshipRepositoryBase;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;

public class TaskToScheduleRepo extends RelationshipRepositoryBase<Task, Long, Schedule, Long> {

	public TaskToScheduleRepo() {
		super(Task.class, Schedule.class);
	}

}
