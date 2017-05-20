package io.crnk.core.engine.properties;

import io.crnk.core.boot.CrnkProperties;

/**
 * Determines how to deal with field that cannot be changed upon a PATCH or POST
 * request. See also {@link JsonApiField}, {@link JsonProperty} and
 * {@value CrnkProperties#RESOURCE_FIELD_IMMUTABLE_WRITE_BEHAVIOR}} for more
 * information.
 */
public enum ResourceFieldImmutableWriteBehavior {

	/**
	 * Ignores fields in POST and PATCH requests that cannot be changed. This
	 * is the default.
	 */
	IGNORE,

	/**
	 * Throws a {@link BadRequestException} when attempting to change an
	 * field with a POST and PATCH request that cannot be changed.
	 */
	FAIL

}
