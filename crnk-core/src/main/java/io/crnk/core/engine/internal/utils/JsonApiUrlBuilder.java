package io.crnk.core.engine.internal.utils;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.DefaultQuerySpecSerializer;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.QuerySpecSerializer;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.queryParams.DefaultQueryParamsSerializer;
import io.crnk.legacy.queryParams.QueryParams;
import io.crnk.legacy.queryParams.QueryParamsSerializer;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

public class JsonApiUrlBuilder {

	private QueryParamsSerializer queryParamsSerializer = new DefaultQueryParamsSerializer();

	private QuerySpecSerializer querySpecSerializer;

	private ResourceRegistry resourceRegistry;

	public JsonApiUrlBuilder(ResourceRegistry resourceRegistry) {
		this.resourceRegistry = resourceRegistry;
		this.querySpecSerializer = new DefaultQuerySpecSerializer(resourceRegistry);
	}

	public String buildUrl(ResourceInformation resourceInformation, Object id, QueryParams queryParams) {
		return buildUrl(resourceInformation, id, queryParams, null);
	}

	public String buildUrl(ResourceInformation resourceInformation, Object id, QuerySpec querySpec) {
		return buildUrl(resourceInformation, id, querySpec, null);
	}

	public String buildUrl(ResourceInformation resourceInformation, Object id, QueryAdapter queryAdapter,
			String relationshipName) {
		if (queryAdapter instanceof QuerySpecAdapter) {
			return buildUrl(resourceInformation, id, ((QuerySpecAdapter) queryAdapter).getQuerySpec(), relationshipName);
		}
		else {
			return buildUrl(resourceInformation, id, ((QueryParamsAdapter) queryAdapter).getQueryParams(), relationshipName);
		}
	}

	public String buildUrl(ResourceInformation resourceInformation, Object id, QuerySpec querySpec, String relationshipName) {
		return buildUrlInternal(resourceInformation, id, querySpec, relationshipName);
	}

	public String buildUrl(ResourceInformation resourceInformation, Object id, QueryParams queryParams, String
			relationshipName) {
		return buildUrlInternal(resourceInformation, id, queryParams, relationshipName);
	}

	private String buildUrlInternal(ResourceInformation resourceInformation, Object id, Object query, String relationshipName) {
		String url = resourceRegistry.getResourceUrl(resourceInformation);

		if (id instanceof Collection) {
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
			String strId = resourceInformation.toIdString(id);
			url += "/" + strId;
		}
		if (relationshipName != null) {
			url += "/relationships/" + relationshipName;
		}

		UrlParameterBuilder urlBuilder = new UrlParameterBuilder(url);
		if (query instanceof QuerySpec) {
			QuerySpec querySpec = (QuerySpec) query;
			urlBuilder.addQueryParameters(querySpecSerializer.serialize(querySpec));
		}
		else if (query instanceof QueryParams) {
			QueryParams queryParams = (QueryParams) query;
			urlBuilder.addQueryParameters(queryParamsSerializer.serializeFilters(queryParams));
			urlBuilder.addQueryParameters(queryParamsSerializer.serializeSorting(queryParams));
			urlBuilder.addQueryParameters(queryParamsSerializer.serializeGrouping(queryParams));
			urlBuilder.addQueryParameters(queryParamsSerializer.serializePagination(queryParams));
			urlBuilder.addQueryParameters(queryParamsSerializer.serializeIncludedFields(queryParams));
			urlBuilder.addQueryParameters(queryParamsSerializer.serializeIncludedRelations(queryParams));
		}
		return urlBuilder.toString();
	}

	class UrlParameterBuilder {

		private StringBuilder builder = new StringBuilder();

		private boolean firstParam;

		private String encoding = "UTF-8";

		public UrlParameterBuilder(String baseUrl) {
			builder.append(baseUrl);
			firstParam = !baseUrl.contains("?");
		}

		@Override
		public String toString() {
			return builder.toString();
		}

		private void addQueryParameters(Map<String, ?> params) {
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

		private void addQueryParameter(String key, Object value) {
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
