package io.crnk.core.engine.http;

import io.crnk.core.engine.url.ServiceUrlProvider;

public class HttpRequestContextProvider {

	private ThreadLocal<HttpRequestContext> threadLocal = new ThreadLocal<>();

	private ServiceUrlProvider serviceUrlProvider = new ServiceUrlProvider() {
		@Override
		public String getUrl() {
			HttpRequestContext request = threadLocal.get();
			if (request == null) {
				throw new IllegalStateException("HttpRequestContext not available, make sure to call onRequestStarted in advance");
			}
			return request.getBaseUrl();
		}
	};

	public HttpRequestContext getRequestContext() {
		return threadLocal.get();
	}

	public void onRequestStarted(HttpRequestContext request) {
		threadLocal.set(request);
	}

	public void onRequestFinished() {
		threadLocal.remove();
	}

	public ServiceUrlProvider getServiceUrlProvider() {
		return serviceUrlProvider;
	}

	public void setServiceUrlProvider(ServiceUrlProvider serviceUrlProvider) {
		this.serviceUrlProvider = serviceUrlProvider;
	}
}
