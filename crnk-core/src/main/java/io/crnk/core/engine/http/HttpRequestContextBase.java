package io.crnk.core.engine.http;

import java.net.URI;
import java.util.Map;
import java.util.Set;

public interface HttpRequestContextBase {

	Set<String> getRequestHeaderNames();

	String getRequestHeader(String name);

	Map<String, Set<String>> getRequestParameters();

	String getPath();

	String getBaseUrl();

	byte[] getRequestBody();

	String getMethod();

	URI getRequestUri();

	HttpResponse getResponse();

	void setResponse(HttpResponse response);
}
