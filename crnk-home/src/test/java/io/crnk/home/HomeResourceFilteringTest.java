package io.crnk.home;

import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilter;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.http.HttpRequestProcessorImpl;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.module.discovery.ReflectionsServiceDiscovery;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;

public class HomeResourceFilteringTest {

	private CrnkBoot boot;

	private HomeModule module;
	private ResourceFilter filter;

	@Before
	public void setup() {
		filter = Mockito.mock(ResourceFilter.class);
		SimpleModule filterModule = new SimpleModule("filter");
		filterModule.addResourceFilter(filter);

		this.module = Mockito.spy(HomeModule.create());
		boot = new CrnkBoot();
		boot.addModule(module);
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.setServiceDiscovery(new ReflectionsServiceDiscovery("io.crnk.test.mock", new SampleJsonServiceLocator
				()));
		boot.addModule(filterModule);
		boot.boot();
	}

	@Test
	public void checkDoesNotDoFiltering() throws IOException {
		Mockito.when(filter.filterResource(Mockito.any(ResourceInformation.class), Mockito.eq(HttpMethod.GET))).
				thenReturn(FilterBehavior.NONE);
		checkResponse(false);
	}

	@Test
	public void checkDoesDoFiltering() throws IOException {
		Mockito.when(filter.filterResource(Mockito.any(ResourceInformation.class), Mockito.eq(HttpMethod.GET))).
				thenReturn(FilterBehavior.FORBIDDEN);
		checkResponse(true);
	}


	private void checkResponse(boolean filtered) throws IOException {
		ArgumentCaptor<Integer> statusCaptor = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<byte[]> responseCaptor = ArgumentCaptor.forClass(byte[].class);

		HttpRequestContextBase requestContextBase = Mockito.mock(HttpRequestContextBase.class);

		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/");
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");

		HttpRequestProcessorImpl requestDispatcher = boot.getRequestDispatcher();
		requestDispatcher.process(requestContextBase);

		Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(statusCaptor.capture(), responseCaptor.capture());
		Assert.assertEquals(200, (int) statusCaptor.getValue());

		String json = new String(responseCaptor.getValue());
		JsonNode response = boot.getObjectMapper().reader().readTree(json);

		JsonNode resourcesNode = response.get("resources");
		JsonNode tasksNode = resourcesNode.get("tag:tasks");
		if (filtered) {
			Assert.assertNull(tasksNode);
		} else {
			Assert.assertEquals("/tasks/", tasksNode.get("href").asText());
		}
	}
}
