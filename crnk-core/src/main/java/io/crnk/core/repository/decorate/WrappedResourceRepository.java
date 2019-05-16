package io.crnk.core.repository.decorate;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;

import java.util.Collection;

/**
 * Wraps another resource repository. In contrast to decorators, a wrapped repository is still a repository and treated as such.
 */
public class WrappedResourceRepository<T, I> implements ResourceRepository<T, I>, Wrapper {

    protected ResourceRepository<T, I> wrappedRepository;

    public WrappedResourceRepository(ResourceRepository<T, I> wrappedRepository) {
        this.wrappedRepository = wrappedRepository;
    }

    @Override
    public Class<T> getResourceClass() {
        return wrappedRepository.getResourceClass();
    }

    @Override
    public T findOne(I id, QuerySpec querySpec) {
        return wrappedRepository.findOne(id, querySpec);
    }

    @Override
    public ResourceList<T> findAll(QuerySpec querySpec) {
        return wrappedRepository.findAll(querySpec);
    }

    @Override
    public ResourceList<T> findAll(Collection<I> ids, QuerySpec querySpec) {
        return wrappedRepository.findAll(ids, querySpec);
    }

    @Override
    public <S extends T> S save(S entity) {
        return wrappedRepository.save(entity);
    }

    @Override
    public <S extends T> S create(S entity) {
        return wrappedRepository.create(entity);
    }

    @Override
    public void delete(I id) {
        wrappedRepository.delete(id);
    }

    @Override
    public Object getWrappedObject() {
        return wrappedRepository;
    }
}
