package io.crnk.core.engine.internal.dispatcher.controller.resource;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.internal.dispatcher.controller.BaseControllerTest;
import io.crnk.core.engine.internal.dispatcher.controller.ResourceDeleteController;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceDeleteControllerTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = "DELETE";

	@Test
	public void onValidRequestShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("tasks/1");
		ResourceDeleteController sut = new ResourceDeleteController();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isTrue();
	}

	@Test
	public void onNonRelationRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("tasks/1/relationships/project");
		ResourceDeleteController sut = new ResourceDeleteController();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onGivenRequestResourceGetShouldHandleIt() {
		// GIVEN

		JsonPath jsonPath = pathBuilder.build("/tasks/1");
		ResourceDeleteController sut = new ResourceDeleteController();
		sut.init(controllerContext);

		// WHEN
		Response response = sut.handle(jsonPath, emptyTaskQuery, null);

		// THEN
		assertThat(response.getDocument()).isNull();
	}
}
