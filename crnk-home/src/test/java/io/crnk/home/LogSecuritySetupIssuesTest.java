package io.crnk.home;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilter;
import io.crnk.core.engine.filter.ResourceFilterContext;
import io.crnk.core.engine.http.HttpHeaders;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpRequestProcessor;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.url.ConstantServiceUrlProvider;
import io.crnk.core.module.SimpleModule;
import io.crnk.test.mock.TestModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class LogSecuritySetupIssuesTest {

	private CrnkBoot boot;

	private HomeModule homeModule;

	@Before
	public void setup() {
		SimpleModule filterModule = new SimpleModule("filter");
		filterModule.addResourceFilter(new ResourceFilter() {
			@Override
			public FilterBehavior filterResource(ResourceFilterContext context, ResourceInformation resourceInformation, HttpMethod method) {
				return FilterBehavior.FORBIDDEN;
			}

			@Override
			public FilterBehavior filterField(ResourceFilterContext context, ResourceField field, HttpMethod method) {
				return FilterBehavior.FORBIDDEN;
			}
		});

		homeModule = HomeModule.create(HomeFormat.JSON_HOME);
		boot = new CrnkBoot();
		boot.addModule(homeModule);
		boot.addModule(filterModule);
		boot.addModule(new TestModule());
		boot.setServiceUrlProvider(new ConstantServiceUrlProvider("http://localhost"));
		boot.boot();

	}


	@Test
	public void checkAccepts() {
		HttpRequestContextBase context = Mockito.mock(HttpRequestContextBase.class);
		Mockito.when(context.getMethod()).thenReturn("GET");
		Mockito.when(context.getRequestHeader(Mockito.eq(HttpHeaders.HTTP_HEADER_ACCEPT)))
				.thenReturn(HttpHeaders.JSON_CONTENT_TYPE);
		HttpRequestProcessor requestProcessor = homeModule.getRequestProcessor();
		HttpRequestContextBaseAdapter contextAdapter = new HttpRequestContextBaseAdapter(context);

		Mockito.when(context.getPath()).thenReturn("/");
		Assert.assertFalse(homeModule.hasPotentialFilterIssues());
		Assert.assertTrue(requestProcessor.accepts(contextAdapter));
		Assert.assertTrue(homeModule.hasPotentialFilterIssues());
	}
}
