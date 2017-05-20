package io.crnk.core.engine.filter;

import io.crnk.core.resource.links.LinksInformation;

/**
 * Manages the chain of repository filters to resolve links.
 */
public interface RepositoryLinksFilterChain {

	/**
	 * Invokes the next filter in the chain or the actual repository once all filters
	 * have been invoked.
	 *
	 * @param context   holding the request and other information.
	 * @param resources for which to compute the links information (as a whole, not for the individual items)
	 * @return filtered links information
	 */
	<T> LinksInformation doFilter(RepositoryFilterContext context, Iterable<T> resources);

}
