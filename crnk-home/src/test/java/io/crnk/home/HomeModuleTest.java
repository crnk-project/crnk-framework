package io.crnk.home;

import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.RepositoryFilter;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.http.HttpRequestProcessorImpl;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.test.mock.ClassTestUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;

public class HomeModuleTest {

	private CrnkBoot boot;

	private HomeModule module;

	@Before
	public void setup() {
		this.module = Mockito.spy(HomeModule.create());
		boot = new CrnkBoot();
		boot.addModule(module);
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery("io.crnk.test.mock", new SampleJsonServiceLocator
				()));
		boot.boot();
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
	public void testWithAnyRequest() throws IOException {
		testHomeJsonReturned(true);
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
		JsonNode usersNode = resourcesNode.get("tag:/tasks");
		Assert.assertEquals("/tasks", usersNode.get("href").asText());
	}

	@Test
	public void testNonRootRequestNotTouchedForDifferentUrl() throws IOException {
		HttpRequestContextBase requestContextBase = Mockito.mock(HttpRequestContextBase.class);

		// create json api request
		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/doesNotExists");
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");

		// execute
		HttpRequestProcessorImpl requestDispatcher = boot.getRequestDispatcher();
		requestDispatcher.process(requestContextBase);
		Mockito.verify(requestContextBase, Mockito.times(0)).setResponse(Mockito.anyInt(), (byte[]) Mockito.anyObject());
	}

	@Test
	public void testNonRootRequestNotTouchedForDifferentContentType() throws IOException {
		HttpRequestContextBase requestContextBase = Mockito.mock(HttpRequestContextBase.class);

		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/");
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("application/doesNotExists");

		HttpRequestProcessorImpl requestDispatcher = boot.getRequestDispatcher();
		requestDispatcher.process(requestContextBase);
		Mockito.verify(requestContextBase, Mockito.times(0)).setResponse(Mockito.anyInt(), (byte[]) Mockito.anyObject());
	}


}
