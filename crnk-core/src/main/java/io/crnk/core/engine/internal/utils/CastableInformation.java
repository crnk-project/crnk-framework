package io.crnk.core.engine.internal.utils;

/**
 * Used to convert generic json meta and links information to typed ones.
 *
 * @param <T> type of information
 */
public interface CastableInformation<T> {

	<L extends T> L as(Class<L> linksClass);
}
