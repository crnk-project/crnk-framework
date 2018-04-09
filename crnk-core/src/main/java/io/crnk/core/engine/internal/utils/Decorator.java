package io.crnk.core.engine.internal.utils;

/**
 * Generic interface for decorates decorating an other object.
 *
 * @param <T> type
 */
public interface Decorator<T> {

	/**
	 * Set upon construction.
	 *
	 * @param object
	 */
	@Deprecated
	default void setDecoratedObject(T object) {

	}
}
