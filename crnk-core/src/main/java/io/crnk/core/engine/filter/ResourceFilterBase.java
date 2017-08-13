package io.crnk.core.engine.filter;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;

/**
 * Base class for {@links ResourceFilter} giving full access to all resources and fields.
 */
public class ResourceFilterBase implements ResourceFilter {

	@Override
	public FilterBehavior filterResource(ResourceInformation resourceInformation, HttpMethod method) {
		return FilterBehavior.NONE;
	}

	@Override
	public FilterBehavior filterField(ResourceField field, HttpMethod method) {
		return FilterBehavior.NONE;
	}
}
