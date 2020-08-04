package io.crnk.core.boot;


import io.crnk.core.engine.properties.ResourceFieldImmutableWriteBehavior;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;

public class CrnkProperties {


    private CrnkProperties() {
    }

    /**
     * If set to &quot;true&quot; it will name all @JsonApiId-annotated field "id" on the rest layer. Not
     * only for the resource data itself, but also for sort and filter parameters. Historically this has not been enabled,
     * but maybe worth to enable by default in Crnk 3.0 (TODO crnk3).
     */
    public static final String ENFORCE_ID_NAME = "crnk.enforceIdName";

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
     * Specifies the default {@link LookupIncludeBehavior} to be used for all relationships. For more information see
     * {@link JsonApiRelation#lookUp()}.
     */
    public static final String DEFAULT_LOOKUP_BEHAVIOR = "crnk.config.lookup.behavior.default";

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
     * See {@link ResourceFieldImmutableWriteBehavior}.
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
     * Set a boolean whether Crnk should allow pagination on inclusions. Most to all times application will choose to do
     * a second request. By default this is disabled.
     * </p>
     * <p>
     * Note that it is currently not possible to specify different paging parameters for the same entity when
     * it is the root resp. an included entity. This can lead to confusion if inclusions are cyclic (not that uncommon).
     * For this reason support is disabled by default.
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
     * Set a boolean whether Crnk should allow unknown parameters in query parameters.
     * </p>
     */
    public static final String ALLOW_UNKNOWN_PARAMETERS = "crnk.config.resource.request.allowUnknownParameters";

    /**
     * <p>
     * Set a boolean whether Crnk should links should be serialized as JSON objects.
     * </p>
     */
    public static final String SERIALIZE_LINKS_AS_OBJECTS = "crnk.config.serialize.object.links";

    /**
     * <p>Set a boolean whether Crnk should reject <code>application/json</code> requests to JSON-API endpoints.
     * Defaults to <code>false</code>.
     * </p><p>The
     * JSON-API specification mandates the use of the <code>application/vnd.api+json</code> MIME-Type. In cases where
     * frontends or
     * intermediate proxies prefer the <code>application/json</code> MIME-Type, that type can be sent in the <code>Accept</code>
     * header instead.</p>
     * <p>If an application wants to serve a different response depending on whether the client's <code>Accept</code> header
     * contains <code>application/vnd.api+json</code> or <code>application/json</code>, this option can be enabled.</p>
     * <p>This <strong>does not affect the <em>payload</em> <code>Content-Type</code></strong>. This means that the response will
     * still have <code>Content-Type: application/vnd.api+json</code> and that <code>POST/PATCH</code> requests, too, need to set
     * <code>Content-Type: application/vnd.api+json</code> to describe their request body.</p>
     *
     * @since 2.4
     */
    public static final String REJECT_PLAIN_JSON = "crnk.config.resource.request.rejectPlainJson";

    /**
     * <center><strong>Setting this option is not recommended.</strong></center><br>
     * <p>Missing resources should be avoided, please use placeholders instead.</p>
     * <p>
     * Set a boolean whether Crnk should throw
     * {@link io.crnk.core.exception.ResourceNotFoundException ResourceNotFoundException} if related resources are missing.
     * </p>crnk.config.resource.request.filterCriteriaInHttp
     * <p>Set this to <code>false</code> to ignore missing related resources.</p>
     */
    public static final String EXCEPTION_ON_MISSING_RELATED_RESOURCE = "crnk.config.serialize.relation.exceptionOnMissingRelatedResource";

	/**
	 * <p>Set a boolean if crnk should send filter criteria in the HTTP GET request body.</p>
	 * <p>By default and in compliance with the JSON API Spec, the filter has to be sent as query param in the URL.
	 * But this might result in errors related to the maximum supported length of URLs, especially with nested filters.
	 * Sending the parameters in the request body will allow for longer, more complex filters.</p>
	 */
	public static final String FILTER_CRITERIA_IN_HTTP_BODY = "crnk.config.resource.request.filterCriteriaInHttpBody";

}
