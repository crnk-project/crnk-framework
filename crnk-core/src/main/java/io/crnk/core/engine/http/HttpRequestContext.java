package io.crnk.core.engine.http;

import java.io.IOException;

public interface HttpRequestContext extends HttpRequestContextBase {

	boolean accepts(String contentType);

	void setContentType(String contentType);

	void setResponse(int statusCode, String text) throws IOException;

	boolean acceptsAny();

	<T> T unwrap(Class<T> type);
}
