package io.crnk.client.http.apache;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.core.engine.http.HttpMethod;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

public class HttpClientAdapter implements HttpAdapter {

	private CloseableHttpClient impl;

	private CopyOnWriteArrayList<HttpClientAdapterListener> listeners = new CopyOnWriteArrayList<>();

	private Integer receiveTimeout;

	public static HttpClientAdapter newInstance() {
		return new HttpClientAdapter();
	}

	public void addListener(HttpClientAdapterListener listener) {
		if (impl != null) {
			throw new IllegalStateException("already initialized");
		}
		listeners.add(listener);
	}

	public CloseableHttpClient getImplementation() {
		if (impl == null) {
			initImpl();
		}
		return impl;
	}

	private synchronized void initImpl() {
		if (impl == null) {
			HttpClientBuilder builder = HttpClients.custom();

			if (receiveTimeout != null) {
				RequestConfig.Builder requestBuilder = RequestConfig.custom();
				requestBuilder = requestBuilder.setSocketTimeout(receiveTimeout);
				builder.setDefaultRequestConfig(requestBuilder.build());
			}

			for (HttpClientAdapterListener listener : listeners) {
				listener.onBuild(builder);
			}
			impl = builder.build();
		}
	}

	@Override
	public HttpAdapterRequest newRequest(String url, HttpMethod method, String requestBody) {
		CloseableHttpClient implementation = getImplementation();
		return new HttpClientRequest(implementation, url, method, requestBody);
	}

	@Override
	public void setReceiveTimeout(int timeout, TimeUnit unit) {
		receiveTimeout = (int) unit.toMillis(timeout);
	}
}
