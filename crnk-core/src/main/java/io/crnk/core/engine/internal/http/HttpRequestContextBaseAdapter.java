package io.crnk.core.engine.internal.http;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.query.QueryContext;

public class HttpRequestContextBaseAdapter implements HttpRequestContext {

	private HttpRequestContextBase base;

	private boolean hasResponse;

	private Map<String, Object> requestAttributes = new ConcurrentHashMap<>();

	private QueryContext queryContext = new QueryContext();

	public HttpRequestContextBaseAdapter(HttpRequestContextBase base) {
		this.base = base;
		this.queryContext.setBaseUrl(base.getBaseUrl());
		this.queryContext.setAttributes(requestAttributes);
		this.queryContext.setRequestPath(base.getPath());
	}

	public boolean hasResponse() {
		return hasResponse;
	}

	@Override
	public boolean accepts(String contentType) {
		String accept = getRequestHeader(HttpHeaders.HTTP_HEADER_ACCEPT);
		if (accept == null) {
			return false;
		}
		for (String acceptElement : accept.split("\\,")) {
			acceptElement = acceptElement.trim();
			if (isCompatible(acceptElement, contentType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean acceptsAny() {
		String accept = getRequestHeader(HttpHeaders.HTTP_HEADER_ACCEPT);
		return accept == null || accepts("*") || accepts("*/*");
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		if (type.isInstance(this)) {
			return (T) this;
		}
		if (type.isInstance(base)) {
			return (T) base;
		}
		return null;
	}

	@Override
	public Object getRequestAttribute(String name) {
		return requestAttributes.get(name);
	}

	@Override
	public void setRequestAttribute(String name, Object value) {
		requestAttributes.put(name, value);
	}

	@Override
	public QueryContext getQueryContext() {
		return queryContext;
	}

	protected boolean isCompatible(String accept, String contentType) {
		String acceptLower = accept.toLowerCase();
		if (accept.equals(contentType)) {
			return true;
		}
		if (acceptLower.startsWith(contentType) && acceptLower.length() > contentType.length()) {
			char c = acceptLower.charAt(contentType.length());
			return c == ' ' || c == ';';
		}
		return false;
	}

	@Override
	public String getRequestHeader(String name) {
		return base.getRequestHeader(name);
	}

	@Override
	public Map<String, Set<String>> getRequestParameters() {
		return base.getRequestParameters();
	}

	@Override
	public String getPath() {
		return base.getPath();
	}

	@Override
	public String getBaseUrl() {
		return base.getBaseUrl();
	}

	@Override
	public byte[] getRequestBody() {
		return base.getRequestBody();
	}

	@Override
	public String getMethod() {
		return base.getMethod();
	}

	@Override
	public URI getRequestUri() {
		return base.getRequestUri();
	}


	@Override
	public HttpResponse getResponse() {
		return base.getResponse();
	}

	@Override
	public void setResponse(HttpResponse response) {
		hasResponse = true;
		base.setResponse(response);
	}
}
