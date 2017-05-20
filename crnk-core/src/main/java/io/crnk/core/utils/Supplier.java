package io.crnk.core.utils;

public interface Supplier<T> {

	/**
	 * Gets a result.
	 *
	 * @return a result
	 */
	T get();
}
