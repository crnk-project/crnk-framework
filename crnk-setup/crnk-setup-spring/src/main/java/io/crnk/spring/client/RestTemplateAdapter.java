package io.crnk.spring.client;

import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterListener;
import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.core.engine.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class RestTemplateAdapter implements HttpAdapter {

    private RestTemplate impl;

    private CopyOnWriteArrayList<RestTemplateAdapterListener> nativeListeners = new CopyOnWriteArrayList<>();

    private CopyOnWriteArrayList<HttpAdapterListener> listeners = new CopyOnWriteArrayList<>();

    private Long networkTimeout;

    private boolean initialized = false;

    public static RestTemplateAdapter newInstance() {
        return new RestTemplateAdapter();
    }

    public RestTemplateAdapter() {
        this(null);
    }

    public RestTemplateAdapter(RestTemplate template) {
        this.impl = template;
    }

    @Override
    public void addListener(HttpAdapterListener listener) {
        checkNotInitialized();
        listeners.add(listener);
    }


    public void addListener(RestTemplateAdapterListener listener) {
        checkNotInitialized();
        nativeListeners.add(listener);
    }

    private void checkNotInitialized() {
        if (initialized) {
            throw new IllegalStateException("already initialized");
        }
    }

    public RestTemplate getImplementation() {
        if (!initialized) {
            initImpl();
        }
        return impl;
    }

    private synchronized void initImpl() {
        if (!initialized) {
            initialized = true;
            if (impl == null) {
                impl = new RestTemplate();
            }

            if (networkTimeout != null) {
                ClientHttpRequestFactory requestFactory = impl.getRequestFactory();
                if (requestFactory instanceof SimpleClientHttpRequestFactory) {
                    SimpleClientHttpRequestFactory simpleRequestFactory =
                            (SimpleClientHttpRequestFactory) impl.getRequestFactory();
                    simpleRequestFactory.setReadTimeout(networkTimeout.intValue());
                } else if (requestFactory instanceof HttpComponentsClientHttpRequestFactory) {
                    HttpComponentsClientHttpRequestFactory apacheRequestFactory =
                            (HttpComponentsClientHttpRequestFactory) impl.getRequestFactory();
                    apacheRequestFactory.setReadTimeout(networkTimeout.intValue());
                } else if (requestFactory instanceof OkHttp3ClientHttpRequestFactory) {
                    OkHttp3ClientHttpRequestFactory okhttpRequestFactory =
                            (OkHttp3ClientHttpRequestFactory) impl.getRequestFactory();
                    okhttpRequestFactory.setReadTimeout(networkTimeout.intValue());
                } else {
                    throw new IllegalStateException("unknown type " + requestFactory);
                }

            }

            for (RestTemplateAdapterListener listener : nativeListeners) {
                listener.onBuild(impl);
            }
        }
    }

    @Override
    public HttpAdapterRequest newRequest(String url, HttpMethod method, String requestBody) {
        RestTemplate implementation = getImplementation();
        return new RestTemplateRequest(implementation, url, method, requestBody, listeners);
    }

    @Override
    public void setReceiveTimeout(int timeout, TimeUnit unit) {
        checkNotInitialized();
        networkTimeout = unit.toMillis(timeout);
    }
}
