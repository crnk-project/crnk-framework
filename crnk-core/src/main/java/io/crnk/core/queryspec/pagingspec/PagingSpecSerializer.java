package io.crnk.core.queryspec.pagingspec;

import java.util.Map;
import java.util.Set;

public interface PagingSpecSerializer<T extends PagingSpec> {

	/**
	 * Provides serialization logic
	 *
	 * @param pagingSpec {@link PagingSpec} instance to deserialize
	 * @param resourceType Resource type
	 * @param map Map object to fill out
	 */
	 void serialize(T pagingSpec, String resourceType, Map<String, Set<String>> map);
}
