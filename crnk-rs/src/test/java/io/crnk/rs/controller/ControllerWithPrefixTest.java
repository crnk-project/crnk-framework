package io.crnk.rs.controller;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.rs.CrnkFeature;
import io.crnk.test.mock.TestModule;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;

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
			property(CrnkProperties.RESOURCE_DEFAULT_DOMAIN, "http://test.local");
			property(CrnkProperties.WEB_PATH_PREFIX, PREFIX);
			register(SampleControllerWithPrefix.class);

			CrnkFeature feature = new CrnkFeature();
			feature.addModule(new TestModule());
			register(feature);
		}
	}
}
