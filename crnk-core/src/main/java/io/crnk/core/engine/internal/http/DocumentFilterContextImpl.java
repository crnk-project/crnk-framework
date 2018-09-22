package io.crnk.core.engine.internal.http;

import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.queryParams.QueryParams;

class DocumentFilterContextImpl implements DocumentFilterContext {

	protected JsonPath jsonPath;

	protected QueryAdapter queryAdapter;

	protected Document requestBody;

	private String method;

	public DocumentFilterContextImpl(JsonPath jsonPath, QueryAdapter queryAdapter, Document requestBody, String method) {
		this.jsonPath = jsonPath;
		this.queryAdapter = queryAdapter;
		this.requestBody = requestBody;
		this.method = method;
	}

	@Override
	public Document getRequestBody() {
		return requestBody;
	}

	@Override
	public QueryParams getQueryParams() {
		return ((QueryParamsAdapter) queryAdapter).getQueryParams();
	}

	@Override
	public QueryAdapter getQueryAdapter() {
		return queryAdapter;
	}

	@Override
	public JsonPath getJsonPath() {
		return jsonPath;
	}

	@Override
	public String getMethod() {
		return method;
	}
}