package io.crnk.core.engine.http;

import io.crnk.core.engine.result.Result;
import io.crnk.core.engine.result.ResultFactory;
import io.crnk.core.engine.url.ServiceUrlProvider;
import io.crnk.core.utils.Supplier;

public class HttpRequestContextProvider {

	private Supplier<ResultFactory> resultFactory;

	private ServiceUrlProvider serviceUrlProvider = new ServiceUrlProvider() {
		@Override
		public String getUrl() {
			HttpRequestContext request = getRequestContext();
			if (request == null) {
				return null;
			}
			return request.getBaseUrl();
		}
	};

	public HttpRequestContextProvider(Supplier<ResultFactory> resultFactory) {
		this.resultFactory = resultFactory;
	}

	/**
	 * Warning! this method can only be used in a non-reactive setting or while the reactive request is being setup!
	 */
	public HttpRequestContext getRequestContext() {
		return (HttpRequestContext) resultFactory.get().getThreadContext();
	}

	/**
	 * Safe method to get HttpRequestContext in traditional and reactive settings. In a reactive setting it will make use of the
	 * subscriber context of Reactor.
	 */
	public Result<HttpRequestContext> getRequestContextResult() {
		return (Result) resultFactory.get().getContext();
	}


	public void onRequestStarted(HttpRequestContext request) {
		resultFactory.get().setThreadContext(request);
	}


	public boolean hasThreadRequestContext() {
		return resultFactory.get().hasThreadContext();
	}

	/**
	 * Warning: in a reactive setting the request may not really be finished.
	 */
	public void onRequestFinished() {
		resultFactory.get().clearContext();
	}

	public ServiceUrlProvider getServiceUrlProvider() {
		return serviceUrlProvider;
	}

	public void setServiceUrlProvider(ServiceUrlProvider serviceUrlProvider) {
		this.serviceUrlProvider = serviceUrlProvider;
	}

	public <T> Result<T> attach(Result<T> result) {
		HttpRequestContext requestContext = this.getRequestContext();
		return resultFactory.get().attachContext(result, requestContext);
	}
}
