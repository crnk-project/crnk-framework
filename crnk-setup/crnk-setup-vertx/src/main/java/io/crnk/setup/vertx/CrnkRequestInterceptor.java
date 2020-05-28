package io.crnk.setup.vertx;

import io.crnk.core.engine.http.HttpRequestContext;
import reactor.core.publisher.Mono;

public interface CrnkRequestInterceptor {

    Mono<HttpRequestContext> onRequest(Mono<HttpRequestContext> mono);
}
