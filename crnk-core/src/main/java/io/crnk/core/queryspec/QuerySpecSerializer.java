package io.crnk.core.queryspec;

import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;

import java.util.Map;
import java.util.Set;

/**
 * Converts {@link QuerySpec} into URL parameters.
 *
 * @deprecated use {@link QuerySpecUrlMapper}
 */
public interface QuerySpecSerializer {

	Map<String, Set<String>> serialize(QuerySpec querySpec);

}
