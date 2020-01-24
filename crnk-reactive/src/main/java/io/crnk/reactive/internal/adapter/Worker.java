package io.crnk.reactive.internal.adapter;

import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.result.Result;
import io.crnk.reactive.internal.MonoResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.concurrent.Callable;

/**
 * Runs a callable within a worker thread and ensures the HttpRequestContext is assigned to that worker thread.
 */
class Worker {

    private static final Logger LOGGER = LoggerFactory.getLogger(Worker.class);

    private final HttpRequestContextProvider requestContextProvider;
    private final Scheduler scheduler;

    public Worker(HttpRequestContextProvider requestContextProvider, Scheduler scheduler) {
        this.requestContextProvider = requestContextProvider;
        this.scheduler = scheduler;
    }

    public <T> Result<T> work(Callable<Result<T>> callable) {

        LOGGER.debug("performing work {}", callable);
        return requestContextProvider.getRequestContextResult().merge(requestContext -> {
            Mono<T> mono = Mono.fromCallable(() -> {
                boolean hasContext = requestContextProvider.hasThreadRequestContext();
                if (!hasContext) {
                    requestContextProvider.onRequestStarted(requestContext);
                }
                try {
                    T response = callable.call().get();
                    return response;
                } finally {
                    if (!hasContext) {
                        requestContextProvider.onRequestFinished();
                    }
                }
            });
            return new MonoResult<>(mono.publishOn(scheduler));
        });
    }
}
