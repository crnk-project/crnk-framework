package io.crnk.core.queryspec;

import io.crnk.core.queryspec.paging.PagingSpecSerializer;

import java.util.Map;
import java.util.Set;

/**
 * Converts {@link QuerySpec} into URL parameters.
 */
public interface QuerySpecSerializer {

	Map<String, Set<String>> serialize(QuerySpec querySpec);

	void setPagingSpecSerializer(PagingSpecSerializer serializer);

	PagingSpecSerializer getPagingSpecSerializer();
}
