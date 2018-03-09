package io.crnk.reactive.engine.document;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.internal.dispatcher.controller.BaseController;
import reactor.core.publisher.Mono;

/**
 * Manages the chain of filters and their application to a request.
 */
public interface ReactiveDocumentFilterChain {

	/**
	 * Executes the next filter in the request chain or the actual {@link BaseController} once all filters
	 * have been invoked.
	 *
	 * @param filterContext
	 * @return new execution context
	 */
	Mono<Response> doFilter(DocumentFilterContext filterContext);
}
