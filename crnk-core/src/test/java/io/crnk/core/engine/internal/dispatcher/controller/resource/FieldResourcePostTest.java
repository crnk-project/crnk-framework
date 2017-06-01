package io.crnk.core.engine.internal.dispatcher.controller.resource;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.internal.dispatcher.controller.BaseControllerTest;
import io.crnk.core.engine.internal.dispatcher.controller.FieldResourcePost;
import io.crnk.core.engine.internal.dispatcher.controller.ResourcePost;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.dispatcher.path.ResourcePath;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.internal.QueryParamsAdapter;
import io.crnk.legacy.queryParams.QueryParams;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class FieldResourcePostTest extends BaseControllerTest {
	private static final String REQUEST_TYPE = HttpMethod.POST.name();

	@Test
	public void onValidRequestShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("tasks/1/project");
		ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
		FieldResourcePost sut = new FieldResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isTrue();
	}

	@Test
	public void onRelationshipRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = new ResourcePath("tasks/1/relationships/project");
		ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
		FieldResourcePost sut = new FieldResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onNonRelationRequestShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = new ResourcePath("tasks");
		ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
		FieldResourcePost sut = new FieldResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		assertThat(result).isFalse();
	}

	@Test
	public void onExistingParentResourceShouldSaveIt() throws Exception {
		// GIVEN
		Document newTaskDocument = new Document();
		newTaskDocument.setData(Nullable.of((Object) createTask()));

		JsonPath taskPath = pathBuilder.build("/tasks");
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper);

		// WHEN
		Response taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(new QueryParams()), null, newTaskDocument);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(taskId).isNotNull();

        /* ------- */

		// GIVEN
		Document newProjectDocument = new Document();
		newProjectDocument.setData(Nullable.of((Object) createProject()));

		JsonPath projectPath = pathBuilder.build("/tasks/" + taskId + "/project");
		FieldResourcePost sut = new FieldResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper);

		// WHEN
		Response projectResponse = sut.handle(projectPath, new QueryParamsAdapter(new QueryParams()), null, newProjectDocument);

		// THEN
		assertThat(projectResponse.getHttpStatus()).isEqualTo(HttpStatus.CREATED_201);
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		assertThat(projectId).isNotNull();

		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(taskId, "project", REQUEST_PARAMS);
		assertThat(project.getId()).isEqualTo(projectId);
	}

	@Test
	public void onExistingParentResourceShouldSaveToToMany() throws Exception {
		// GIVEN
		Document newTaskDocument = new Document();
		newTaskDocument.setData(Nullable.of((Object) createTask()));

		JsonPath taskPath = pathBuilder.build("/tasks");
		ResourcePost resourcePost = new ResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper);

		// WHEN
		Response taskResponse = resourcePost.handle(taskPath, new QueryParamsAdapter(new QueryParams()), null, newTaskDocument);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		Long taskId = Long.parseLong(taskResponse.getDocument().getSingleData().get().getId());
		assertThat(taskId).isNotNull();

        /* ------- */

		// GIVEN
		Document newProjectDocument = new Document();
		newProjectDocument.setData(Nullable.of((Object) createProject()));

		JsonPath projectPath = pathBuilder.build("/tasks/" + taskId + "/projects");
		FieldResourcePost sut = new FieldResourcePost(resourceRegistry, PROPERTIES_PROVIDER, typeParser, objectMapper, documentMapper);

		// WHEN
		Response projectResponse = sut.handle(projectPath, new QueryParamsAdapter(new QueryParams()), null, newProjectDocument);

		// THEN
		assertThat(projectResponse.getHttpStatus()).isEqualTo(HttpStatus.CREATED_201);
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("sample project");
		Long projectId = Long.parseLong(projectResponse.getDocument().getSingleData().get().getId());
		assertThat(projectId).isNotNull();

		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(taskId, "projects", REQUEST_PARAMS);
		assertThat(project.getId()).isEqualTo(projectId);
	}
}
