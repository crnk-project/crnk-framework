package io.crnk.core.engine.internal.http;

import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.internal.utils.ExceptionUtil;
import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class HttpRequestContextBaseAdapter implements HttpRequestContext {

	private HttpRequestContextBase base;

	private boolean hasResponse;

	private HashMap<String, Object> requestAttributes = new HashMap<>();

	public HttpRequestContextBaseAdapter(HttpRequestContextBase base) {
		this.base = base;
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
	public void setContentType(String contentType) {
		base.setResponseHeader(HttpHeaders.HTTP_CONTENT_TYPE, contentType);
	}

	@Override
	public void setResponse(final int statusCode, final String text) throws IOException {
		hasResponse = true;

		ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				String charSet = HttpHeaders.DEFAULT_CHARSET;

				/*
				TODO
				String contentType = base.getResponseHeader(HTTP_CONTENT_TYPE);
				String accept = getRequestHeader(HTTP_HEADER_ACCEPT);
				if (accept != null && contentType != null) {
					if (contentType == null) {
						throw new IllegalStateException("no contentType specified, cannot determine charset");
					}
					for (String acceptElement : accept.split("\\,")) {
						if (isCompatible(acceptElement, contentType)) {



						}
					}
				}*/

				byte[] bytes = text.getBytes(charSet);
				base.setResponse(statusCode, bytes);
				return null;
			}
		});
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
	public RepositoryMethodParameterProvider getRequestParameterProvider() {
		return base.getRequestParameterProvider();
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
	public byte[] getRequestBody() throws IOException {
		return base.getRequestBody();
	}

	@Override
	public void setResponseHeader(String name, String value) {
		base.setResponseHeader(name, value);
	}

	@Override
	public void setResponse(int code, byte[] body) throws IOException {
		hasResponse = true;
		base.setResponse(code, body);
	}

	@Override
	public String getMethod() {
		return base.getMethod();
	}

	@Override
	public String getResponseHeader(String name) {
		return base.getResponseHeader(name);
	}
}
