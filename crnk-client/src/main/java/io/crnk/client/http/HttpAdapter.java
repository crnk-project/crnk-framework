package io.crnk.client.http;

import io.crnk.core.engine.http.HttpMethod;

import java.util.concurrent.TimeUnit;

public interface HttpAdapter {

	HttpAdapterRequest newRequest(String url, HttpMethod method, String requestBody);

	void setReceiveTimeout(int timeout, TimeUnit unit);

}
