package io.crnk.home;

import com.fasterxml.jackson.databind.JsonNode;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilter;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpResponse;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.http.HttpRequestDispatcherImpl;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.SimpleModule;
import io.crnk.test.mock.TestModule;
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
		boot.addModule(new TestModule());
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
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
		ArgumentCaptor<HttpResponse> responseCaptor = ArgumentCaptor.forClass(HttpResponse.class);

		HttpRequestContextBase requestContextBase = Mockito.mock(HttpRequestContextBase.class);

		Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
		Mockito.when(requestContextBase.getPath()).thenReturn("/");
		Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");

		HttpRequestDispatcherImpl requestDispatcher = boot.getRequestDispatcher();
		requestDispatcher.process(requestContextBase);

		Mockito.verify(requestContextBase, Mockito.times(1)).setResponse(responseCaptor.capture());
		HttpResponse response = responseCaptor.getValue();
		Assert.assertEquals(200, response.getStatusCode());

		JsonNode json = boot.getObjectMapper().reader().readTree(new String(response.getBody()));
		JsonNode resourcesNode = json.get("links");
		JsonNode tasksNode = resourcesNode.get("tasks");
		if (filtered) {
			Assert.assertNull(tasksNode);
		} else {
			Assert.assertEquals("http://localhost/tasks", tasksNode.asText());
		}
	}
}
