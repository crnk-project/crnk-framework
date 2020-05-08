package io.crnk.core.queryspec.mapper;

import java.util.Set;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.queryspec.QuerySpec;

/**
 * Allows the contruction of an url out of a QuerySpec
 */
public interface UrlBuilder {

	/**
	 * Parameter propagation allows to pass along non-JSON-API parameters to a repository and include it in all the computed links (paging, self, related). Use case is, for
	 * example, to allow passing security-related parameters in the URL rather than request headers, improving usability for developers (like the CSRF token, but be careful
	 * here...).
	 *
	 * @param name of parameter
	 */
	void addPropagatedParameter(String name);

	/**
	 * {@link #addPropagatedParameter(String)}
	 */
	Set<String> getPropagatedParameters();

	String buildUrl(QueryContext queryContext, Object resource);

	String buildUrl(QueryContext queryContext, Object resource, QuerySpec querySpec);

	String buildUrl(QueryContext queryContext, Object resource, QuerySpec querySpec, String relationshipName);

	String buildUrl(QueryContext queryContext, Object resource, QuerySpec querySpec, String relationshipName, boolean selfLink);

	String buildUrl(QueryContext queryContext, ResourceInformation resourceInformation);

	String buildUrl(QueryContext queryContext, ResourceInformation resourceInformation, Object id, QuerySpec querySpec);

	String buildUrl(QueryContext queryContext, ResourceInformation resourceInformation, Object id, QuerySpec querySpec, String relationshipName);

	String buildUrl(QueryContext queryContext, ResourceInformation resourceInformation, Object id, QuerySpec querySpec, String relationshipName, boolean selfLink);

	/**
	 * Allows the {@link UrlBuilder} to intercept and adapt urls. There are use cases, where one may like to attach additional security parameters with every request. While most
	 * use cases rely on HTTP request headers, sometimes the offered functionality here can also proof useful.
	 *
	 * @param url
	 * @param queryContext
	 * @return filtered url
	 */
	String filterUrl(String url, QueryContext queryContext);
}
