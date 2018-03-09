package io.crnk.reactive.engine.document;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;
import reactor.core.publisher.Mono;

/**
 * Allows to intercept and modify incoming requests and responses. This is
 * a low-level interface getting called early with the actual
 * request data structures.
 */
public interface ReactiveDocumentFilter {

	/**
	 * Filters an incoming request. To continue processing the request, {@link DocumentFilterChain#doFilter(DocumentFilterContext)} must
	 * be called. Information about the request is available from {@link DocumentFilterContext}.
	 *
	 * @param filterContext request context
	 * @param chain         next filters
	 * @return response
	 */
	Mono<Response> filter(DocumentFilterContext filterContext, ReactiveDocumentFilterChain chain);

}
