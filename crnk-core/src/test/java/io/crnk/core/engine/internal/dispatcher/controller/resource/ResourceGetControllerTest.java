package io.crnk.core.engine.internal.dispatcher.controller.resource;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.Resource;
import io.crnk.core.engine.document.ResourceIdentifier;
import io.crnk.core.engine.internal.dispatcher.controller.BaseControllerTest;
import io.crnk.core.engine.internal.dispatcher.controller.RelationshipsResourcePostController;
import io.crnk.core.engine.internal.dispatcher.controller.ResourceGetController;
import io.crnk.core.engine.internal.dispatcher.controller.ResourcePostController;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Task;
import io.crnk.core.mock.models.TaskWithLookup;
import io.crnk.core.mock.repository.TaskToProjectRepository;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.utils.Nullable;
import io.crnk.legacy.queryParams.QueryParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceGetControllerTest extends BaseControllerTest {

	private static final String REQUEST_TYPE = "GET";

	@Before
	public void before() throws IOException {
		this.prepare();
	}

	@Test
	public void onGivenRequestCollectionGetShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/");
		ResourceGetController sut = new ResourceGetController();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, false);
	}

	@Test
	public void onGivenRequestResourceGetShouldAcceptIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/2");
		ResourceGetController sut = new ResourceGetController();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, REQUEST_TYPE);

		// THEN
		Assert.assertEquals(result, true);
	}

	@Test
	public void onMethodMismatchShouldDenyIt() {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/2");
		ResourceGetController sut = new ResourceGetController();
		sut.init(controllerContext);

		// WHEN
		boolean result = sut.isAcceptable(jsonPath, "POST");

		// THEN
		Assert.assertEquals(result, false);
	}

	@Test
	public void onGivenRequestResourceGetShouldHandleIt() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of((Object) data));

		JsonPath taskPath = pathBuilder.build("/tasks");

		// WHEN
		ResourcePostController resourcePost = new ResourcePostController();
		resourcePost.init(controllerContext);
		Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, newTaskBody);
		assertThat(taskResponse.getDocument().getData().get()).isExactlyInstanceOf(Resource.class);
		String taskId = ((Resource) taskResponse.getDocument().getData().get()).getId();
		assertThat(taskId).isNotNull();

		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/" + taskId);
		ResourceGetController sut = new ResourceGetController();
		sut.init(controllerContext);

		// WHEN
		Response response = sut.handle(jsonPath, emptyTaskQuery, null);

		// THEN
		Assert.assertNotNull(response);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void onGivenRequestResourceGetShouldThrowError() throws Exception {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/tasks/" + -1);
		ResourceGetController sut = new ResourceGetController();
		sut.init(controllerContext);

		// WHEN
		Response response = sut.handle(jsonPath, emptyTaskQuery, null);

		// THEN
		Assert.assertNull(response);
	}

	@Test
	public void onGivenRequestResourceShouldLoadAutoIncludeFields() throws Exception {
		// GIVEN
		JsonPath jsonPath = pathBuilder.build("/task-with-lookup/1");
		ResourceGetController responseGetResp = new ResourceGetController();
		responseGetResp.init(controllerContext);
		Map<String, Set<String>> queryParams = new HashMap<>();
		QuerySpec querySpec = new QuerySpec(TaskWithLookup.class);
		querySpec.includeRelation(PathSpec.of("project"));
		querySpec.includeRelation(PathSpec.of("projectNull"));
		querySpec.includeRelation(PathSpec.of("projectOverridden"));
		querySpec.includeRelation(PathSpec.of("projectOverriddenNull"));


		// WHEN
		QuerySpecAdapter queryParamsAdapter = container.toQueryAdapter(querySpec);
		Response response = responseGetResp.handle(jsonPath, queryParamsAdapter, null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getDocument().getData().get()).isExactlyInstanceOf(Resource.class);
		assertThat(response.getDocument().getSingleData().get().getType()).isEqualTo("task-with-lookup");
		Resource responseData = response.getDocument().getSingleData().get();
		assertThat(responseData.getRelationships().get("project").getSingleData().get().getId()).isEqualTo("42");
		assertThat(responseData.getRelationships().get("projectNull").getSingleData().get().getId()).isEqualTo("1");
		assertThat(responseData.getRelationships().get("projectOverridden").getSingleData().get().getId()).isEqualTo("1");
		assertThat(responseData.getRelationships().get("projectOverriddenNull").getSingleData().get().getId()).isEqualTo("1");
	}

	@Test
	public void onGivenRequestResourceShouldNotLoadAutoIncludeFields() throws Exception {
		// GIVEN
		Document newTaskBody = new Document();
		Resource data = createTask();
		newTaskBody.setData(Nullable.of((Object) data));

		JsonPath taskPath = pathBuilder.build("/tasks");
		ResourcePostController resourcePost = new ResourcePostController();
		resourcePost.init(controllerContext);

		// WHEN -- adding a task
		Response taskResponse = resourcePost.handle(taskPath, emptyTaskQuery, newTaskBody);

		// THEN
		assertThat(taskResponse.getDocument().getSingleData().get()).isExactlyInstanceOf(Resource.class);
		assertThat(taskResponse.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		assertThat(taskResponse.getDocument().getSingleData().get().getId()).isNotNull();

		/* ------- */

		// GIVEN
		Document newProjectBody = new Document();
		newProjectBody.setData(Nullable.of((Object) createProject()));

		JsonPath projectPath = pathBuilder.build("/projects");

		// WHEN -- adding a project
		Response projectResponse = resourcePost.handle(projectPath, emptyProjectQuery, newProjectBody);

		// THEN
		assertThat(projectResponse.getDocument().getSingleData().get()).isExactlyInstanceOf(Resource.class);
		assertThat(projectResponse.getDocument().getSingleData().get().getType()).isEqualTo("projects");
		assertThat(projectResponse.getDocument().getSingleData().get().getId()).isNotNull();
		assertThat(projectResponse.getDocument().getSingleData().get().getAttributes().get("name").asText()).isEqualTo("sample project");

		/* ------- */

		// GIVEN
		Document newTaskToProjectBody = new Document();
		ResourceIdentifier reldata = new ResourceIdentifier();
		newTaskToProjectBody.setData(Nullable.of((Object) data));
		data.setType("projects");
		data.setId("2");

		JsonPath savedTaskPath = pathBuilder.build("/tasks/" + TASK_ID + "/relationships/project");
		RelationshipsResourcePostController sut = new RelationshipsResourcePostController();
		sut.init(controllerContext);

		// WHEN -- adding a relation between task and project
		Response projectRelationshipResponse = sut.handle(savedTaskPath, emptyProjectQuery, newTaskToProjectBody);
		assertThat(projectRelationshipResponse).isNotNull();

		// THEN
		TaskToProjectRepository taskToProjectRepository = new TaskToProjectRepository();
		Project project = taskToProjectRepository.findOneTarget(TASK_ID, "project", new QueryParams());
		assertThat(project.getId()).isEqualTo(PROJECT_ID);

		// Given
		JsonPath jsonPath = pathBuilder.build("/tasks/" + TASK_ID);
		ResourceGetController responseGetResp = new ResourceGetController();
		responseGetResp.init(controllerContext);
		QuerySpec requestParams = new QuerySpec(Task.class);
		requestParams.includeRelation(PathSpec.of("project"));

		// WHEN
		Response response = responseGetResp.handle(jsonPath, container.toQueryAdapter(requestParams), null);

		// THEN
		Assert.assertNotNull(response);
		assertThat(response.getDocument().getSingleData().get().getType()).isEqualTo("tasks");
		assertThat(taskResponse.getDocument().getSingleData().get().getRelationships().get("project").getData().get()).isNull();
	}

}
