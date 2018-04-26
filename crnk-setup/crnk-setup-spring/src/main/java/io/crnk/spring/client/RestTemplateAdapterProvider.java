package io.crnk.spring.client;

import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterProvider;
import io.crnk.core.engine.internal.utils.ClassUtils;

public class RestTemplateAdapterProvider implements HttpAdapterProvider {

	private static final String REST_TEMPLATE_DETECTION_CLASS = "org.springframework.web.client.RestTemplate";

	@Override
	public boolean isAvailable() {
		return ClassUtils.existsClass(REST_TEMPLATE_DETECTION_CLASS);
	}

	@Override
	public HttpAdapter newInstance() {
		return RestTemplateAdapter.newInstance();
	}
}
