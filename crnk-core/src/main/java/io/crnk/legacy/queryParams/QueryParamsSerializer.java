package io.crnk.legacy.queryParams;

import java.util.Map;
import java.util.Set;

/**
 * Converts {@link QueryParams} into URL parameters.
 *
 * @deprecated make use of QuerySpec
 */
@Deprecated
public interface QueryParamsSerializer {

	Map<String, Set<String>> serializeFilters(QueryParams queryParams);

	Map<String, Set<String>> serializeGrouping(QueryParams queryParams);

	Map<String, String> serializeSorting(QueryParams queryParams);

	Map<String, Set<String>> serializeIncludedFields(QueryParams queryParams);

	Map<String, Set<String>> serializeIncludedRelations(QueryParams queryParams);

	Map<String, String> serializePagination(QueryParams queryParams);

}
