package io.crnk.rs.controller;

import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.rs.resource.exception.ExampleException;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static io.crnk.rs.type.JsonApiMediaType.APPLICATION_JSON_API_TYPE;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class ControllerTest extends JerseyTest {

	@Test
	public void onSimpleCollectionGetShouldReturnCollectionOfResources() {
		// WHEN
		String taskCollectionResponse = target(getPrefixForPath() + "tasks/")
				.request(APPLICATION_JSON_API_TYPE)
				.get(String.class);

		// THEN
		Assert.assertNotNull(taskCollectionResponse);
	}

	@Test
	public void onSimpleResourceGetShouldReturnOneResource() {
		// WHEN
		String headerTestValue = "test value";
		String taskResourceResponse = target(getPrefixForPath() + "tasks/1")
				.queryParam("filter")
				.request(APPLICATION_JSON_API_TYPE)
				.header("X-test", headerTestValue)
				.get(String.class);

		assertThatJson(taskResourceResponse)
				.node("data").isPresent()
				.node("data.attributes.name").isStringEqualTo(headerTestValue);
	}

	@Test
	public void onCollectionRequestWithParamsGetShouldReturnCollection() {
		// WHEN
		String taskResourceResponse = target(getPrefixForPath() + "tasks")
				.queryParam("filter[name]", "John")
				.request(APPLICATION_JSON_API_TYPE)
				.get(String.class);

		// THEN
		Assert.assertNotNull(taskResourceResponse);
	}

	@Test
	public void shouldReturnErrorResponseWhenMappedExceptionThrown() throws IOException {

		//Getting task of id = 5, simulates error and is throwing an exception we want to check.
		Response errorResponse = target(getPrefixForPath() + "tasks/5")
				.request(APPLICATION_JSON_API_TYPE)
				.get();

		assertThat(errorResponse.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR_500);
		String errorBody = errorResponse.readEntity(String.class);
		assertThatJson(errorBody)
				.node("errors").isPresent()
				.node("errors[0].id").isStringEqualTo(ExampleException.ERROR_ID);
		assertThatJson(errorBody).node("errors[0].title").isStringEqualTo(ExampleException.ERROR_TITLE);
	}

	@Test
	public void onNonJsonApiRequestShouldReturnOk() {
		// WHEN
		String response = target(getPrefixForPath() + "tasks/sample")
				.request()
				.get(String.class);

		// THEN
		assertThat(response).isEqualTo(SampleControllerWithoutPrefix.NON_RESOURCE_RESPONSE);
	}

	private String getPrefixForPath() {
		String prefix = getPrefix();
		return prefix != null ? prefix + PathBuilder.SEPARATOR : "";
	}

	protected abstract String getPrefix();
}
