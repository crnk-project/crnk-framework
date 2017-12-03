package io.crnk.spring.client;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.core.engine.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RestTemplateAdapter implements HttpAdapter {

	private RestTemplate impl;

	private CopyOnWriteArrayList<RestTemplateAdapterListener> listeners = new CopyOnWriteArrayList<>();

	private Long networkTimeout;

	public static RestTemplateAdapter newInstance() {
		return new RestTemplateAdapter();
	}

	public void addListener(RestTemplateAdapterListener listener) {
		checkNotInitialized();
		listeners.add(listener);
	}

	private void checkNotInitialized() {
		if (impl != null) {
			throw new IllegalStateException("already initialized");
		}
	}

	public RestTemplate getImplementation() {
		if (impl == null) {
			initImpl();
		}
		return impl;
	}

	private synchronized void initImpl() {
		if (impl == null) {
			impl = new RestTemplate();

			if (networkTimeout != null) {
				ClientHttpRequestFactory requestFactory = impl.getRequestFactory();
				if (requestFactory instanceof ClientHttpRequestFactory) {
					SimpleClientHttpRequestFactory simpleRequestFactory =
							(SimpleClientHttpRequestFactory) impl.getRequestFactory();
					simpleRequestFactory.setReadTimeout(networkTimeout.intValue());
				}
				else if (requestFactory instanceof HttpComponentsClientHttpRequestFactory) {
					HttpComponentsClientHttpRequestFactory apacheRequestFactory =
							(HttpComponentsClientHttpRequestFactory) impl.getRequestFactory();
					apacheRequestFactory.setReadTimeout(networkTimeout.intValue());
				}
				else if (requestFactory instanceof OkHttp3ClientHttpRequestFactory) {
					OkHttp3ClientHttpRequestFactory okhttpRequestFactory =
							(OkHttp3ClientHttpRequestFactory) impl.getRequestFactory();
					okhttpRequestFactory.setReadTimeout(networkTimeout.intValue());
				}
				else {
					throw new IllegalStateException("unknown type " + requestFactory);
				}

			}

			for (RestTemplateAdapterListener listener : listeners) {
				listener.onBuild(impl);
			}
		}
	}

	@Override
	public HttpAdapterRequest newRequest(String url, HttpMethod method, String requestBody) {
		RestTemplate implementation = getImplementation();
		return new RestTemplateRequest(implementation, url, method, requestBody);
	}

	@Override
	public void setReceiveTimeout(int timeout, TimeUnit unit) {
		checkNotInitialized();
		networkTimeout = unit.toMillis(timeout);
	}
}
