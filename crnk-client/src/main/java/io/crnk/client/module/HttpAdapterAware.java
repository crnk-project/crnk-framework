package io.crnk.client.module;

import io.crnk.client.http.HttpAdapter;

/**
 * Can be implemented by modules to get access to the HttpAdapter implementation.
 */
public interface HttpAdapterAware {

	void setHttpAdapter(HttpAdapter httpAdapter);
}
