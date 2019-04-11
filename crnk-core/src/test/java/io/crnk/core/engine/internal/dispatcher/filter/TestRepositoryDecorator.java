package io.crnk.core.engine.internal.dispatcher.filter;

import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.repository.ScheduleRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.repository.decorate.RepositoryDecoratorFactoryBase;
import io.crnk.core.repository.decorate.ResourceRepositoryDecorator;
import io.crnk.core.repository.decorate.ResourceRepositoryDecoratorBase;

import java.io.Serializable;

public class TestRepositoryDecorator extends RepositoryDecoratorFactoryBase {

	@SuppressWarnings("unchecked")
	@Override
	public <T, I extends Serializable> ResourceRepositoryDecorator<T, I> decorateRepository(
			ResourceRepository<T, I> repository) {
		if (repository.getResourceClass() == Schedule.class) {
			return (ResourceRepositoryDecorator<T, I>) new DecoratedScheduleRepository();
		}
		return null;
	}

	public static class DecoratedScheduleRepository extends ResourceRepositoryDecoratorBase<Schedule, Long>
			implements ScheduleRepository {

		@Override
		public ScheduleList findAll(QuerySpec querySpec) {
			return (ScheduleList) super.findAll(querySpec);
		}
	}
}
