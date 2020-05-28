package io.crnk.test.mock.repository;

import io.crnk.core.repository.BulkResourceRepository;
import io.crnk.core.repository.InMemoryResourceRepository;

import java.util.ArrayList;
import java.util.List;

public class BulkInMemoryRepository<T, I> extends InMemoryResourceRepository<T, I> implements BulkResourceRepository<T, I> {

    public BulkInMemoryRepository(Class<T> resourceClass) {
        super(resourceClass);
    }

    @Override
    public <S extends T> List<S> save(List<S> resources) {
        List<S> results = new ArrayList<>();
        for (S resource : resources) {
            results.add(save(resource));
        }
        return results;
    }

    @Override
    public <S extends T> List<S> create(List<S> resources) {
        List<S> results = new ArrayList<>();
        for (S resource : resources) {
            results.add(create(resource));
        }
        return results;
    }

    @Override
    public void delete(List<I> ids) {
        for (I id : ids) {
            delete(id);
        }
    }
}
