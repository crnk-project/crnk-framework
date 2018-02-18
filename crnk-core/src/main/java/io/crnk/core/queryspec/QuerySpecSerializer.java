package io.crnk.core.queryspec;

import java.util.Map;
import java.util.Set;

/**
 * Converts {@link QuerySpec} into URL parameters.
 */
public interface QuerySpecSerializer {

	Map<String, Set<String>> serialize(QuerySpec querySpec);
}
