package io.crnk.home;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.internal.http.HttpRequestDispatcherImpl;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.TestModule;
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

	@Test
	public void testWithAnyRequest() throws IOException {
		testHomeJsonReturned(true);
	}

	private void testHomeJsonReturned(boolean anyRequest) throws IOException {
		ArgumentCaptor<HttpResponse> responseCaptor = ArgumentCaptor.forClass(HttpResponse.class);

		HttpRequestContextBase requestContextBase = Mockito.mock(HttpRequestContextBase.class);

		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/");
		Mockito.when(requestContextBase.getRequestHeader("Accept"))
				.thenReturn(anyRequest ? "*" : HomeModule.JSON_HOME_CONTENT_TYPE);

		HttpRequestDispatcherImpl requestDispatcher = boot.getRequestDispatcher();
		requestDispatcher.process(requestContextBase);

		Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(responseCaptor.capture());
		String expectedContentType = anyRequest ? HomeModule.JSON_CONTENT_TYPE : HomeModule.JSON_HOME_CONTENT_TYPE;
		HttpResponse response = responseCaptor.getValue();
		Assert.assertEquals(expectedContentType, response.getContentType());
		Assert.assertEquals(200, response.getStatusCode());

		JsonNode json = boot.getObjectMapper().reader().readTree(new String(response.getBody()));
		JsonNode resourcesNode = json.get("resources");
		JsonNode usersNode = resourcesNode.get("tag:tasks");
		Assert.assertEquals("tasks", usersNode.get("href").asText());
	}
}
