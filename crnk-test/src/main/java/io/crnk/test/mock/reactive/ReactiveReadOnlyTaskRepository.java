package io.crnk.test.mock.reactive;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.reactive.repository.ReactiveResourceRepository;
import io.crnk.test.mock.models.ReadOnlyTask;
import org.junit.Assert;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;

public class ReactiveReadOnlyTaskRepository implements ReactiveResourceRepository<ReadOnlyTask, Long> {


    @Override
    public Class<ReadOnlyTask> getResourceClass() {
        return ReadOnlyTask.class;
    }

    @Override
    public Mono<ReadOnlyTask> findOne(Long id, QuerySpec querySpec) {
        ReadOnlyTask readOnlyTask = new ReadOnlyTask();
        readOnlyTask.setId(id);
        readOnlyTask.setName("test");
        return Mono.just(readOnlyTask);
    }

    @Override
    public Mono<ResourceList<ReadOnlyTask>> findAll(QuerySpec querySpec) {
        ReadOnlyTask readOnlyTask = new ReadOnlyTask();
        readOnlyTask.setId(12L);
        readOnlyTask.setName("test");
        return Mono.just(querySpec.apply(Arrays.asList(readOnlyTask)));
    }

    @Override
    public Mono<ResourceList<ReadOnlyTask>> findAll(Collection<Long> ids, QuerySpec querySpec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<ReadOnlyTask> save(ReadOnlyTask resource) {
        Assert.fail();
        return null;
    }

    @Override
    public Mono<ReadOnlyTask> create(io.crnk.test.mock.models.ReadOnlyTask resource) {
        Assert.fail();
        return null;
    }

    @Override
    public Mono<Boolean> delete(Long id) {
        Assert.fail();
        return null;
    }
}
