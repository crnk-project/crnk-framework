package io.crnk.reactive.internal;

import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ResultFactory;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation based on Reactor library. Attempts to pass along request context in subscriber context using {@value SUBSCRIBER_CONTEXT_KEY}.
 */
public class MonoResultFactory implements ResultFactory {

	public static final String SUBSCRIBER_CONTEXT_KEY = "crnkContext";

	private ThreadLocal threadLocal = new ThreadLocal();

	@Override
	public <T> Result<T> just(T object) {
		Mono<T> mono = Mono.just(object);
		return toResult(mono);
	}

	private <T> Result<T> toResult(Mono<T> mono) {
		Object context = threadLocal.get();
		if (context != null) {
			mono.subscriberContext(it -> it.put(SUBSCRIBER_CONTEXT_KEY, context));
		}
		return new MonoResult(mono);
	}

	@Override
	public <T> Result<List<T>> zip(List<Result<T>> results) {
		if (results.isEmpty()) {
			return just(new ArrayList<>());
		}
		List<Mono<T>> monos = new ArrayList<>();
		for (Result<T> result : results) {
			monos.add(((MonoResult) result).getMono());
		}
		Mono<List<T>> zipped = Mono.zip(monos, a -> Arrays.asList((T[]) a));
		return toResult(zipped);
	}

	@Override
	public boolean isAsync() {
		return true;
	}

	@Override
	public Object getThreadContext() {
		Object context = threadLocal.get();
		if (context == null) {
			throw new UnsupportedOperationException("context not available");
		}
		return context;
	}

	@Override
	public boolean hasThreadContext() {
		return threadLocal.get() != null;
	}

	@Override
	public <T> Result<T> attachContext(Result<T> result, Object context) {
		MonoResult monoResult = (MonoResult) result;
		return new MonoResult<>(monoResult.getMono().subscriberContext(Context.of(SUBSCRIBER_CONTEXT_KEY, context)));
	}

	@Override
	public Result<Object> getContext() {
		return new MonoResult(Mono.subscriberContext().map(it -> it.get(SUBSCRIBER_CONTEXT_KEY)));
	}

	@Override
	public void setThreadContext(Object context) {
		threadLocal.set(context);
	}

	@Override
	public void clearContext() {
		threadLocal.remove();
	}
}
