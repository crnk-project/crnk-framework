package io.crnk.home;

import java.io.IOException;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.internal.http.HttpRequestDispatcherImpl;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.test.mock.ClassTestUtils;
import io.crnk.test.mock.TestModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class HomeModuleTest {

	private CrnkBoot boot;

	private HomeModule module;

	@Before
	public void setup() {
		this.module = Mockito.spy(HomeModule.create(HomeFormat.JSON_HOME));
		boot = new CrnkBoot();
		boot.addModule(module);
		boot.addModule(new TestModule());
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.boot();
	}


	@Test
	public void checkAccepts() {
		HttpRequestContextBase context = Mockito.mock(HttpRequestContextBase.class);
		Mockito.when(context.getMethod()).thenReturn("GET");
		Mockito.when(context.getRequestHeader(Mockito.eq(HttpHeaders.HTTP_HEADER_ACCEPT))).thenReturn(HttpHeaders.JSON_CONTENT_TYPE);
		HttpRequestProcessor requestProcessor = module.getRequestProcessor();
		HttpRequestContextBaseAdapter contextAdapter = new HttpRequestContextBaseAdapter(context);

		Mockito.when(context.getPath()).thenReturn("/");
		Assert.assertTrue(requestProcessor.accepts(contextAdapter));

		Mockito.when(context.getPath()).thenReturn("/doesNotExists");
		Assert.assertFalse(requestProcessor.accepts(contextAdapter));

		Mockito.when(context.getPath()).thenReturn("/tasks");
		Assert.assertFalse(requestProcessor.accepts(contextAdapter));
	}

	@Test
	public void checkNotAcceptedDueToPathFilter() {
		module.addPathFilter(httpRequestContext -> false);

		HttpRequestContextBase context = Mockito.mock(HttpRequestContextBase.class);
		Mockito.when(context.getMethod()).thenReturn("GET");
		Mockito.when(context.getRequestHeader(Mockito.eq(HttpHeaders.HTTP_HEADER_ACCEPT))).thenReturn(HttpHeaders.JSON_CONTENT_TYPE);
		Mockito.when(context.getPath()).thenReturn("/");
		HttpRequestContextBaseAdapter contextAdapter = new HttpRequestContextBaseAdapter(context);

		HttpRequestProcessor requestProcessor = module.getRequestProcessor();
		Assert.assertFalse(requestProcessor.accepts(contextAdapter));
	}

	@Test
	public void hasProtectedConstructor() {
		ClassTestUtils.assertProtectedConstructor(HomeModule.class);
	}

	@Test
	public void moduleName() {
		HomeModule module = boot.getModuleRegistry().getModule(HomeModule.class).get();
		Assert.assertEquals("home", module.getModuleName());
	}

	@Test
	public void testNonRootRequestNotTouchedForDifferentUrl() throws IOException {
		HttpRequestContextBase requestContextBase = Mockito.mock(HttpRequestContextBase.class);

		// create json api request
		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/doesNotExists");
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");

		// execute
		HttpRequestDispatcherImpl requestDispatcher = boot.getRequestDispatcher();
		requestDispatcher.process(requestContextBase);
		Mockito.verify(requestContextBase, Mockito.times(0)).setResponse(Mockito.any(HttpResponse.class));
	}

	@Test
	public void testNonRootRequestNotTouchedForDifferentContentType() throws IOException {
		HttpRequestContextBase requestContextBase = Mockito.mock(HttpRequestContextBase.class);

		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/");
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("application/doesNotExists");

		HttpRequestDispatcherImpl requestDispatcher = boot.getRequestDispatcher();
		requestDispatcher.process(requestContextBase);
		Mockito.verify(requestContextBase, Mockito.times(0)).setResponse(Mockito.any(HttpResponse.class));
	}


}
