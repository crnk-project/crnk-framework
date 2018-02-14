package io.crnk.core.queryspec.paging;

import java.util.Set;

public interface PagingSpecDeserializer<T extends PagingSpec> {

	/**
	 * Initializes a {@link PagingSpec} instance
	 *
	 * @return Instance with default values
	 */
	T init();

	/**
	 * Provides deserialization logic of a single iteration
	 *
	 * @param pagingSpec {@link PagingSpec} instance to fill out
	 * @param name Parameter's name
	 * @param values Parameter's set of values
	 * @throws {@link io.crnk.core.exception.ParametersDeserializationException} in case of deserialization error
	 */
	void deserialize(T pagingSpec, String name, Set<String> values);
}
