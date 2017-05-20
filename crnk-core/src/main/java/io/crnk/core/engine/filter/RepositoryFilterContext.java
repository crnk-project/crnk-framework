package io.crnk.core.engine.filter;

import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;

/**
 * Provides context information about Crnk and the current request for
 * {@link RepositoryFilter}. See {@link RepositoryFilter} for a higher-level
 * filter closer to the actual repositories.
 */
public interface RepositoryFilterContext {

	/**
	 * @return information about the request
	 */
	RepositoryRequestSpec getRequest();
}
