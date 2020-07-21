package io.crnk.core.mock;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.query.QueryContext;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

public class TestHttpRequestContext implements HttpRequestContext {
	private byte[] requestBody = new byte[0];

	@Override
	public boolean accepts(String contentType) {
		return false;
	}

	@Override
	public boolean acceptsAny() {
		return false;
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		return null;
	}

	@Override
	public Object getRequestAttribute(String name) {
		return null;
	}

	@Override
	public void setRequestAttribute(String name, Object value) {

	}

	@Override
	public QueryContext getQueryContext() {
		return null;
	}

	@Override
	public boolean hasResponse() {
		return false;
	}

	@Override
	public Set<String> getRequestHeaderNames() {
		return null;
	}

	@Override
	public String getRequestHeader(String name) {
		return null;
	}

	@Override
	public Map<String, Set<String>> getRequestParameters() {
		return null;
	}

	@Override
	public String getPath() {
		return null;
	}

	@Override
	public String getBaseUrl() {
		return null;
	}

	@Override
	public byte[] getRequestBody() {
		return requestBody;
	}

	public void setRequestBody(byte[] requestBody) {
		this.requestBody = requestBody;
	}

	public void setRequestBody(String body) {
		requestBody = body.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public String getMethod() {
		return HttpMethod.GET.toString();
	}

	@Override
	public URI getRequestUri() {
		return null;
	}

	@Override
	public HttpResponse getResponse() {
		return null;
	}

	@Override
	public void setResponse(HttpResponse response) {

	}
}
