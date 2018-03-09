package io.crnk.core.engine.result;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ImmediateResult<T> implements Result<T> {

	private T object;

	public ImmediateResult(T object) {
		this.object = object;
	}

	@Override
	public T get() {
		return object;
	}

	@Override
	public <D> Result<D> map(Function<T, D> function) {
		return new ImmediateResult<>(function.apply(object));
	}

	@Override
	public Result<T> onErrorResume(Function<? super Throwable, T> function) {
		throw new UnsupportedOperationException("only available for async implementations");
	}

	@Override
	public void subscribe(Consumer<T> consumer, Consumer<? super Throwable> exceptionConsumer) {
		throw new UnsupportedOperationException("only available for async implementations");
	}

	@Override
	public Result<T> doWork(Consumer<T> function) {
		function.accept(object);
		return this;
	}

	@Override
	public <D, R> Result<R> zipWith(Result<D> other, BiFunction<T, D, R> function) {
		D otherObject = other.get();
		return new ImmediateResult<>(function.apply(object, otherObject));
	}

	@Override
	public <R> Result<R> merge(Function<T, Result<R>> other) {
		return other.apply(object);
	}

}
