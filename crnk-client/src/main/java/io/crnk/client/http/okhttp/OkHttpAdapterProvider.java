package io.crnk.client.http.okhttp;

import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterProvider;
import io.crnk.core.engine.internal.utils.ClassUtils;

public class OkHttpAdapterProvider implements HttpAdapterProvider {

	private static final String OK_HTTP_CLIENT_DETECTION_CLASS = "okhttp3.OkHttpClient";

	@Override
	public boolean isAvailable() {
		return ClassUtils.existsClass(OK_HTTP_CLIENT_DETECTION_CLASS);
	}

	@Override
	public HttpAdapter newInstance() {
		return OkHttpAdapter.newInstance();
	}
}
