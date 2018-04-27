package io.crnk.core.queryspec;

import java.util.Map;
import java.util.Set;

import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;

/**
 * Converts {@link QuerySpec} into URL parameters.
 *
 * @deprecated use {@link QuerySpecUrlMapper}
 */
public interface QuerySpecSerializer {

	Map<String, Set<String>> serialize(QuerySpec querySpec);

}
