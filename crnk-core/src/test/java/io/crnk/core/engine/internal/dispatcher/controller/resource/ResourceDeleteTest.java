package io.crnk.core.engine.internal.dispatcher.controller.resource;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.internal.dispatcher.controller.BaseControllerTest;
import io.crnk.core.engine.internal.dispatcher.controller.ResourceDelete;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.queryParams.QueryParams;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ResourceDeleteTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = "DELETE";

	@Test
	public void onValidRequestShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("tasks/1");
		ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
		ResourceDelete sut = new ResourceDelete(resourceRegistry);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isTrue();
	}

	@Test
	public void onNonRelationRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = new ResourcePath("tasks/1/relationships/project");
		ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
		ResourceDelete sut = new ResourceDelete(resourceRegistry);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onGivenRequestResourceGetShouldHandleIt() throws Exception {
		// GIVEN

		JsonPath jsonPath = pathBuilder.build("/tasks/1");
		ResourceDelete sut = new ResourceDelete(resourceRegistry);

		// WHEN
		Response response = sut.handle(jsonPath, new QueryParamsAdapter(new QueryParams()), null, null);

		// THEN
		assertThat(response.getDocument()).isNull();
	}
}
