package io.crnk.core.engine.filter;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;

/**
 * Allows to limit access to resource types and fields for a given user. The interface operates on the model/type-level
 * and is complemented by DataRoomFilter that further allows to limit visibility of individual resources.
 * <p>
 * Note that invocations to this methods are cached on a per-request basis.
 */
// tag::docs[]
public interface ResourceFilter {
	// end::docs[]

	/**
	 * Allows to filter the given type.
	 *
	 * @param resourceInformation to filter
	 * @param method              to which to apply this filter to
	 */
	// tag::docs[]
	FilterBehavior filterResource(ResourceFilterContext filterContext, ResourceInformation resourceInformation, HttpMethod method);
	// end::docs[]

	/**
	 * Allows to filter the given field.
	 *
	 * @param field  to filter
	 * @param method to which to apply this filter to
	 */

	// tag::docs[]
	FilterBehavior filterField(ResourceFilterContext filterContext, ResourceField field, HttpMethod method);
}
// end::docs[]
