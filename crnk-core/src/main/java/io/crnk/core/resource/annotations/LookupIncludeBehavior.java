package io.crnk.core.resource.annotations;

/**
 * Defines the relationship look up strategy for a resource(s) relationship field.
 *
 * @see JsonApiRelation
 * @since 3.0
 */
public enum LookupIncludeBehavior {
	/**
	 * Defines that relationship resource is never called.
	 */
	NONE,
	/**
	 * Defines that relationship resource is called if the field is null.
	 */
	AUTOMATICALLY_WHEN_NULL,
	/**
	 * Defines that relationship resource is always called.
	 */
	AUTOMATICALLY_ALWAYS
}
