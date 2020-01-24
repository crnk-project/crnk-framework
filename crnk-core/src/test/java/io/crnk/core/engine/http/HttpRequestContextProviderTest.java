package io.crnk.core.engine.http;


import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.ImmediateResultFactory;
import io.crnk.core.module.ModuleRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class HttpRequestContextProviderTest {

	@Test
	public void test() {
		ModuleRegistry moduleRegistry = Mockito.mock(ModuleRegistry.class);
		ResourceRegistry resourceRegistry = Mockito.mock(ResourceRegistry.class);
		Mockito.when(resourceRegistry.getLatestVersion()).thenReturn(2);
		Mockito.when(moduleRegistry.getResourceRegistry()).thenReturn(resourceRegistry);

		ImmediateResultFactory resultFactory = new ImmediateResultFactory();
		HttpRequestContextProvider provider = new HttpRequestContextProvider(() -> resultFactory, moduleRegistry);

		HttpRequestContext context = Mockito.mock(HttpRequestContext.class);
		Mockito.when(context.getBaseUrl()).thenReturn("http://test");
		QueryContext queryContext = new QueryContext();
		Mockito.when(context.getQueryContext()).thenReturn(queryContext);

		Assert.assertFalse(provider.hasThreadRequestContext());

		provider.onRequestStarted(context);
		Assert.assertTrue(provider.hasThreadRequestContext());
		Assert.assertSame(context, provider.getRequestContext());
		Assert.assertEquals("http://test", provider.getServiceUrlProvider().getUrl());
		provider.onRequestFinished();
		Assert.assertFalse(provider.hasThreadRequestContext());
		Assert.assertEquals(2, queryContext.getRequestVersion()); // set to latest from registry
	}
}
