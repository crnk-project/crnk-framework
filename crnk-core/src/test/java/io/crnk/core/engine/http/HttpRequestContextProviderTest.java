package io.crnk.core.engine.http;


import io.crnk.core.engine.result.ImmediateResultFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class HttpRequestContextProviderTest {

	@Test
	public void test() {
		ImmediateResultFactory resultFactory = new ImmediateResultFactory();
		HttpRequestContextProvider provider = new HttpRequestContextProvider(() -> resultFactory);

		HttpRequestContext context = Mockito.mock(HttpRequestContext.class);
		Mockito.when(context.getBaseUrl()).thenReturn("http://test");

		Assert.assertFalse(provider.hasThreadRequestContext());

		provider.onRequestStarted(context);
		Assert.assertTrue(provider.hasThreadRequestContext());
		Assert.assertSame(context, provider.getRequestContext());
		Assert.assertEquals("http://test", provider.getServiceUrlProvider().getUrl());
		provider.onRequestFinished();
		Assert.assertFalse(provider.hasThreadRequestContext());
	}
}
