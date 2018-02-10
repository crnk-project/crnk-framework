package io.crnk.core.queryspec.paging;

import io.crnk.core.queryspec.DefaultQuerySpecDeserializer;

public interface PagingSpecDeserializer {

	/**
	 * Initializes a {@link PagingSpec} instance
	 *
	 * @return Instance with default values
	 */
	PagingSpec init();

	/**
	 * Provides deserialization logics of a single iteration
	 *
	 * @param pagingSpec {@link PagingSpec} instance to fill out
	 * @param parameter {@link io.crnk.core.queryspec.DefaultQuerySpecDeserializer.Parameter} object to deserialize
	 */
	void deserialize(PagingSpec pagingSpec, DefaultQuerySpecDeserializer.Parameter parameter);
}
