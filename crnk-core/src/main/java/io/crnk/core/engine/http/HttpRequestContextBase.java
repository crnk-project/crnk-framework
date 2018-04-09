package io.crnk.core.engine.http;

import io.crnk.legacy.internal.RepositoryMethodParameterProvider;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public interface HttpRequestContextBase {

	RepositoryMethodParameterProvider getRequestParameterProvider();

	String getRequestHeader(String name);

	Map<String, Set<String>> getRequestParameters();

	String getPath();

	String getBaseUrl();

	byte[] getRequestBody();

	/**
	 * @deprecated use {@link HttpResponse}
	 */
	@Deprecated
	void setResponseHeader(String name, String value);

	/**
	 * @deprecated use {@link HttpResponse}
	 */
	@Deprecated
	void setResponse(int code, byte[] body) throws IOException;

	String getMethod();

	/**
	 * @deprecated use {@link HttpResponse}
	 */
	@Deprecated
	String getResponseHeader(String name);

	HttpResponse getResponse();

	void setResponse(HttpResponse response);
}
