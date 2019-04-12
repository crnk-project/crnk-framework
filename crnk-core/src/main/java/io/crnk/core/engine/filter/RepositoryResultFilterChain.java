package io.crnk.core.engine.filter;

import java.util.Collection;

/**
 * Manages the chain of repository filters to perform result filtering.
 */
public interface RepositoryResultFilterChain<T> {

    /**
     * Invokes the next filter in the chain or the actual repository once all filters
     * have been invoked.
     *
     * @param context holding the request and other information.
     * @return filtered result
     */
    Collection<T> doFilter(RepositoryFilterContext context);

}
