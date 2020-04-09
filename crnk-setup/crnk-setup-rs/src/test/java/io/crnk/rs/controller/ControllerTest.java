package io.crnk.rs.controller;

import io.crnk.core.engine.internal.dispatcher.path.PathBuilder;
import io.crnk.test.JerseyTestBase;
import io.crnk.test.mock.TestExceptionMapper;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.repository.TaskRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;

import static io.crnk.rs.type.JsonApiMediaType.APPLICATION_JSON_API_TYPE;
import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class ControllerTest extends JerseyTestBase {

	@Before
	public void setup() {
		TaskRepository repo = new TaskRepository();

		Task task = new Task();
		task.setName("test value");
		task.setId(1L);
		repo.save(task);
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
		TaskRepository.clear();
	}


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
				.queryParam("filter[tasks][name]", "John")
				.request(APPLICATION_JSON_API_TYPE)
				.get(String.class);

		// THEN
		Assert.assertNotNull(taskResourceResponse);
	}

	@Test
	public void shouldReturnErrorResponseWhenMappedExceptionThrown() {

		//Getting task of id = 10000, simulates error and is throwing an exception we want to check.
		Response errorResponse = target(getPrefixForPath() + "tasks/10000")
				.request(APPLICATION_JSON_API_TYPE)
				.get();

		assertThat(errorResponse.getStatus()).isEqualTo(TestExceptionMapper.HTTP_ERROR_CODE);
		String errorBody = errorResponse.readEntity(String.class);
		assertThatJson(errorBody)
				.node("errors").isPresent();

	}

	protected String getPrefixForPath() {
		String prefix = getPrefix();
		return prefix != null ? prefix + PathBuilder.SEPARATOR : "";
	}

	protected abstract String getPrefix();
}
