package io.crnk.test.mock.repository;

import io.crnk.core.repository.RelationshipRepositoryBase;
import io.crnk.test.mock.models.Schedule;
import io.crnk.test.mock.models.Task;

public class ScheduleToTaskRepository extends RelationshipRepositoryBase<Schedule, Long, Task, Long> {

	public ScheduleToTaskRepository() {
		super(Schedule.class, Task.class);
	}

}
