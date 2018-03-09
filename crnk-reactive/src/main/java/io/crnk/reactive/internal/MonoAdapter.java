package io.crnk.reactive.internal;

import io.crnk.core.engine.result.Result;
import reactor.core.publisher.Mono;

public class MonoAdapter {

	public static <T> Mono<T> adapt(Result<T> mono) {
		return null; // FIXME
	}
}
