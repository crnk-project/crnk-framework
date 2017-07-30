package io.crnk.ui;


import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.module.Module;
import io.crnk.test.mock.ClassTestUtils;
import io.crnk.ui.internal.UIHttpRequestProcessor;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

public class UIModuleTest {


	@Test
	public void ui() {
		UIModuleConfig config = new UIModuleConfig();
		config.setPath("something");
		UIModule module = UIModule.create(config);
		Assert.assertEquals("ui", module.getModuleName());
		Assert.assertEquals("something", module.getConfig().getPath());
	}

	@Test
	public void setup() {
		UIModule module = UIModule.create(new UIModuleConfig());
		Module.ModuleContext context = Mockito.mock(Module.ModuleContext.class);
		module.setupModule(context);
		Mockito.verify(context, Mockito.times(1)).addHttpRequestProcessor(Mockito.any(HttpRequestProcessor.class));
	}

	@Test
	public void hasProtectedConstructor() {
		ClassTestUtils.assertProtectedConstructor(UIModule.class);
	}

	@Test
	public void processorReturnsFile() throws IOException {
		UIHttpRequestProcessor processor = new UIHttpRequestProcessor(new UIModuleConfig());

		HttpRequestContext context = Mockito.mock(HttpRequestContext.class);
		Mockito.when(context.getPath()).thenReturn("browse/index.html");
		Mockito.when(context.getMethod()).thenReturn("GET");

		processor.process(context);

		Mockito.verify(context, Mockito.times(1)).setResponse(Mockito.eq(200), Mockito.any(byte[].class));
		Mockito.verify(context, Mockito.times(1)).setContentType(Mockito.eq("text/html"));
	}


	@Test
	public void processorReturnsIndexHtmlForRootPage() throws IOException {
		UIHttpRequestProcessor processor = new UIHttpRequestProcessor(new UIModuleConfig());

		HttpRequestContext context = Mockito.mock(HttpRequestContext.class);
		Mockito.when(context.getMethod()).thenReturn("GET");
		Mockito.when(context.getPath()).thenReturn("browse/");

		processor.process(context);

		Mockito.verify(context, Mockito.times(1)).setResponse(Mockito.eq(200), Mockito.any(byte[].class));
	}


	@Test
	public void processorNotUsedForPost() throws IOException {
		UIHttpRequestProcessor processor = new UIHttpRequestProcessor(new UIModuleConfig());

		HttpRequestContext context = Mockito.mock(HttpRequestContext.class);
		Mockito.when(context.getMethod()).thenReturn("POST");
		Mockito.when(context.getPath()).thenReturn("browse/index.html");

		processor.process(context);
		Mockito.verify(context, Mockito.times(0)).setResponse(Mockito.anyInt(), Mockito.any(byte[].class));
	}

	@Test
	public void processorNotUsedForNonBrowsePath() throws IOException {
		UIHttpRequestProcessor processor = new UIHttpRequestProcessor(new UIModuleConfig());

		HttpRequestContext context = Mockito.mock(HttpRequestContext.class);
		Mockito.when(context.getMethod()).thenReturn("POST");
		Mockito.when(context.getPath()).thenReturn("somethingDifferent/index.html");

		processor.process(context);
		Mockito.verify(context, Mockito.times(0)).setResponse(Mockito.anyInt(), Mockito.any(byte[].class));
	}
}
