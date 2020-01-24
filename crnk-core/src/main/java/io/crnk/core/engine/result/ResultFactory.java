package io.crnk.core.engine.result;

import java.util.List;

public interface ResultFactory {

	<T> Result<T> just(T object);

	<T> Result<List<T>> zip(List<Result<T>> results);

	boolean isAsync();

	Object getThreadContext();

	Result<Object> getContext();

	void setThreadContext(Object context);

	void clearContext();

	boolean hasThreadContext();

	<T> Result<T> attachContext(Result<T> result, Object context);

	<T> Result<List<T>> all(List<Result<T>> results);
}
