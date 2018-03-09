package io.crnk.core.module.internal;

import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.filter.ResourceFilter;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceFilterDirectoryImpl implements ResourceFilterDirectory {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceFilterDirectoryImpl.class);

	private final List<ResourceFilter> filters;

	private final HttpRequestContextProvider requestContextProvider;

	private final ResourceRegistry resourceRegistry;

	public ResourceFilterDirectoryImpl(List<ResourceFilter> filters, HttpRequestContextProvider requestContextProvider, ResourceRegistry resourceRegistry) {
		this.filters = filters;
		this.requestContextProvider = requestContextProvider;
		this.resourceRegistry = resourceRegistry;
	}

	@Override
	@Deprecated
	public FilterBehavior get(ResourceInformation resourceInformation, HttpMethod method) {
		return get(resourceInformation, method, requestContextProvider.getRequestContext().getQueryContext());
	}

	@Override
	public FilterBehavior get(ResourceInformation resourceInformation, HttpMethod method, QueryContext queryContext) {
		Map<Object, FilterBehavior> map = getCache(method, queryContext);

		FilterBehavior behavior = map.get(resourceInformation);
		if (behavior != null) {
			return behavior;
		}

		behavior = FilterBehavior.NONE;
		for (ResourceFilter filter : filters) {
			behavior = behavior.merge(filter.filterResource(resourceInformation, method));
			if (behavior == FilterBehavior.FORBIDDEN) {
				break;
			}
		}
		map.put(resourceInformation, behavior);
		return behavior;
	}

	@Override
	@Deprecated
	public FilterBehavior get(ResourceField field, HttpMethod method) {
		return get(field, method, requestContextProvider.getRequestContext().getQueryContext());
	}

	@Override
	public FilterBehavior get(ResourceField field, HttpMethod method, QueryContext queryContext) {
		Map<Object, FilterBehavior> map = getCache(method, queryContext);

		FilterBehavior behavior = map.get(field);
		if (behavior != null) {
			return behavior;
		}

		behavior = FilterBehavior.NONE;
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
				PreconditionUtil.assertNotNull(oppositeResourceType, oppositeRegistryEntry);
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
		String key = ResourceFilterDirectoryImpl.class.getSimpleName() + method;
		if (queryContext == null) {
			return new HashMap<>(); // e.g. testing
		}
		Map<Object, FilterBehavior> cache = (Map<Object, FilterBehavior>) queryContext.getAttribute(key);
		if (cache == null) {
			cache = new ConcurrentHashMap<>();
			queryContext.setAttribute(key, cache);
		}
		return cache;
	}
}
