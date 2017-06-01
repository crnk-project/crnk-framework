package io.crnk.client.http.apache;

import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterProvider;
import io.crnk.core.engine.internal.utils.ClassUtils;

public class HttpClientAdapterProvider implements HttpAdapterProvider {

	private static final String APACHE_HTTP_CLIENT_DETECTION_CLASS = "org.apache.http.impl.client.CloseableHttpClient";

	@Override
	public boolean isAvailable() {
		return ClassUtils.existsClass(APACHE_HTTP_CLIENT_DETECTION_CLASS);
	}

	@Override
	public HttpAdapter newInstance() {
		return HttpClientAdapter.newInstance();
	}
}
