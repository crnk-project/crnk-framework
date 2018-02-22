package io.crnk.core.queryspec.pagingspec;

import java.util.Map;
import java.util.Set;

public interface PagingBehavior<T extends PagingSpec> {

	/**
	 * Provides serialization logic
	 *
	 * @param pagingSpec {@link PagingSpec} instance to deserialize
	 * @param resourceType Resource type
	 * @return map Filled out map object
	 */
	Map<String, Set<String>> serialize(T pagingSpec, String resourceType);

	/**
	 * Provides deserialization logic of a single iteration
	 *
	 * @param parameters Map of parameters to deserialize
	 * @return Filled out {@link PagingSpec} instance
	 * @throws {@link io.crnk.core.exception.ParametersDeserializationException} in case of deserialization error
	 */
	T deserialize(Map<String, Set<String>> parameters);

	/**
	 * Creates a new instance of {@link PagingSpec}
	 * @return New {@link PagingSpec} instance
	 */
	T createEmptyPagingSpec();
}
