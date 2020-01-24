package io.crnk.test.mock.repository;

import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextAware;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.annotations.JsonApiExposed;
import io.crnk.core.resource.links.DefaultSelfLinksInformation;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.TestException;
import io.crnk.test.mock.UnknownException;
import io.crnk.test.mock.models.Task;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@JsonApiExposed
public class TaskRepository implements ResourceRepository<Task, Long>, HttpRequestContextAware {

    private static final ConcurrentHashMap<Long, Task> map = new ConcurrentHashMap<>();

    private HttpRequestContextProvider contextProvider;

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
        DefaultResourceList<Task> list = querySpec.apply(map.values());

        // header testing
        if (contextProvider != null) {
            HttpRequestContext requestContext = contextProvider.getRequestContext();
            if (requestContext != null) {
                String testHeader = requestContext.getRequestHeader("X-TEST");
                if (testHeader != null) {
                    DefaultSelfLinksInformation links = new DefaultSelfLinksInformation();
                    links.setSelf(testHeader);
                    list.setLinks(links);
                }
            }
        }
        return list;
    }

    @Override
    public ResourceList<Task> findAll(Collection<Long> ids, QuerySpec querySpec) {
        List<Task> results = new LinkedList<>();
        for (Task value : map.values()) {
            if (contains(value, ids)) {
                results.add(value);
            }
        }
        return querySpec.apply(results);
    }

    private boolean contains(Task value, Collection<Long> ids) {
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

    @Override
    public void setHttpRequestContextProvider(HttpRequestContextProvider requestContextProvider) {
        this.contextProvider = requestContextProvider;
    }
}
