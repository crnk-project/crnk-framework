package io.crnk.core.engine.internal.http;

import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.query.QueryAdapter;

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