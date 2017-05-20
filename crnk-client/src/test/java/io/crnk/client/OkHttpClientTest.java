package io.crnk.client;

import io.crnk.client.http.okhttp.OkHttpAdapter;

public class OkHttpClientTest extends QuerySpecClientTest {

	@Override
	protected void setupClient(CrnkClient client) {
		super.setupClient(client);
		client.setHttpAdapter(OkHttpAdapter.newInstance());
	}
}
