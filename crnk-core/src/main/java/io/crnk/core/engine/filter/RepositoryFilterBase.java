package io.crnk.core.engine.filter;

import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

import java.util.Map;

/**
 * Base class for {@links RepositoryFilter} implementations doing nothing except forwarding the call to the next element in the filter chain.
 */
public class RepositoryFilterBase implements RepositoryFilter {

	@Override
	public <K> Map<K, JsonApiResponse> filterBulkRequest(RepositoryFilterContext context, RepositoryBulkRequestFilterChain<K> chain) {
		return chain.doFilter(context);
	}

	@Override
	public JsonApiResponse filterRequest(RepositoryFilterContext context, RepositoryRequestFilterChain chain) {
		return chain.doFilter(context);
	}

	@Override
	public <T> Iterable<T> filterResult(RepositoryFilterContext context, RepositoryResultFilterChain<T> chain) {
		return chain.doFilter(context);
	}

	@Override
	public <T> MetaInformation filterMeta(RepositoryFilterContext context, Iterable<T> resources,
										  RepositoryMetaFilterChain chain) {
		return chain.doFilter(context, resources);
	}

	@Override
	public <T> LinksInformation filterLinks(RepositoryFilterContext context, Iterable<T> resources,
											RepositoryLinksFilterChain chain) {
		return chain.doFilter(context, resources);
	}
}
