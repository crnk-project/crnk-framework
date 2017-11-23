package io.crnk.core.boot;


import io.crnk.core.engine.properties.ResourceFieldImmutableWriteBehavior;

public class CrnkProperties {

	private CrnkProperties() {
	}


	/**
	 * Set package to scan for resources, repositories and exception mappers.
	 * <p>
	 * It allows configuring from which package should be searched to get models, repositories used by the core and
	 * exception mappers used to map thrown from repositories exceptions.
	 * </p>
	 * <p>
	 * Multiple packages can be passed by specifying a comma separated string of packages.
	 * </p>
	 * <p>
	 * The name of the configuration property is <tt>{@value}</tt>.
	 * </p>
	 *
	 * @since 0.9.0
	 */
	public static final String RESOURCE_SEARCH_PACKAGE = "crnk.config.core.resource.package";

	/**
	 * Set default domain.
	 * <p>
	 * An URL assigned to this value will be added to all of the links returned by Crnk framework. The URL
	 * cannot end with slash.
	 * </p>
	 * <p>
	 * The name of the configuration property is <tt>{@value}</tt>.
	 * </p>
	 *
	 * @since 0.9.0
	 */
	public static final String RESOURCE_DEFAULT_DOMAIN = "crnk.config.core.resource.domain";

	/**
	 * Set prefix to be searched when performing method matching and building building <i>links</i> objects in
	 * responses.
	 * <p>
	 * <p>
	 * The name of the configuration property is <tt>{@value}</tt>.
	 * </p>
	 *
	 * @since 0.9.4
	 */
	public static final String WEB_PATH_PREFIX = "crnk.config.web.path.prefix";

	/**
	 * Set a boolean whether crnk will always try to look up a relationship field that has been included in the request.
	 * Refer to {@link io.crnk.core.resource.annotations.JsonApiLookupIncludeAutomatically} for only adding it to a specific
	 * field.
	 *
	 * @since 2.8.2
	 */
	public static final String INCLUDE_AUTOMATICALLY = "crnk.config.include.automatically";

	/**
	 * There are two mechanisms in place to determine whether an inclusion was
	 * requested.
	 * <p>
	 * <ul>
	 * <li>include[tasks]=project.schedule</li> (BY_ROOT_PATH)
	 * <li>include[tasks]=project&include[projects]=schedule</li> (BY_TYPE)
	 * </ul>
	 * <p>
	 * For simple object structures they are semantically the same, but they do differ
	 * for more complex ones, like when multiple attributes lead
	 * to the same type or for cycle structures. In the later case BY_TYPE inclusions
	 * become recursive, while BY_ROOT_PATH do not. Note that the use of BY_TYPE
	 * outmatches BY_ROOT_PATH, so BY_TYPE includes everything BY_ROOT_PATH does
	 * and potentially more.
	 * <p>
	 * Possible values: BY_TYPE (default), BY_ROOT_PATH
	 */
	public static final String INCLUDE_BEHAVIOR = "crnk.config.include.behavior";

	/**
	 * Set a boolean whether crnk will try to overwrite a value previously assigned
	 * to a relationship field that has been included in the request.
	 * Refer to {@link io.crnk.resource.annotations.JsonApiLookupIncludeAutomatically}.overwrite for only adding it to a
	 * specific field.
	 *
	 * @since 2.8.2
	 */
	public static final String INCLUDE_AUTOMATICALLY_OVERWRITE = "crnk.config.include.automatically.overwrite";

	/**
	 * See {@link ResourceFieldImmutableWriteBehavior}. By default
	 * {@value ResourceFieldImmutableWriteBehavior#IGNORE} is used.
	 */
	public static final String RESOURCE_FIELD_IMMUTABLE_WRITE_BEHAVIOR = "crnk.config.resource.immutableWrite";


	/**
	 * <p>
	 * Set a boolean whether Crnk should return a JSON API response with data field set to {@code null}
	 * if an action returns a {@code null} value.
	 * </p>
	 * <p>
	 * JAX-RS default default is to return a 204 response.
	 * </p>
	 */
	public static final String NULL_DATA_RESPONSE_ENABLED = "crnk.config.null.data.response.enabled";

	/**
	 * <p>
	 * 	 Set a boolean whether Crnk should allow pagination on inclusions. Most to all times application will choose to do
	 *   a second request. By default this is disabled.
	 * </p>
	 * <p>
	 *   Note that it is currently not possible to specify different paging parameters for the same entity when
	 *   it is the root resp. an included entity. This can lead to confusion if inclusions are cyclic (not that uncommon).
	 *   For this reason support is disabled by default.
	 * </p>
	 */
	public static final String INCLUDE_PAGING_ENABLED = "crnk.config.include.paging.enabled";

	/**
	 * <p>
	 * Set a boolean whether Crnk should return a 404 response (Resource not found) when a single resource is {@code null}
	 * </p>
	 */
	public static final String RETURN_404_ON_NULL = "crnk.config.resource.response.return_404";

	/**
	 * <p>
	 * Set a boolean whether Crnk should allow unknown attributes in query parameters.
	 * </p>
	 */
	public static final String ALLOW_UNKNOWN_ATTRIBUTES = "crnk.config.resource.request.allowUnknownAttributes";

	/**
	 * <p>
	 * Set a default page limit for Crnk responses.
	 * </p>
	 */
	public static final String DEFAULT_PAGE_LIMIT = "crnk.config.resource.request.defaultPageLimit";

	/**
	 * <p>
	 * Set a maximum allowed page limit for Crnk responses.
	 * </p>
	 */
	public static final String MAX_PAGE_LIMIT = "crnk.config.resource.request.maxPageLimit";
}
