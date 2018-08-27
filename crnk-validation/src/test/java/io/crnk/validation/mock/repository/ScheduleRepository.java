package io.crnk.validation.mock.repository;

import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.validation.mock.models.Schedule;

public class ScheduleRepository extends InMemoryResourceRepository<Schedule, Long> {

	public ScheduleRepository() {
		super(Schedule.class);
	}
}
