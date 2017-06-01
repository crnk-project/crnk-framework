package io.crnk.client.http;

import io.crnk.client.AbstractClientTest;
import io.crnk.client.CrnkClient;
import io.crnk.client.http.apache.HttpClientAdapter;
import io.crnk.client.http.apache.HttpClientAdapterProvider;
import io.crnk.client.http.okhttp.OkHttpAdapter;
import io.crnk.client.http.okhttp.OkHttpAdapterProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HttpAdapterProviderTest extends AbstractClientTest {

	@Before
	public void setup() {
		client = new CrnkClient(getBaseUri().toString());
	}

	@Test(expected = IllegalStateException.class)
	public void shouldThrowExceptionIfNoProviderInstalled() {
		client.getHttpAdapterProviders().clear();
		client.getHttpAdapter();
	}

	@Test
	public void httpClientHasPriority() {
		Assert.assertTrue(client.getHttpAdapter() instanceof OkHttpAdapter);
	}

	@Test
	public void testOkHttpProvider() {
		OkHttpAdapterProvider provider = new OkHttpAdapterProvider();
		Assert.assertTrue(provider.isAvailable());
		Assert.assertTrue(provider.newInstance() instanceof OkHttpAdapter);
	}

	@Test
	public void testHttpClientProvider() {
		HttpClientAdapterProvider provider = new HttpClientAdapterProvider();
		Assert.assertTrue(provider.isAvailable());
		Assert.assertTrue(provider.newInstance() instanceof HttpClientAdapter);
	}

}