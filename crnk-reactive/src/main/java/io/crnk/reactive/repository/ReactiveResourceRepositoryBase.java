package io.crnk.reactive.repository;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Recommended base class to implement a resource repository making use of the
 * QuerySpec and ResourceList. Note that the former
 * <p>
 * Base implements for {@link ReactiveResourceRepository} implementing most of the
 * methods. Unless {@link #save(T)} and {@link #delete(I)} get
 * overridden, this repository is read-only. Only {@link #findAll(QuerySpec)}
 * needs to be implemented to have a working repository.
 *
 * @param <T> resource type
 * @param <I> identity type
 */
public abstract class ReactiveResourceRepositoryBase<T, I> implements ReactiveResourceRepository<T, I>, ResourceRegistryAware {

    private Class<T> resourceClass;

    protected ResourceRegistry resourceRegistry;

    public ReactiveResourceRepositoryBase(Class<T> resourceClass) {
        this.resourceClass = resourceClass;
    }

    @Override
    public Class<T> getResourceClass() {
        return resourceClass;
    }

    /**
     * Forwards to {@link #findAll(QuerySpec)}
     *
     * @param id        of the resource
     * @param querySpec for field and relation inclusion
     * @return resource
     */
    @Override
    public Mono<T> findOne(I id, QuerySpec querySpec) {
        RegistryEntry entry = resourceRegistry.findEntry(resourceClass);
        String idName = entry.getResourceInformation().getIdField().getUnderlyingName();

        QuerySpec idQuerySpec = querySpec.clone();
        idQuerySpec.addFilter(new FilterSpec(Arrays.asList(idName), FilterOperator.EQ, id));
        return findAll(idQuerySpec).map(list -> {
            Iterator<T> iterator = list.iterator();
            if (iterator.hasNext()) {
                T resource = iterator.next();
                PreconditionUtil.verify(!iterator.hasNext(), "expected single result, got resources=%s for id=%s", list, id);
                return resource;
            } else {
                throw new ResourceNotFoundException("resource not found");
            }
        });
    }

    /**
     * Forwards to {@link #findAll(QuerySpec)}
     *
     * @param ids       of the resources
     * @param querySpec for field and relation inclusion
     * @return resources
     */
    @Override
    public Mono<ResourceList<T>> findAll(Collection<I> ids, QuerySpec querySpec) {
        RegistryEntry entry = resourceRegistry.findEntry(resourceClass);
        String idName = entry.getResourceInformation().getIdField().getUnderlyingName();
        FilterSpec filter = new FilterSpec(Arrays.asList(idName), FilterOperator.EQ, ids);
        QuerySpec idQuerySpec = querySpec.clone();
        idQuerySpec.addFilter(filter);
        return findAll(idQuerySpec);
    }

    /**
     * read-only by default
     *
     * @param resource to save
     * @return saved resource
     */
    @Override
    public Mono<T> save(T resource) {
        return Mono.error(new UnsupportedOperationException());
    }

    @Override
    public Mono<T> create(T resource) {
        return save(resource);
    }

    /**
     * read-only by default
     *
     * @param id of resource to delete
     */
    @Override
    public Mono<Boolean> delete(I id) {
        return Mono.error(new UnsupportedOperationException());
    }

    @Override
    public void setResourceRegistry(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }
}
