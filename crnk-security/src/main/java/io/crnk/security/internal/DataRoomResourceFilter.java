package io.crnk.security.internal;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.repository.decorate.WrappedResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;

public class DataRoomResourceFilter<T, I extends Serializable> extends WrappedResourceRepository<T, I> implements ResourceRegistryAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataRoomResourceFilter.class);

    private final DataRoomMatcher matcher;

    private ResourceRegistry resourceRegistry;


    public DataRoomResourceFilter(ResourceRepository<T, I> wrappedRepository, DataRoomMatcher matcher) {
        super(wrappedRepository);
        this.matcher = matcher;
    }

    @Override
    public T findOne(I id, QuerySpec querySpec) {
        T resource = wrappedRepository.findOne(id, querySpec);
        matcher.verifyMatch(resource, HttpMethod.GET);
        return resource;
    }

    @Override
    public ResourceList<T> findAll(QuerySpec querySpec) {
        QuerySpec dataroomQuerySpec = matcher.filter(querySpec, HttpMethod.GET);
        return wrappedRepository.findAll(dataroomQuerySpec);
    }

    @Override
    public ResourceList<T> findAll(Collection<I> ids, QuerySpec querySpec) {
        QuerySpec dataroomQuerySpec = matcher.filter(querySpec, HttpMethod.GET);
        return wrappedRepository.findAll(ids, dataroomQuerySpec);
    }

    @Override
    public <S extends T> S save(S entity) {
        RegistryEntry entry = resourceRegistry.getEntry(getResourceClass());
        PreconditionUtil.verify(entry != null, "unknown resource class %s", getResourceClass());
        ResourceInformation resourceInformation = entry.getResourceInformation();
        I id = (I) resourceInformation.getId(entity);
        PreconditionUtil.verify(id != null, "entity %s cannot be saved without id", entity);
        T currentEntity = wrappedRepository.findOne(id, new QuerySpec(getResourceClass()));

        // TODO causes issues in JPA => try to improve (detect what changes and whether needs checking)
        matcher.verifyMatch(currentEntity, HttpMethod.PATCH);
        if (currentEntity == entity) {
            LOGGER.warn("saved resource {} should be same as returned by findOne. Make sure finders" +
                    " return a current instance from the underlying data source free of any ongoing " +
                    "changes. JPA/EntityMangaer are an example where this is violated by default " +
                    "due to the managed nature of entities. As alternative solution fields can be " +
                    "made non-patchable with @JsonApiField and this error silenced", entity);
        }

        matcher.verifyMatch(entity, HttpMethod.PATCH);
        return wrappedRepository.save(entity);
    }

    @Override
    public <S extends T> S create(S entity) {
        matcher.verifyMatch(entity, HttpMethod.POST);
        return wrappedRepository.create(entity);
    }

    @Override
    public void delete(I id) {
        T resource = findOne(id, new QuerySpec(getResourceClass()));
        matcher.verifyMatch(resource, HttpMethod.DELETE);
        wrappedRepository.delete(id);
    }

    @Override
    public void setResourceRegistry(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }
}
