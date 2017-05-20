package io.crnk.core.module;

import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.ScheduleRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepositoryV2;
import io.crnk.core.repository.decorate.RelationshipRepositoryDecoratorBase;
import io.crnk.core.repository.decorate.ResourceRepositoryDecoratorBase;
import org.junit.Test;
import org.mockito.Mockito;

public class DecoratorTest {

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Test
	public void testDecoratedResourceRepositoryBase() {
		ScheduleRepository repository = Mockito.mock(ScheduleRepository.class);
		ResourceRepositoryDecoratorBase<Schedule, Long> decorator = new ResourceRepositoryDecoratorBase() {
		};
		decorator.setDecoratedObject(repository);

		decorator.create(null);
		Mockito.verify(repository, Mockito.times(1)).create(Mockito.any(Schedule.class));

		decorator.delete(null);
		Mockito.verify(repository, Mockito.times(1)).delete(Mockito.anyLong());

		decorator.findAll(null);
		Mockito.verify(repository, Mockito.times(1)).findAll(Mockito.any(QuerySpec.class));

		decorator.findAll(null, null);
		Mockito.verify(repository, Mockito.times(1)).findAll(Mockito.anyListOf(Long.class), Mockito.any(QuerySpec.class));

		decorator.findOne(null, null);
		Mockito.verify(repository, Mockito.times(1)).findOne(Mockito.anyLong(), Mockito.any(QuerySpec.class));
	}


	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	public void testDecoratedRelationshipRepositoryBase() {
		RelationshipRepositoryV2<Schedule, Long, Task, Long> repository = Mockito.mock(RelationshipRepositoryV2.class);
		RelationshipRepositoryDecoratorBase<Schedule, Long, Task, Long> decorator = new RelationshipRepositoryDecoratorBase() {
		};
		decorator.setDecoratedObject(repository);

		decorator.findManyTargets(null, null, null);
		Mockito.verify(repository, Mockito.times(1)).findManyTargets(Mockito.anyLong(), Mockito.anyString(),
				Mockito.any(QuerySpec.class));

		decorator.findOneTarget(null, null, null);
		Mockito.verify(repository, Mockito.times(1)).findOneTarget(Mockito.anyLong(), Mockito.anyString(),
				Mockito.any(QuerySpec.class));

		decorator.setRelation(null, null, null);
		Mockito.verify(repository, Mockito.times(1)).setRelation(Mockito.any(Schedule.class), Mockito.anyLong(),
				Mockito.anyString());

		decorator.addRelations(null, null, null);
		Mockito.verify(repository, Mockito.times(1)).addRelations(Mockito.any(Schedule.class), Mockito.anyListOf(Long.class),
				Mockito.anyString());

		decorator.removeRelations(null, null, null);
		Mockito.verify(repository, Mockito.times(1)).removeRelations(Mockito.any(Schedule.class), Mockito.anyListOf(Long.class),
				Mockito.anyString());
	}
}
