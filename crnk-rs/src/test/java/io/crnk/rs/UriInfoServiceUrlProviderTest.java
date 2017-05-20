package io.crnk.rs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.boot.CrnkProperties;
import io.crnk.legacy.locator.SampleJsonServiceLocator;
import io.crnk.legacy.queryParams.DefaultQueryParamsParser;
import io.crnk.legacy.queryParams.QueryParamsBuilder;
import io.crnk.rs.controller.SampleControllerWithPrefix;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import static io.crnk.rs.type.JsonApiMediaType.APPLICATION_JSON_API_TYPE;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

public class UriInfoServiceUrlProviderTest extends JerseyTest {

	@Override
	protected TestContainerFactory getTestContainerFactory() {
		return new JettyTestContainerFactory();
	}

	@Override
	protected Application configure() {
		return new TestApplication();
	}

	@Test
	public void testLinkToHaveValidUrl() {
		String headerTestValue = "test value";
		String taskResourceResponse = target("/tasks/1").request(APPLICATION_JSON_API_TYPE).header("X-test", headerTestValue)
				.get(String.class);

		assertThatJson(taskResourceResponse).node("data.relationships.project.links.self")
				.isStringEqualTo(this.target().getUri().toString() + "tasks/1/relationships/project");
		assertThatJson(taskResourceResponse).node("data.relationships.project.links.related")
				.isStringEqualTo(this.target().getUri().toString() + "tasks/1/project");

	}

	@ApplicationPath("/")
	private static class TestApplication extends ResourceConfig {

		public TestApplication() {
			property(CrnkProperties.RESOURCE_SEARCH_PACKAGE, "io.crnk.rs.resource");
			register(SampleControllerWithPrefix.class);
			register(new CrnkFeature(new ObjectMapper(), new QueryParamsBuilder(new DefaultQueryParamsParser()),
					new SampleJsonServiceLocator()));

		}
	}
}
