package io.crnk.home;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpHeaders;
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

public class JsonApiFormatTest {

	private CrnkBoot boot;

	private HomeModule module;

	@Before
	public void setup() {
		MetaModuleConfig config = new MetaModuleConfig();
		config.addMetaProvider(new ResourceMetaProvider());
		MetaModule metaModule = MetaModule.createServerModule(config);

		this.module = Mockito.spy(HomeModule.create(HomeFormat.JSON_API));
		boot = new CrnkBoot();
		boot.addModule(module);
		boot.addModule(metaModule);
		boot.addModule(new TestModule());
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.boot();
	}


	@Test
	public void testWithAnyRequest() throws IOException {
		testJsonApiReturned(true);
	}


	@Test
	public void testWithJsonApiRequest() throws IOException {
		testJsonApiReturned(false);
	}

	private void testJsonApiReturned(boolean anyRequest) throws IOException {
		ArgumentCaptor<HttpResponse> responseCaptor = ArgumentCaptor.forClass(HttpResponse.class);

		HttpRequestContextBase requestContextBase = Mockito.mock(HttpRequestContextBase.class);

		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/");
		Mockito.when(requestContextBase.getRequestHeader("Accept"))
				.thenReturn(anyRequest ? "*" : HttpHeaders.JSONAPI_CONTENT_TYPE);

		HttpRequestDispatcherImpl requestDispatcher = boot.getRequestDispatcher();
		requestDispatcher.process(requestContextBase);

		Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(responseCaptor.capture());
		String expectedContentType = anyRequest ? HomeModule.JSON_CONTENT_TYPE : HttpHeaders.JSONAPI_CONTENT_TYPE;
		Assert.assertEquals(expectedContentType, responseCaptor.getValue().getHeader(("Content-Type")));
		Assert.assertEquals(200, responseCaptor.getValue().getStatusCode());

		String json = new String(responseCaptor.getValue().getBody());
		JsonNode response = boot.getObjectMapper().reader().readTree(json);

		JsonNode resourcesNode = response.get("links");
		JsonNode tasksNode = resourcesNode.get("tasks");
		Assert.assertEquals("http://localhost/tasks", tasksNode.asText());

		Assert.assertEquals("http://localhost/meta/", resourcesNode.get("meta").asText());
		Assert.assertNull(resourcesNode.get("meta/resource"));
		Assert.assertNull(resourcesNode.get("meta/attribute"));
	}

	@Test
	public void testRequestSubDirectory() throws IOException {
		ArgumentCaptor<HttpResponse> responseCaptor = ArgumentCaptor.forClass(HttpResponse.class);

		HttpRequestContextBase requestContextBase = Mockito.mock(HttpRequestContextBase.class);

		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/meta/");

		HttpRequestDispatcherImpl requestDispatcher = boot.getRequestDispatcher();
		requestDispatcher.process(requestContextBase);

		Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(responseCaptor.capture());
		String expectedContentType = HomeModule.JSON_CONTENT_TYPE;
		Assert.assertEquals(expectedContentType, responseCaptor.getValue().getHeader("Content-Type"));
		Assert.assertEquals(200, responseCaptor.getValue().getStatusCode());
		String json = new String(responseCaptor.getValue().getBody());
		JsonNode response = boot.getObjectMapper().reader().readTree(json);

		JsonNode linksNode = response.get("links");
		JsonNode resourceNode = linksNode.get("resource");
		Assert.assertEquals("http://localhost/meta/resource", resourceNode.asText());

		Assert.assertNull(linksNode.get("meta"));
		Assert.assertNotNull(linksNode.get("element"));
		Assert.assertNotNull(linksNode.get("attribute"));
	}
}
