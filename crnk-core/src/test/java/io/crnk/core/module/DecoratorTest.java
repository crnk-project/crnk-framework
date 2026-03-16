package io.crnk.core.module;

import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.repository.ScheduleRepository;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.RelationshipRepository;
import io.crnk.core.repository.decorate.WrappedRelationshipRepository;
import io.crnk.core.repository.decorate.WrappedResourceRepository;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.isNull;

public class DecoratorTest {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    public void testDecoratedResourceRepositoryBase() {
        ScheduleRepository repository = Mockito.mock(ScheduleRepository.class);
        WrappedResourceRepository<Schedule, Long> decorator = new WrappedResourceRepository<>(repository);
        Assert.assertSame(repository, decorator.getWrappedObject());

        decorator.create(null);
        Mockito.verify(repository, Mockito.times(1)).create(isNull());

        decorator.delete(null);
        Mockito.verify(repository, Mockito.times(1)).delete(isNull());

        decorator.findAll(null);
        Mockito.verify(repository, Mockito.times(1)).findAll(isNull());

        decorator.findAll(null, null);
        Mockito.verify(repository, Mockito.times(1)).findAll(isNull(), isNull());

        decorator.getResourceClass();
        Mockito.verify(repository, Mockito.times(1)).getResourceClass();

        Schedule schedule = Mockito.mock(Schedule.class);
        decorator.save(schedule);
        Mockito.verify(repository, Mockito.times(1)).save(Mockito.eq(schedule));

        decorator.findOne(null, null);
        Mockito.verify(repository, Mockito.times(1)).findOne(isNull(), isNull());
    }

    interface RegistryAwareResourceRepository extends ScheduleRepository, ResourceRegistryAware {

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testDecoratedRelationshipRepositoryBase() {
        RelationshipRepository<Schedule, Long, Task, Long> repository = Mockito.mock(RelationshipRepository.class);
        WrappedRelationshipRepository<Schedule, Long, Task, Long> decorator = new WrappedRelationshipRepository(repository);

        decorator.findManyTargets(null, null, null);
        Mockito.verify(repository, Mockito.times(1)).findManyTargets(isNull(), isNull(), isNull());

        decorator.findOneTarget(null, null, null);
        Mockito.verify(repository, Mockito.times(1)).findOneTarget(isNull(), isNull(), isNull());

        decorator.setRelation(null, null, null);
        Mockito.verify(repository, Mockito.times(1)).setRelation(isNull(), isNull(), isNull());

        decorator.addRelations(null, null, null);
        Mockito.verify(repository, Mockito.times(1)).addRelations(isNull(), isNull(), isNull());

        decorator.setRelations(null, null, null);
        Mockito.verify(repository, Mockito.times(1)).setRelations(isNull(), isNull(), isNull());

        decorator.removeRelations(null, null, null);
        Mockito.verify(repository, Mockito.times(1)).removeRelations(isNull(), isNull(), isNull());

        decorator.getTargetResourceClass();
        Mockito.verify(repository, Mockito.times(1)).getTargetResourceClass();

        decorator.getSourceResourceClass();
        Mockito.verify(repository, Mockito.times(1)).getSourceResourceClass();
    }
}
