package io.crnk.core.engine.filter;

import io.crnk.core.repository.response.JsonApiResponse;

/**
 * Manages the chain of filters and their application to a request.
 */
public interface RepositoryRequestFilterChain {

	/**
	 * Invokes the next filter in the chain or the actual repository once all filters
	 * have been invoked.
	 *
	 * @param filterRequestContext request context
	 */
	JsonApiResponse doFilter(RepositoryFilterContext context);

}
