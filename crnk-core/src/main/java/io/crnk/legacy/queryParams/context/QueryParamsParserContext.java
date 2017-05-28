package io.crnk.legacy.queryParams.context;

import io.crnk.core.engine.information.resource.ResourceInformation;

import java.util.Set;

/**
 * Supplies information about the query parameters of the incoming request. This
 * information is then used by QueryParamsParsers to create QueryParams objects.
 *
 * @deprecated make use of QuerySpec
 */
@Deprecated
public interface QueryParamsParserContext {

	/**
	 * Returns the set of legacy values that match the given query legacy
	 * name of the current request.
	 */
	Set<String> getParameterValue(String parameterName);

	/**
	 * Returns the set of query legacy names associated to the current
	 * request.
	 */
	Iterable<String> getParameterNames();

	/**
	 * Returns ResourceInformation for the primary document of the current
	 * request.
	 */
	ResourceInformation getRequestedResourceInformation();
}
