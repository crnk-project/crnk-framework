package io.crnk.core.engine.internal.utils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.mapper.QuerySpecUrlMapper;
import io.crnk.core.queryspec.mapper.UrlBuilder;

public class JsonApiUrlBuilder implements UrlBuilder {


	private final ModuleRegistry moduleRegistry;

	private Set<String> propagatedParameters = new HashSet<>();

	public JsonApiUrlBuilder(ModuleRegistry moduleRegistry) {
		this.moduleRegistry = moduleRegistry;
	}

	@Override
	public void addPropagatedParameter(String name) {
		propagatedParameters.add(name);
	}

	@Override
	public Set<String> getPropagatedParameters() {
		return propagatedParameters;
	}

	@Override
	public String buildUrl(QueryContext queryContext, Object resource) {
		RegistryEntry entry = moduleRegistry.getResourceRegistry().findEntry(resource);
		ResourceInformation resourceInformation = entry.getResourceInformation();
		Object id = resourceInformation.getId(resource);
		return buildUrl(queryContext, resourceInformation, id, null);
	}

	@Override
	public String buildUrl(QueryContext queryContext, Object resource, QuerySpec querySpec) {
		RegistryEntry entry = moduleRegistry.getResourceRegistry().findEntry(resource);
		ResourceInformation resourceInformation = entry.getResourceInformation();
		Object id = resourceInformation.getId(resource);
		return buildUrl(queryContext, resourceInformation, id, querySpec);
	}

	@Override
	public String buildUrl(QueryContext queryContext, Object resource, QuerySpec querySpec, String relationshipName) {
		RegistryEntry entry = moduleRegistry.getResourceRegistry().findEntry(resource);
		ResourceInformation resourceInformation = entry.getResourceInformation();
		Object id = resourceInformation.getId(resource);
		return buildUrl(queryContext, resourceInformation, id, querySpec, relationshipName);
	}

	@Override
	public String buildUrl(QueryContext queryContext, Object resource, QuerySpec querySpec, String relationshipName, boolean selfLink) {
		RegistryEntry entry = moduleRegistry.getResourceRegistry().findEntry(resource);
		ResourceInformation resourceInformation = entry.getResourceInformation();
		Object id = resourceInformation.getId(resource);
		return buildUrl(queryContext, resourceInformation, id, querySpec, relationshipName, selfLink);
	}

	@Override
	public String buildUrl(QueryContext queryContext, ResourceInformation resourceInformation) {
		return buildUrl(queryContext, resourceInformation, null, null, null);
	}

	@Override
	public String buildUrl(QueryContext queryContext, ResourceInformation resourceInformation, Object id, QuerySpec querySpec) {
		return buildUrl(queryContext, resourceInformation, id, querySpec, null);
	}

	@Override
	public String buildUrl(QueryContext queryContext, ResourceInformation resourceInformation, Object id, QuerySpec querySpec, String relationshipName) {
		return buildUrlInternal(queryContext, resourceInformation, id, querySpec, relationshipName, true);
	}

	public String buildUrl(QueryContext queryContext, ResourceInformation resourceInformation, Object id, QuerySpec querySpec, String relationshipName, boolean selfLink) {
		return buildUrlInternal(queryContext, resourceInformation, id, querySpec, relationshipName, selfLink);
	}

	private String buildUrlInternal(QueryContext queryContext, ResourceInformation resourceInformation, Object id, Object query, String relationshipName, boolean selfLink) {
		String url;
		ResourceRegistry resourceRegistry = moduleRegistry.getResourceRegistry();
		if (id instanceof Collection) {
			if (resourceInformation.isNested()) {
				throw new UnsupportedOperationException("not yet implemented");
			}
			url = resourceRegistry.getResourceUrl(queryContext, resourceInformation);
			if (url == null) {
				return null;
			}
			Collection<?> ids = (Collection<?>) id;
			Collection<String> strIds = new ArrayList<>();
			for (Object idElem : ids) {
				String strIdElem = resourceInformation.toIdString(idElem);
				strIds.add(strIdElem);
			}
			url += "/";
			url += StringUtils.join(",", strIds);
		}
		else if (id != null) {
			url = resourceRegistry.getResourceUrl(queryContext, resourceInformation, id);
		}
		else {
			url = resourceRegistry.getResourceUrl(queryContext, resourceInformation);
		}
		if (url == null) {
			return null;
		}
		if (relationshipName != null && selfLink) {
			url += "/relationships/" + relationshipName;
		}
		else if (relationshipName != null) {
			url += "/" + relationshipName;
		}

		UrlParameterBuilder urlBuilder = new UrlParameterBuilder(url);

		QuerySpec querySpec = (QuerySpec) query;
		QuerySpecUrlMapper urlMapper = moduleRegistry.getUrlMapper();
		urlBuilder.addQueryParameters(urlMapper.serialize(querySpec, queryContext));

		if (queryContext != null) {
			addPropagatedParameters(urlBuilder, queryContext.getRequestContext());
		}

		return urlBuilder.toString();
	}

	@Override
	public String filterUrl(String url, QueryContext queryContext) {
		UrlParameterBuilder urlBuilder = new UrlParameterBuilder(url);
		if (queryContext != null) {
			addPropagatedParameters(urlBuilder, queryContext.getRequestContext());
		}
		return urlBuilder.toString();
	}

	private void addPropagatedParameters(UrlParameterBuilder urlBuilder, HttpRequestContext requestContext) {
		if (requestContext != null) {
			for (String propagedParameter : propagatedParameters) {
				Set<String> propagatedValues = requestContext.getRequestParameters().get(propagedParameter);
				if (propagatedValues != null) {
					urlBuilder.addQueryParameter(propagedParameter, propagatedValues);
				}
			}
		}
	}

	public static class UrlParameterBuilder {

		private StringBuilder builder = new StringBuilder();

		private boolean firstParam;

		private String encoding = "UTF-8";

		public UrlParameterBuilder(String baseUrl) {
			PreconditionUtil.verify(baseUrl != null, "baseUrl must not be null");
			builder.append(baseUrl);
			firstParam = !baseUrl.contains("?");
		}

		@Override
		public String toString() {
			return builder.toString();
		}

		public void addQueryParameters(Map<String, ?> params) {
			if (params != null && !params.isEmpty()) {
				for (Map.Entry<String, ?> entry : params.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();
					addQueryParameter(key, value);
				}
			}
		}

		public void addQueryParameter(String key, final String value) {
			if (firstParam) {
				builder.append("?");
				firstParam = false;
			}
			else {
				builder.append("&");
			}
			builder.append(key);
			builder.append("=");
			ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					builder.append(URLEncoder.encode(value, encoding));
					return null;
				}
			});
		}

		public void addQueryParameter(String key, Object value) {
			if (value instanceof Collection) {
				for (Object element : (Collection<?>) value) {
					addQueryParameter(key, (String) element);
				}
			}
			else {
				addQueryParameter(key, (String) value);
			}
		}
	}
}
