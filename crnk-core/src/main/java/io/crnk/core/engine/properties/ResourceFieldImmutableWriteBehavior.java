package io.crnk.core.engine.properties;

import io.crnk.core.boot.CrnkProperties;

/**
 * Determines how to deal with field that cannot be changed upon a PATCH or POST
 * request. See also {@link io.crnk.core.resource.annotations.JsonApiField}, {@link com.fasterxml.jackson.annotation.JsonProperty} and
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
	 * Throws a {@link io.crnk.core.exception.BadRequestException} when attempting to change an
	 * field with a POST and PATCH request that cannot be changed.
	 */
	FAIL

}
