package io.crnk.client.http.okhttp;

import io.crnk.client.http.HttpAdapterListener;
import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpMethod;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OkHttpRequest implements HttpAdapterRequest {

    private static final MediaType CONTENT_TYPE = MediaType.parse(HttpHeaders.JSONAPI_CONTENT_TYPE_AND_CHARSET);

    private final String requestBody;

    private final String url;

    private final HttpMethod method;

    private Map<String, String> headers = new HashMap<>();

    private List<HttpAdapterListener> listeners;

    private Builder builder;

    private OkHttpClient client;

    public OkHttpRequest(OkHttpClient client, String url, HttpMethod method, String requestBody, List<HttpAdapterListener> listeners) {
        this.client = client;
        this.listeners = listeners;
        this.requestBody = requestBody;
        this.url = url;
        this.method = method;

        builder = new Request.Builder().url(url);


        RequestBody requestBodyObj = requestBody != null ? RequestBody.create(CONTENT_TYPE, requestBody) : null;
        builder.method(method.toString(), requestBodyObj);
    }

    @Override
    public void header(String name, String value) {
        builder = builder.header(name, value);
        headers.put(name, value);
    }

    @Override
    public HttpAdapterResponse execute() throws IOException {
        listeners.stream().forEach(it -> it.onRequest(this));
        Request request = builder.build();
        Response response = client.newCall(request).execute();
        OkHttpResponse adapterResponse = new OkHttpResponse(response);
        listeners.stream().forEach(it -> it.onResponse(this, adapterResponse));
        return adapterResponse;
    }

    @Override
    public String getBody() {
        return requestBody;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return method;
    }

    @Override
    public Set<String> getHeadersNames() {
        return headers.keySet();
    }

    @Override
    public String getHeaderValue(String name) {
        return headers.get(name);
    }

}
