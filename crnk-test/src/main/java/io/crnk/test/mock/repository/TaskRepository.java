package io.crnk.test.mock.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.annotations.JsonApiExposed;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.TestException;
import io.crnk.test.mock.UnknownException;
import io.crnk.test.mock.models.Task;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@JsonApiExposed
public class TaskRepository implements ResourceRepositoryV2<Task, Long> {

    private static final ConcurrentHashMap<Long, Task> map = new ConcurrentHashMap<>();

    public static void clear() {
        map.clear();
    }

    public <S extends Task> S save(S entity) {

        if (entity.getId() == null) {
            entity.setId((long) (map.size() + 1));
        }
        map.put(entity.getId(), entity);

        if (entity.getId() == 10000) {
            throw new TestException("msg");
        }
        if (entity.getId() == 10001) {
            throw new UnknownException("msg");
        }

        return entity;
    }

    @Override
    public <S extends Task> S create(S resource) {
        return save(resource);
    }

    @Override
    public Class<Task> getResourceClass() {
        return Task.class;
    }

    public Task findOne(Long aLong, QuerySpec querySpec) {
        if (aLong == 10000) {
            throw new TestException("msg");
        }
        if (aLong == 10001) {
            throw new UnknownException("msg");
        }

        Task task = map.get(aLong);
        if (task == null) {
            throw new ResourceNotFoundException("failed to find resource with id " + aLong);
        }
        return task;
    }

    @Override
    public ResourceList<Task> findAll(QuerySpec querySpec) {
        return querySpec.apply(map.values());
    }

    @Override
    public ResourceList<Task> findAll(Iterable<Long> ids, QuerySpec queryParams) {
        List<Task> querySpec = new LinkedList<>();
        for (Task value : map.values()) {
            if (contains(value, ids)) {
                querySpec.add(value);
            }
        }
        return queryParams.apply(querySpec);
    }

    private boolean contains(Task value, Iterable<Long> ids) {
        for (Long id : ids) {
            if (value.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void delete(Long aLong) {
        map.remove(aLong);
    }
}
