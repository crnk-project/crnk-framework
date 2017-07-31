package io.crnk.core.resource;

public enum RestrictedQueryParamsMembers {
	/**
	 * Set of collection's fields used for filtering
	 */
	filter, // NOSONAR ok in this case
	/**
	 * Set of collection's fields used for sorting
	 */
	sort, // NOSONAR ok in this case
	/**
	 * Field to group by the collection
	 */
	@Deprecated
	group, // NOSONAR ok in this case
	/**
	 * Pagination properties
	 */
	page,// NOSONAR ok in this case
	/**
	 * List of specified fields to include in models
	 */
	fields,// NOSONAR ok in this case
	unknown, /**
	 * Additional resources that should be attached to response
	 */
	include// NOSONAR ok in this case
}
