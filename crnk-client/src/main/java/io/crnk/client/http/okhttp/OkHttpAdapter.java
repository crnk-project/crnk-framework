package io.crnk.client.http.okhttp;

import io.crnk.client.http.HttpAdapter;
import io.crnk.client.http.HttpAdapterListener;
import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.core.engine.http.HttpMethod;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class OkHttpAdapter implements HttpAdapter {

    private OkHttpClient impl;

    private CopyOnWriteArrayList<HttpAdapterListener> listeners = new CopyOnWriteArrayList<>();

    private CopyOnWriteArrayList<OkHttpAdapterListener> nativeListeners = new CopyOnWriteArrayList<>();

    private Long networkTimeout;

    public static OkHttpAdapter newInstance() {
        return new OkHttpAdapter();
    }

    @Override
    public void addListener(HttpAdapterListener listener) {
        checkNotInitialized();
        listeners.add(listener);
    }

    public void addListener(OkHttpAdapterListener listener) {
        checkNotInitialized();
        nativeListeners.add(listener);
    }

    private void checkNotInitialized() {
        if (impl != null) {
            throw new IllegalStateException("already initialized");
        }
    }

    public OkHttpClient getImplementation() {
        if (impl == null) {
            initImpl();
        }
        return impl;
    }

    private synchronized void initImpl() {
        if (impl == null) {
            Builder builder = new OkHttpClient.Builder();

            if (networkTimeout != null) {
                builder.readTimeout(networkTimeout, TimeUnit.MILLISECONDS);
            }

            for (OkHttpAdapterListener listener : nativeListeners) {
                listener.onBuild(builder);
            }
            impl = builder.build();
        }
    }


    @Override
    public HttpAdapterRequest newRequest(String url, HttpMethod method, String requestBody) {
        OkHttpClient implementation = getImplementation();
        return new OkHttpRequest(implementation, url, method, requestBody, listeners);
    }

    @Override
    public void setReceiveTimeout(int timeout, TimeUnit unit) {
        checkNotInitialized();
        networkTimeout = unit.toMillis(timeout);
    }
}
