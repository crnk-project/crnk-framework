package io.crnk.core.engine.filter;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.internal.dispatcher.controller.BaseController;

/**
 * Manages the chain of filters and their application to a request.
 */
public interface DocumentFilterChain {

	/**
	 * Executes the next filter in the request chain or the actual {@link BaseController} once all filters
	 * have been invoked.
	 *
	 * @param filterRequestContext request context
	 * @return new execution context
	 */
	Response doFilter(DocumentFilterContext filterRequestContext);
}
