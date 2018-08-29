package io.crnk.core.engine.internal.http;

import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.query.QueryAdapterBuilder;
import io.crnk.core.module.Module;
import org.junit.Test;

import java.util.Random;

import static java.lang.Math.abs;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonApiRequestProcessorBaseTest {

	@Test
	public void shouldObtainVersionFromValidHeader() {
		int version = abs(new Random().nextInt());
		JsonApiRequestProcessorBase processorBase = new JsonApiRequestProcessorBase(
				mock(Module.ModuleContext.class), mock(QueryAdapterBuilder.class), mock(ControllerRegistry.class));
		HttpRequestContext requestContext = mock(HttpRequestContext.class);
		when(requestContext.getRequestHeader("Content-Type")).thenReturn("application/vnd.api.v" + version + "+json");

		int obtainedVersion = processorBase.obtainVersion(requestContext);

		assertEquals(version, obtainedVersion);
	}

	@Test
	public void obtainVersionShouldBeZeroForInvalidHeader() {
		JsonApiRequestProcessorBase processorBase = new JsonApiRequestProcessorBase(
				mock(Module.ModuleContext.class), mock(QueryAdapterBuilder.class), mock(ControllerRegistry.class));
		HttpRequestContext requestContext = mock(HttpRequestContext.class);
		when(requestContext.getRequestHeader("Content-Type")).thenReturn("application/json");

		int obtainedVersion = processorBase.obtainVersion(requestContext);

		assertEquals(0, obtainedVersion);
	}

	@Test
	public void obtainVersionShouldBeZeroWhenHeaderIsMissing() {
		JsonApiRequestProcessorBase processorBase = new JsonApiRequestProcessorBase(
				mock(Module.ModuleContext.class), mock(QueryAdapterBuilder.class), mock(ControllerRegistry.class));
		HttpRequestContext requestContext = mock(HttpRequestContext.class);
		when(requestContext.getRequestHeader("Content-Type")).thenReturn(null);

		int obtainedVersion = processorBase.obtainVersion(requestContext);

		assertEquals(0, obtainedVersion);
	}
}
