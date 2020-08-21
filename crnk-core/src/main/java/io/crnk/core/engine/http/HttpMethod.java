package io.crnk.core.engine.http;

/**
 * Due to no RESTful dependencies, crnk doesn't have any place to store a list of available HTTP methods, so
 * when referring to HTTP methods, this enum should be used.
 */
public enum HttpMethod {
	GET,
	POST,
	DELETE,
	PATCH,
	PUT,
	OPTIONS,
	HEAD
}
