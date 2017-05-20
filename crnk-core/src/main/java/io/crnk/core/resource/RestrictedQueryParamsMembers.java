package io.crnk.core.resource;

public enum RestrictedQueryParamsMembers {
	/**
	 * Set of collection's fields used for filtering
	 */
	filter,
	/**
	 * Set of collection's fields used for sorting
	 */
	sort,
	/**
	 * Field to group by the collection
	 */
	group,
	/**
	 * Pagination properties
	 */
	page,
	/**
	 * List of specified fields to include in models
	 */
	fields,
	/**
	 * Additional resources that should be attached to response
	 */
	include
}
