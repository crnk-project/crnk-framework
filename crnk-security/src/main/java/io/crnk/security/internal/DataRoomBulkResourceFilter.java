package io.crnk.security.internal;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.BulkResourceRepository;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.list.ResourceList;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class DataRoomBulkResourceFilter<T, I extends Serializable> extends DataRoomResourceFilter<T, I> implements BulkResourceRepository<T, I> {


    public DataRoomBulkResourceFilter(ResourceRepository<T, I> wrappedRepository, DataRoomMatcher matcher) {
        super(wrappedRepository, matcher);
    }

    private BulkResourceRepository<T, I> getWrapped() {
        return (BulkResourceRepository<T, I>) wrappedRepository;
    }

    @Override
    public <S extends T> List<S> save(List<S> resources) {
        for (S resource : resources) {
            matcher.verifyMatch(resource, HttpMethod.PATCH);
        }

        // verify access to unchanged state
        RegistryEntry entry = resourceRegistry.getEntry(getResourceClass());
        PreconditionUtil.verify(entry != null, "unknown resource class %s", getResourceClass());
        ResourceInformation resourceInformation = entry.getResourceInformation();

        List<I> ids = resources.stream().map(resource -> (I) resourceInformation.getId(resource)).collect(Collectors.toList());
        ResourceList<T> currentResources = wrappedRepository.findAll(ids, new QuerySpec(getResourceClass()));
        if (currentResources.size() != ids.size()) {
            throw new ForbiddenException("not allowed to access resource");
        }
        for (T currentResource : currentResources) {
            matcher.verifyMatch(currentResource, HttpMethod.PATCH);
        }

        return getWrapped().save(resources);
    }

    @Override
    public <S extends T> List<S> create(List<S> resources) {
        for (S resource : resources) {
            matcher.verifyMatch(resource, HttpMethod.POST);
        }
        return getWrapped().create(resources);
    }

    @Override
    public void delete(List<I> ids) {
        List<T> resources = getWrapped().findAll(ids, new QuerySpec(getResourceClass()));
        for (T resource : resources) {
            matcher.verifyMatch(resource, HttpMethod.DELETE);
        }
        if (resources.size() != ids.size()) {
            throw new ForbiddenException("not allowed to access resource");
        }
        getWrapped().delete(ids);
    }
}
