package io.crnk.core.engine.filter;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;

public interface FilterBehaviorDirectory {

	/**
	 * Checks all {@link RepositoryFilter} whether the given type is filtered.
	 */
	FilterBehavior get(ResourceInformation resourceInformation, HttpMethod method);

	/**
	 * Checks all {@link RepositoryFilter} whether the given field is filtered.
	 */
	FilterBehavior get(ResourceField field, HttpMethod method);
}
