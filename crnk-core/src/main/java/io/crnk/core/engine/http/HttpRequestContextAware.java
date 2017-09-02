package io.crnk.core.engine.http;

/**
 * Can be implemented by repositories to get access to request parameters.
 */
public interface HttpRequestContextAware {

	void setHttpRequestContextProvider(HttpRequestContextProvider requestContextProvider);
}
