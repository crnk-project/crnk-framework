package io.crnk.legacy.queryParams;

import io.crnk.core.queryspec.QuerySpec;

/**
 * Converts QuerySpec to QueryParams to ease legacy handling.
 *
 * @deprecated no longer needed in the future
 */
@Deprecated
public interface QueryParamsConverter {

	/**
	 * @param rootType type of the root resources being requested
	 * @param spec     the QuerySpec to be converted
	 * @return QuerySpec
	 */
	QueryParams fromParams(Class<?> rootType, QuerySpec spec);

}