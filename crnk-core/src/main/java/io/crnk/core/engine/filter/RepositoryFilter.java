package io.crnk.core.engine.filter;

import io.crnk.core.repository.response.JsonApiResponse;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;

import java.util.Collection;
import java.util.Map;

/**
 * Allows to intercept calls to repositories by modules and make changes.
 */
public interface RepositoryFilter {

	/**
	 * Filters a regular request.
	 *
	 * @param context to access request and crnk information
	 * @param chain   to proceed to the next filter resp. actual repository.
	 * @return filtered result to be returned to next filter resp. caller
	 */
	JsonApiResponse filterRequest(RepositoryFilterContext context, RepositoryRequestFilterChain chain);

	/**
	 * Filters a bulk request (used to fetch included relationships).
	 *
	 * @param context to access request and crnk information
	 * @param chain   to proceed to the next filter resp. actual repository.
	 * @return filtered results to be returned to next filter resp. caller
	 */
	<K> Map<K, JsonApiResponse> filterBulkRequest(RepositoryFilterContext context, RepositoryBulkRequestFilterChain<K> chain);

	/**
	 * Filter a result, ban be either a single entity or collection.
	 *
	 * @param context to access request and crnk information
	 * @param chain   to proceed to the next filter resp. actual repository.
	 * @return filtered result to be returned to next filter resp. caller
	 */
	<T> Collection<T> filterResult(RepositoryFilterContext context, RepositoryResultFilterChain<T> chain);

	/**
	 * Filters the meta information.
	 *
	 * @param context to access request and crnk information
	 * @param chain   to proceed to the next filter resp. actual repository.
	 * @return filtered metaInformation to be returned to next filter resp. caller
	 */
	<T> MetaInformation filterMeta(RepositoryFilterContext context, Collection<T> resources, RepositoryMetaFilterChain chain);

	/**
	 * Filters the links information.
	 *
	 * @param context to access request and crnk information
	 * @param chain   to proceed to the next filter resp. actual repository.
	 * @return filtered linksInformation to be returned to next filter resp. caller
	 */
	<T> LinksInformation filterLinks(RepositoryFilterContext context, Collection<T> resources, RepositoryLinksFilterChain chain);

}
