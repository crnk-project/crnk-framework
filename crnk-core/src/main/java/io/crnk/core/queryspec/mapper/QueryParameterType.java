package io.crnk.core.queryspec.mapper;

public enum QueryParameterType {

	/**
	 * Set of collection's fields used for filtering
	 */
	FILTER,
	/**
	 * Set of collection's fields used for sorting
	 */
	SORT,

	/**
	 * Pagination properties
	 */
	PAGE,

	/**
	 * Used as fallback to content negotiation with HTTP ACCEPT header
	 */
	VERSION,

	/**
	 * List of specified fields to include in models
	 */
	FIELDS,

	/**
	 * Additional resources that should be attached to response
	 */
	INCLUDE,

	UNKNOWN
}
