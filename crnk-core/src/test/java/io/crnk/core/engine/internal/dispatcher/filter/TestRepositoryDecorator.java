package io.crnk.core.engine.internal.dispatcher.filter;

import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.repository.ScheduleRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactory;
import io.crnk.core.repository.decorate.WrappedResourceRepository;

public class TestRepositoryDecorator implements RepositoryDecoratorFactory {


    @Override
    public Object decorateRepository(Object repository) {
        if (repository instanceof ResourceRepository && ((ResourceRepository) repository).getResourceClass() == Schedule.class) {
            return new DecoratedScheduleRepository((ResourceRepository<Schedule, Long>) repository);
        }
        return repository;
    }

    public static class DecoratedScheduleRepository extends WrappedResourceRepository<Schedule, Long>
            implements ScheduleRepository {

        public DecoratedScheduleRepository(ResourceRepository<Schedule, Long> wrappedRepository) {
            super(wrappedRepository);
        }

        @Override
        public ScheduleList findAll(QuerySpec querySpec) {
            return (ScheduleList) super.findAll(querySpec);
        }
    }
}
