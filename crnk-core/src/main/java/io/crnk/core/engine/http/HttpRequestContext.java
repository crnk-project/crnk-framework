package io.crnk.core.engine.http;

import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.query.QueryContext;

public interface HttpRequestContext extends HttpRequestContextBase {

	boolean accepts(String contentType);

	/**
	 * @deprecated use {@link HttpResponse}
	 */
	@Deprecated
	void setContentType(String contentType);

	/**
	 * @deprecated use {@link HttpResponse}
	 */
	@Deprecated
	void setResponse(int statusCode, String text);

	boolean acceptsAny();

	<T> T unwrap(Class<T> type);

	Object getRequestAttribute(String name);

	void setRequestAttribute(String name, Object value);

	QueryContext getQueryContext();
}
