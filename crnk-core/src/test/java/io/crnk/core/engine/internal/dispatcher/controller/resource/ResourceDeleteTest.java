package io.crnk.core.engine.internal.dispatcher.controller.resource;

import static org.assertj.core.api.Assertions.assertThat;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.internal.dispatcher.controller.BaseControllerTest;
import io.crnk.core.engine.internal.dispatcher.controller.ResourceDelete;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import org.junit.Test;

public class ResourceDeleteTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = "DELETE";

	@Test
	public void onValidRequestShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("tasks/1");
		ResourceDelete sut = new ResourceDelete();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isTrue();
	}

	@Test
	public void onNonRelationRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = new ResourcePath("tasks/1/relationships/project");
		ResourceDelete sut = new ResourceDelete();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onGivenRequestResourceGetShouldHandleIt() throws Exception {
		// GIVEN

		JsonPath jsonPath = pathBuilder.build("/tasks/1");
		ResourceDelete sut = new ResourceDelete();
		sut.init(controllerContext);

		// WHEN
		Response response = sut.handle(jsonPath, emptyTaskQuery, null, null);

		// THEN
		assertThat(response.getDocument()).isNull();
	}
}
