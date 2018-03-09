package io.crnk.reactive.internal;

import io.crnk.core.engine.result.Result;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class MonoResult<T> implements Result<T> {

	private final Mono<T> mono;

	public MonoResult(Mono<T> mono) {
		this.mono = mono;
	}

	@Override
	public T get() {
		return mono.block();
	}

	@Override
	public <D> Result<D> map(Function<T, D> function) {
		return new MonoResult(mono.map(function));
	}

	@Override
	public Result<T> onErrorResume(Function<? super Throwable, T> function) {
		Mono<T> resumed = mono.onErrorResume(it -> {
			T result = function.apply(it);
			return Mono.just(result);
		});
		return new MonoResult<>(resumed);
	}

	@Override
	public void subscribe(Consumer<T> consumer, Consumer<? super Throwable> exceptionConsumer) {
		mono.subscribe(consumer, exceptionConsumer);
	}

	@Override
	public Result<T> doWork(Consumer<T> function) {
		return new MonoResult<>(mono.doOnNext(function));
	}

	@Override
	public <D, R> Result<R> zipWith(Result<D> other, BiFunction<T, D, R> function) {
		MonoResult<D> otherMono = (MonoResult<D>) other;
		Mono<R> zipped = mono.zipWith(otherMono.mono, function);
		return new MonoResult<>(zipped);
	}

	@Override
	public <R> Result<R> merge(Function<T, Result<R>> other) {
		Mono<R> flatMapped = mono.flatMap(it -> {
			MonoResult<R> result = (MonoResult<R>) other.apply(it);
			return result.mono;
		});
		return new MonoResult<>(flatMapped);
	}

	public Mono<T> getMono() {
		return mono;
	}
}
