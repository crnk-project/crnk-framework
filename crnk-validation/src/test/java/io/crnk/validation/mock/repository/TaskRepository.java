package io.crnk.validation.mock.repository;

import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.validation.mock.models.Task;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TaskRepository implements ResourceRepository<Task, Long> {

    public static final ConcurrentHashMap<Long, Task> map = new ConcurrentHashMap<>();

    @Override
    public <S extends Task> S save(S entity) {
        if (entity.getId() == null) {
            entity.setId((long) (map.size() + 1));
        }
        map.put(entity.getId(), entity);

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

    @Override
    public Task findOne(Long aLong, QuerySpec querySpec) {
        Task task = map.get(aLong);
        if (task == null) {
            throw new ResourceNotFoundException("");
        }
        return task;
    }

    @Override
    public ResourceList<Task> findAll(QuerySpec querySpec) {
        return querySpec.apply(map.values());
    }

    @Override
    public ResourceList<Task> findAll(Collection<Long> ids, QuerySpec querySpec) {
        List<Task> values = new LinkedList<>();
        for (Task value : map.values()) {
            if (contains(value, ids)) {
                values.add(value);
            }
        }
        return querySpec.apply(values);
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
