package io.crnk.rs.controller;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.rs.CrnkFeature;
import io.crnk.test.mock.TestModule;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import static io.crnk.rs.type.JsonApiMediaType.APPLICATION_JSON_API_TYPE;

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

	@Test
	public void onCallWithoutWebpathPrefix() {
		//Getting task of id = 10000, simulates error and is throwing an exception we want to check.
		Response errorResponse = target("tasks")
				.request(APPLICATION_JSON_API_TYPE)
				.get();

		assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.NOT_FOUND_404);
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
