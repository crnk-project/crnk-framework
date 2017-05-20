package io.crnk.core.engine.internal.utils;

/**
 * Generic interface for decorates decorating an other object.
 *
 * @param <T> type
 */
public interface Decorator<T> {

	void setDecoratedObject(T object);
}
