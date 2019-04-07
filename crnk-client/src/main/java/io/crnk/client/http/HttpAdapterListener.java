package io.crnk.client.http;

public interface HttpAdapterListener {

    void onRequest(HttpAdapterRequest request);

    void onResponse(HttpAdapterRequest request, HttpAdapterResponse response);
}
