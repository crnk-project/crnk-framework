package io.crnk.client.internal;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.query.QueryContext;

public class ClientHttpRequestContext implements HttpRequestContext {

	private QueryContext queryContext;

	public ClientHttpRequestContext(QueryContext queryContext) {
		this.queryContext = queryContext;
	}

	@Override
	public boolean accepts(String contentType) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean acceptsAny() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getRequestAttribute(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setRequestAttribute(String name, Object value) {
		throw new UnsupportedOperationException();

	}

	@Override
	public QueryContext getQueryContext() {
		return queryContext;
	}

	@Override
	public boolean hasResponse() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> getRequestHeaderNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRequestHeader(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Set<String>> getRequestParameters() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPath() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getBaseUrl() {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] getRequestBody() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMethod() {
		throw new UnsupportedOperationException();
	}

	@Override
	public URI getRequestUri() {
		throw new UnsupportedOperationException();
	}

	@Override
	public HttpResponse getResponse() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setResponse(HttpResponse response) {
		throw new UnsupportedOperationException();
	}
}
