package io.crnk.ui;


import java.io.IOException;

import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.ui.internal.UIHttpRequestProcessor;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class UIModuleTest {


	@Test
	public void ui() {
		UIModule module = UIModule.create(new UIModuleConfig());
		Assert.assertEquals("ui", module.getModuleName());
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
