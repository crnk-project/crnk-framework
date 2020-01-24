package io.crnk.test.mock.repository;

import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.test.mock.models.ScheduleStatus;

public class ScheduleStatusRepositoryImpl extends InMemoryResourceRepository<ScheduleStatus, Long> {

    public ScheduleStatusRepositoryImpl() {
        super(ScheduleStatus.class);
    }
}