package io.crnk.core.engine.http;


import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class HttpRequestContextProviderTest {

	@Test
	public void test() {
		HttpRequestContextProvider provider = new HttpRequestContextProvider();

		HttpRequestContext context = Mockito.mock(HttpRequestContext.class);
		Mockito.when(context.getBaseUrl()).thenReturn("http://test");

		Assert.assertNull(provider.getRequestContext());
		try {
			provider.getServiceUrlProvider().getUrl();
			Assert.fail();
		} catch (IllegalStateException e) {
			// ok
		}

		provider.onRequestStarted(context);
		Assert.assertSame(context, provider.getRequestContext());
		Assert.assertEquals("http://test", provider.getServiceUrlProvider().getUrl());
		provider.onRequestFinished();
		Assert.assertNull(provider.getRequestContext());
	}
}
