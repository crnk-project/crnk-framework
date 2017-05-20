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

	byte[] getRequestBody() throws IOException;

	void setResponseHeader(String name, String value);

	void setResponse(int code, byte[] body) throws IOException;

	String getMethod();

	String getResponseHeader(String name);
}
