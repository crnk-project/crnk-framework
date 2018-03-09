package io.crnk.reactive.repository.filter;

import io.crnk.core.engine.filter.RepositoryFilterContext;
import io.crnk.core.repository.response.JsonApiResponse;
import reactor.core.publisher.Mono;

public interface ReactiveRepositoryFilter {

	Mono<JsonApiResponse> filterAccess(RepositoryFilterContext context, Mono<JsonApiResponse> response);


}
