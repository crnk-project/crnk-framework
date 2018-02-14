package io.crnk.home;

import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.internal.http.HttpRequestProcessorImpl;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.TestModule;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class JsonHomeFormatTest {

	private CrnkBoot boot;

	private HomeModule module;

	@Before
	public void setup() {
		MetaModuleConfig config = new MetaModuleConfig();
		config.addMetaProvider(new ResourceMetaProvider());
		MetaModule metaModule = MetaModule.createServerModule(config);

		this.module = Mockito.spy(HomeModule.create(HomeFormat.JSON_HOME));
		boot = new CrnkBoot();
		boot.addModule(module);
		boot.addModule(metaModule);
		boot.addModule(new TestModule());
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.boot();
	}


	@Test
	public void testWithHomeRequest() throws IOException {
		testHomeJsonReturned(false);
	}

	private void testHomeJsonReturned(boolean anyRequest) throws IOException {
		ArgumentCaptor<Integer> statusCaptor = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<byte[]> responseCaptor = ArgumentCaptor.forClass(byte[].class);

		HttpRequestContextBase requestContextBase = Mockito.mock(HttpRequestContextBase.class);

		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/");
		Mockito.when(requestContextBase.getRequestHeader("Accept"))
				.thenReturn(anyRequest ? "*" : HomeModule.JSON_HOME_CONTENT_TYPE);

		HttpRequestProcessorImpl requestDispatcher = boot.getRequestDispatcher();
		requestDispatcher.process(requestContextBase);

		Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(statusCaptor.capture(), responseCaptor.capture());
		String expectedContentType = anyRequest ? HomeModule.JSON_CONTENT_TYPE : HomeModule.JSON_HOME_CONTENT_TYPE;
		Mockito.verify(requestContextBase, Mockito.times(1)).setResponseHeader("Content-Type", expectedContentType);
		Assert.assertEquals(200, (int) statusCaptor.getValue());

		String json = new String(responseCaptor.getValue());
		JsonNode response = boot.getObjectMapper().reader().readTree(json);

		JsonNode resourcesNode = response.get("resources");
		JsonNode usersNode = resourcesNode.get("tag:tasks");
		Assert.assertEquals("tasks", usersNode.get("href").asText());
	}
}
