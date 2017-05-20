package io.crnk.client.http.apache;

import org.apache.http.impl.client.HttpClientBuilder;

public class HttpClientAdapterListenerBase implements HttpClientAdapterListener {

	@Override
	public void onBuild(HttpClientBuilder builder) {
		// nothing to do
	}
}
