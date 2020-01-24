package io.crnk.core.engine.result;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes all operators on the current thread.
 */
public class ImmediateResultFactory implements ResultFactory {

	private ThreadLocal<Object> threadLocal = new ThreadLocal<>();

	@Override
	public <T> Result<T> just(T object) {
		if (object == null) {
			throw new IllegalArgumentException("cannot use null");
		}
		return new ImmediateResult<>(object);
	}

	@Override
	public <T> Result<List<T>> zip(List<Result<T>> results) {
		ArrayList<T> list = new ArrayList<>();
		for (Result<T> result : results) {
			list.add(result.get());
		}
		return new ImmediateResult<>(list);
	}

	@Override
	public boolean isAsync() {
		return false;
	}

	public Object getThreadContext() {
		return threadLocal.get();
	}

	@Override
	public Result<Object> getContext() {
		return new ImmediateResult<>(getThreadContext());
	}

	public void setThreadContext(Object context) {
		threadLocal.set(context);
	}

	public void clearContext() {
		threadLocal.remove();
	}

	@Override
	public boolean hasThreadContext() {
		return threadLocal.get() != null;
	}

	@Override
	public <T> Result<T> attachContext(Result<T> result, Object context) {
		return result;
	}

	@Override
	public <T> Result<List<T>> all(List<Result<T>> results) {
		List<T> list = new ArrayList<>();
		for (Result<T> result : results) {
			list.add(result.get());
		}
		return just(list);
	}
}
