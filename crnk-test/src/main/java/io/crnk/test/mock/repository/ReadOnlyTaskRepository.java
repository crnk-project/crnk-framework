package io.crnk.test.mock.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.annotations.JsonApiExposed;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.models.ReadOnlyTask;
import org.junit.Assert;

import java.util.Arrays;
import java.util.Collection;

public class ReadOnlyTaskRepository implements ResourceRepository<ReadOnlyTask, Long> {


    @Override
    public Class<ReadOnlyTask> getResourceClass() {
        return ReadOnlyTask.class;
    }

    @Override
    public ReadOnlyTask findOne(Long id, QuerySpec querySpec) {
        ReadOnlyTask readOnlyTask = new ReadOnlyTask();
        readOnlyTask.setId(id);
        readOnlyTask.setName("test");
        return readOnlyTask;
    }

    @Override
    public ResourceList<ReadOnlyTask> findAll(QuerySpec querySpec) {
        ReadOnlyTask readOnlyTask = new ReadOnlyTask();
        readOnlyTask.setId(12L);
        readOnlyTask.setName("test");
        return querySpec.apply(Arrays.asList(readOnlyTask));
    }

    @Override
    public ResourceList<ReadOnlyTask> findAll(Collection<Long> ids, QuerySpec querySpec) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <S extends ReadOnlyTask> S save(S resource) {
        Assert.fail();
        return null;
    }

    @Override
    public <S extends ReadOnlyTask> S create(S resource) {
        Assert.fail();
        return null;
    }

    @Override
    public void delete(Long id) {
        Assert.fail();
    }
}