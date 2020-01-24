package io.crnk.core.engine.filter;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryContext;

public interface ResourceFilterDirectory {

	/**
	 * Checks all {@link RepositoryFilter} whether the given type is filtered.
	 */
	FilterBehavior get(ResourceInformation resourceInformation, HttpMethod method, QueryContext queryContext);

	/**
	 * Checks all {@link RepositoryFilter} whether the given field is filtered.
	 */
	FilterBehavior get(ResourceField field, HttpMethod method, QueryContext queryContext);

	/**
	 * Verifies access to the given field. Returns true if allowed. Throws an exception if
	 * not allowed. And return false if ignored.
	 * @param field
	 * @param method
	 * @param queryContext
	 * @param allowIgnore
	 * @return
	 */
	boolean canAccess(ResourceField field, HttpMethod method, QueryContext queryContext, boolean allowIgnore);
}
