package io.crnk.core.engine.filter;

import io.crnk.core.resource.meta.MetaInformation;

import java.util.Collection;

/**
 * Manages the chain of repository filters to resolve meta information.
 */
public interface RepositoryMetaFilterChain {

	/**
	 * Invokes the next filter in the chain or the actual repository once all filters
	 * have been invoked.
	 *
	 * @param context   holding the request and other information.
	 * @param resources for which to compute the meta information (as a whole, not for the individual items)
	 * @return filtered meta information
	 */
	<T> MetaInformation doFilter(RepositoryFilterContext context, Collection<T> resources);

}
