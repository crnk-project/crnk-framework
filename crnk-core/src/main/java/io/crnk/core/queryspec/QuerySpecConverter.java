package io.crnk.core.queryspec;

import io.crnk.legacy.queryParams.QueryParams;

/**
 * Converts QueryParams to QuerySpec to ease legacy handling.
 *
 * @deprecated no longer needed in the future
 */
@Deprecated
public interface QuerySpecConverter {

	/**
	 * @param rootType type of the root resources being requested
	 * @param params   the QueryParams to be converted
	 * @return QuerySpec
	 */
	QuerySpec fromParams(Class<?> rootType, QueryParams params);

}
