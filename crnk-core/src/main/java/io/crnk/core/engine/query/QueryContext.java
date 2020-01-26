package io.crnk.core.engine.query;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.resource.annotations.JsonApiVersion;

/**
 * Holds context information about the current request.
 */
public class QueryContext {

	private String baseUrl;

	private Map<String, Object> attributes = new ConcurrentHashMap<>();

	private String requestPath;

	private int requestVersion = -1;

	private HttpRequestContext requestContext;

	/**
	 * @return requestContext underlying this query giving access to lower-layer transport layer.
	 */
	public HttpRequestContext getRequestContext() {
		return requestContext;
	}

	/**
	 * @param requestContext underlying this query giving access to lower-layer transport layer.
	 */
	public void setRequestContext(HttpRequestContext requestContext) {
		this.requestContext = requestContext;
	}

	/**
	 * @return version used to serve this request. See {@link JsonApiVersion}.
	 */
	public int getRequestVersion() {
		return requestVersion;
	}

	public QueryContext setRequestVersion(int requestVersion) {
		this.requestVersion = requestVersion;
		return this;
	}

	/**
	 * @return base URL requested by the client. Does not include repository paths and URL parameters.
	 */
	public String getBaseUrl() {
		return Objects.requireNonNull(baseUrl);
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	/**
	 * @return attribute with given key associated to the current request. Can be used, for example, to hold
	 * the current user principal.
	 */
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	public void setAttribute(String key, Object value) {
		attributes.put(key, value);
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	/**
	 * @return path to the requested repository based from {@link #getBaseUrl()}.
	 */
	public String getRequestPath() {
		return requestPath;
	}

	public void setRequestPath(String requestPath) {
		this.requestPath = requestPath;
	}

	public void initializeDefaults(ResourceRegistry resourceRegistry) {
		// serve latest version if no other is specified
		if (getRequestVersion() == -1) {
			setRequestVersion(resourceRegistry.getLatestVersion());
		}
	}
}
