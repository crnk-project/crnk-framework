package io.crnk.core.engine.filter;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryContext;

public interface ResourceFilterDirectory {

	/**
	 * Checks all {@link RepositoryFilter} whether the given type is filtered.
	 *
	 * @deprecated pass QueryContext as well to work in a reactive setup
	 */
	@Deprecated
	FilterBehavior get(ResourceInformation resourceInformation, HttpMethod method);

	/**
	 * Checks all {@link RepositoryFilter} whether the given field is filtered.
	 *
	 * @deprecated pass QueryContext as well to work in a reactive setup
	 */
	@Deprecated
	FilterBehavior get(ResourceField field, HttpMethod method);

	/**
	 * Checks all {@link RepositoryFilter} whether the given type is filtered.
	 */
	FilterBehavior get(ResourceInformation resourceInformation, HttpMethod method, QueryContext queryContext);

	/**
	 * Checks all {@link RepositoryFilter} whether the given field is filtered.
	 */
	FilterBehavior get(ResourceField field, HttpMethod method, QueryContext queryContext);
}
