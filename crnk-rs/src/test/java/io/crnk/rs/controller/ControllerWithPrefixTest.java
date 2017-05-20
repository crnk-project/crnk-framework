package io.crnk.rs.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.queryParams.DefaultQueryParamsParser;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import io.crnk.rs.CrnkFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

public class ControllerWithPrefixTest extends ControllerTest {
	private static final String PREFIX = "/api/v1";

	@Override
	protected TestContainerFactory getTestContainerFactory() {
		return new JettyTestContainerFactory();
	}

	@Override
	protected Application configure() {
		return new TestApplication();
	}

	@Override
	protected String getPrefix() {
		return PREFIX;
	}

	@ApplicationPath("/")
	private static class TestApplication extends ResourceConfig {
		public TestApplication() {
			property(CrnkProperties.RESOURCE_SEARCH_PACKAGE, "io.crnk.rs.resource");
			property(CrnkProperties.RESOURCE_DEFAULT_DOMAIN, "http://test.local");
			property(CrnkProperties.WEB_PATH_PREFIX, PREFIX);
			register(SampleControllerWithPrefix.class);
			register(new CrnkFeature(new ObjectMapper(), new QueryParamsBuilder(new DefaultQueryParamsParser()), new SampleJsonServiceLocator()));

		}
	}
}
