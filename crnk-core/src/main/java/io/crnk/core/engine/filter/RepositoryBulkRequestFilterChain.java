package io.crnk.core.engine.filter;

import io.crnk.core.repository.response.JsonApiResponse;

import java.util.Map;

/**
 * Manages the chain of repository filters to perform a bulk request.
 *
 * @param <K> key type used to distinguish bulk request items.
 */
public interface RepositoryBulkRequestFilterChain<K> {

	/**
	 * Invokes the next filter in the chain or the actual repository once all filters
	 * have been invoked.
	 *
	 * @param context holding the request and other information.
	 */
	Map<K, JsonApiResponse> doFilter(RepositoryFilterContext context);

}
