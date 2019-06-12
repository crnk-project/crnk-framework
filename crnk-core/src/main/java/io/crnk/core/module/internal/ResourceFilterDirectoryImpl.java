package io.crnk.core.module.internal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilter;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.ForbiddenException;
import io.crnk.core.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceFilterDirectoryImpl implements ResourceFilterDirectory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceFilterDirectoryImpl.class);

    private final List<ResourceFilter> filters;

    private final HttpRequestContextProvider requestContextProvider;

    private final ResourceRegistry resourceRegistry;


    public ResourceFilterDirectoryImpl(List<ResourceFilter> filters, HttpRequestContextProvider requestContextProvider,
                                       ResourceRegistry resourceRegistry) {
        this.filters = filters;
        this.requestContextProvider = requestContextProvider;
        this.resourceRegistry = resourceRegistry;
    }

    @Override
    public FilterBehavior get(ResourceInformation resourceInformation, HttpMethod method, QueryContext queryContext) {
        Map<Object, FilterBehavior> map = getCache(method, queryContext);
        if (queryContext != null) {
            FilterBehavior behavior = map.get(resourceInformation);
            if (behavior != null) {
                return behavior;
            }
        }

        FilterBehavior behavior = FilterBehavior.NONE;
        for (ResourceFilter filter : filters) {
            behavior = behavior.merge(filter.filterResource(resourceInformation, method));
            if (behavior == FilterBehavior.FORBIDDEN) {
                break;
            }
        }
        if (queryContext != null) {
            map.put(resourceInformation, behavior);
        }
        return behavior;
    }

    @Override
    public FilterBehavior get(ResourceField field, HttpMethod method, QueryContext queryContext) {
        Map<Object, FilterBehavior> map = getCache(method, queryContext);

        FilterBehavior behavior = map.get(field);
        if (behavior != null) {
            return behavior;
        }


        boolean modifiable = field.getAccess().allows(method);

        // TODO field.getAccess not fine-grained, should change in the future
        behavior = modifiable ? FilterBehavior.NONE : FilterBehavior.IGNORED;

        for (ResourceFilter filter : filters) {
            behavior = behavior.merge(filter.filterField(field, method));
            if (behavior == FilterBehavior.FORBIDDEN) {
                break;
            }
        }

        if (field.getResourceFieldType() == ResourceFieldType.RELATIONSHIP) {
            // for relationships opposite site must also be accessible (at least with GET)
            String oppositeResourceType = field.getOppositeResourceType();
            RegistryEntry oppositeRegistryEntry = resourceRegistry.getEntry(oppositeResourceType);
            if (oppositeRegistryEntry != null) {
                ResourceInformation oppositeResourceInformation = oppositeRegistryEntry.getResourceInformation();

                // consider checking more than GET? intersection/union of multiple?
                behavior = behavior.merge(get(oppositeResourceInformation, HttpMethod.GET, queryContext));
            } else {
                LOGGER.warn("opposite side {} not found", oppositeResourceType);
            }
        }

        map.put(field, behavior);
        return behavior;
    }


    private Map<Object, FilterBehavior> getCache(HttpMethod method, QueryContext queryContext) {
        if (queryContext == null) {
            return null; // no caching without context
        }
        String key = ResourceFilterDirectoryImpl.class.getSimpleName() + method;
        Map<Object, FilterBehavior> cache = (Map<Object, FilterBehavior>) queryContext.getAttribute(key);
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            queryContext.setAttribute(key, cache);
        }
        return cache;
    }

    /**
     * Allows to check whether the given field can be written.
     *
     * @param field from the information model or null if is a dynamic field (like JsonAny).
     */
    @Override
    public boolean canAccess(ResourceField field, HttpMethod method, QueryContext queryContext, boolean allowIgnore) {
        if (field == null) {
            return true;
        }
        FilterBehavior filterBehavior = get(field, method, queryContext);
        if (filterBehavior == FilterBehavior.NONE) {
            return true;
		} if (filterBehavior == FilterBehavior.FORBIDDEN || !allowIgnore) {
			String resourceType = field.getResourceInformation().getResourceType();
			throw new ForbiddenException("field '" + resourceType + "." + field.getJsonName() + "' cannot be accessed for " + method);
		} else if (filterBehavior == FilterBehavior.UNAUTHORIZED || !allowIgnore) {
			String resourceType = field.getResourceInformation().getResourceType();
			throw new UnauthorizedException("field '" + resourceType + "." + field.getJsonName() + "' can only be access when logged in for " + method);
		} else {
            LOGGER.debug("ignoring field {}", field.getUnderlyingName());
            PreconditionUtil.verifyEquals(FilterBehavior.IGNORED, filterBehavior, "unknown behavior");
            return false;
        }
    }
}
