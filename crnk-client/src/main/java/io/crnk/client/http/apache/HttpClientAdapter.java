package io.crnk.client.http.apache;

import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterListener;
import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class HttpClientAdapter implements HttpAdapter {

    private CloseableHttpClient impl;

    private CopyOnWriteArrayList<HttpClientAdapterListener> nativeListeners = new CopyOnWriteArrayList<>();

    private CopyOnWriteArrayList<HttpAdapterListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void addListener(HttpAdapterListener listener) {
        checkNotInitialized();
        listeners.add(listener);
    }

    private Integer receiveTimeout;

    public static HttpClientAdapter newInstance() {
        return new HttpClientAdapter();
    }

    public void addListener(HttpClientAdapterListener listener) {
        checkNotInitialized();
        nativeListeners.add(listener);
    }

    private void checkNotInitialized() {
        if (impl != null) {
            throw new IllegalStateException("already initialized");
        }
    }

    public CloseableHttpClient getImplementation() {
        if (impl == null) {
            initImpl();
        }
        return impl;
    }

    private synchronized void initImpl() {
        if (impl == null) {
            HttpClientBuilder builder = createBuilder();

            if (receiveTimeout != null) {
                RequestConfig.Builder requestBuilder = RequestConfig.custom();
                requestBuilder = requestBuilder.setSocketTimeout(receiveTimeout);
                builder.setDefaultRequestConfig(requestBuilder.build());
            }

            for (HttpClientAdapterListener listener : nativeListeners) {
                listener.onBuild(builder);
            }
            impl = builder.build();
        }
    }

    private HttpClientBuilder createBuilder() {
        // brave enforces this, hopefully can be removed again eventually

        HttpClientBuilder builder = null;
        for (HttpClientAdapterListener listener : nativeListeners) {
            if (listener instanceof HttpClientBuilderFactory) {
                PreconditionUtil
                        .assertNull("only one module can contribute a HttpClientBuilder with HttpClientBuilderFactory", builder);
                builder = ((HttpClientBuilderFactory) listener).createBuilder();
            }
        }

        if (builder != null) {
            return builder;
        } else {
            return HttpClients.custom();
        }

    }

    @Override
    public HttpAdapterRequest newRequest(String url, HttpMethod method, String requestBody) {
        CloseableHttpClient implementation = getImplementation();
        return new HttpClientRequest(implementation, url, method, requestBody, listeners);
    }

    @Override
    public void setReceiveTimeout(int timeout, TimeUnit unit) {
        checkNotInitialized();
        receiveTimeout = (int) unit.toMillis(timeout);
    }
}
