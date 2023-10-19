package io.crnk.rs.controller;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.rs.CrnkFeature;
import io.crnk.test.mock.TestModule;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class ControllerWithoutPrefixTest extends ControllerTest {

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
		return null;
	}

	@Test
	public void onNonJsonApiPostCallShouldBeIgnored() {
		// WHEN
		Response response = target("tasks/1")
				.request(MediaType.MEDIA_TYPE_WILDCARD)
				.post(Entity.entity("binary", MediaType.APPLICATION_OCTET_STREAM_TYPE));

		// THEN
		assertThat(response.getStatusInfo().getFamily()).isEqualTo(Response.Status.Family.SUCCESSFUL);
		String responseString = response.readEntity(String.class);
		assertThat(responseString).isEqualTo(SampleOverlayingController.NON_RESOURCE_OVERLAY_RESPONSE);
	}

	@Test
	public void onNonJsonApiGetCallShouldBeIgnored() {
		// WHEN
		String response = target("tasks/1")
				.request(MediaType.TEXT_PLAIN_TYPE)
				.get(String.class);

		// THEN
		assertThat(response).isEqualTo(SampleOverlayingController.NON_RESOURCE_OVERLAY_RESPONSE);
	}

	@ApplicationPath("/")
	private static class TestApplication extends ResourceConfig {

		public TestApplication() {
			property(CrnkProperties.RESOURCE_DEFAULT_DOMAIN, "http://test.local");
			register(SampleControllerWithoutPrefix.class);
			register(SampleOverlayingController.class);

			CrnkFeature feature = new CrnkFeature();
			feature.addModule(new TestModule());
			register(feature);
		}
	}
}
